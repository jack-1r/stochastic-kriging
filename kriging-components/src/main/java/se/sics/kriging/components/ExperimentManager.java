package se.sics.kriging.components;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathworks.toolbox.javabuilder.MWException;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.stochastic.executors.AbortExecution;
import se.sics.stochastic.executors.PortExecutors;
import se.sics.stochastic.executors.RequestExecution;
import se.sics.stochastic.executors.ResponseExecution;
import se.sics.stochastic.kriging.ExpPoint;
import se.sics.stochastic.kriging.SimulatedAnnealing;
import se.sics.stochastic.kriging.StochasticKriging;
import se.sics.stochastic.sampling.LatinHypercube;

public class ExperimentManager extends ComponentDefinition {
    
    private static ExecutorService es = Executors.newCachedThreadPool();
    
    private final Logger logger = LoggerFactory
            .getLogger(ExperimentManager.class);
    
    Positive<PortExecutors> portExec = requires(PortExecutors.class);
    Negative<PortApplication> portApp = provides(PortApplication.class);
    Negative<PortStatus> portStatus = provides(PortStatus.class);
    
    private int seed = new Random().nextInt();
    private LatinHypercube sampler;
    private int expId = new Random().nextInt();
    private int initRepId = new Random().nextInt();
    private String model;
    private double[][] newPoints;
    HashMap<Integer, ExperimentPoint> designPoints;
    
    private Long startTime;
    
    private int[] coeffVector = null;
    private HashSet<int[]> coeffMatrix = null;
    private int N_SEP = 2; /* number of sub-partition per dimension */
    
    String source = this.getClass().getCanonicalName().toString();
    
    String baseCommand;
    int confidenceLevel; // 95%
    double confidenceLength;
    int num;
    double[] min;
    double[] max;
    double[] step;
    boolean[] isInteger;
    double mseThreshold;
    int nEffCores;
    int nFixedRun;
    
    StochasticKriging krg;
    SimulatedAnnealing anl;
    
    public ExperimentManager() {
        
        subscribe(handleInit, control);
        subscribe(handleStart, control);
        subscribe(handleResponse, portExec);
        subscribe(handleExpStart, portApp);
        subscribe(handleSKProcceed, portApp);
        subscribe(handleMSEMinimize, portApp);
        subscribe(handleMinimizationContinue, portApp);
        subscribe(handleRequestResult, portApp);
        subscribe(handleGraph, portApp);
        subscribe(handleAbort, portApp);
    }
    
    private Handler<InitExperiment> handleInit = new Handler<InitExperiment>() {
        public void handle(InitExperiment event) {
            logger.debug("initiated.");
        }
    };
    
    private Handler<Start> handleStart = new Handler<Start>() {
        public void handle(Start event) {
        }
    };
    
    private Handler<RequestExpStart> handleExpStart = new Handler<RequestExpStart>() {
        public void handle(RequestExpStart event) {
            logger.debug("request received.");
            
            designPoints = new HashMap<Integer, ExperimentPoint>();
            
            // load all the experiment's params
            baseCommand = "java -jar " + event.getJarPath();
            confidenceLevel = event.getConfidence();
            num = event.getNum();
            confidenceLength = event.getRatio();
            min = event.getLb();
            max = event.getUb();
            mseThreshold = event.getThreshold();
            isInteger = event.getIntVector();
            nEffCores = event.getEffnCores();
            
            coeffVector = new int[min.length];
            coeffMatrix = new HashSet<int[]>();
            step = new double[min.length];
            for (int i = 0; i < min.length; i++) {
                step[i] = (max[i] - min[i]) / N_SEP;
            }
            generateCoeffMatrix(min.length);
            
            if (nEffCores < num) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.WARNING,
                        "The number of cores are too few."), portStatus);
            }
            
            if (event.isFixedRun())
                nFixedRun = event.getnFixedRun();
            else
                nFixedRun = 0;
            
            // print all experiment data.
            printExpStatus();
            
            sampler = new LatinHypercube(num, min, max, isInteger);
            
            // initiate the SK solver
            krg = new StochasticKriging();
            
            // initiate the SA solver
            anl = new SimulatedAnnealing();
            
            // calculate the number of run per core
            int nRuns = Math.round((float) nEffCores / num);
            if (nRuns == 0) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.WARNING,
                        "The number of cores are too few."), portStatus);
                nRuns = 1;
            }
            
            // sample the input space and send the experiment request to the
            // executor.
            while (!sampler.isEmpty()) {
                // fill the designPoint HashMap with sample points.
                ExperimentPoint newPoint = new ExperimentPoint(expId,
                        initRepId, confidenceLevel);
                newPoint.setDesignPoint(sampler.getNext());
                logger.debug("New experiment point:{}",
                        Arrays.toString(newPoint.getDesignPoint()));
                
                // send the experiment request.
                for (int count = 0; count < nRuns; count++) {
                    requestExpRun(newPoint);
                    newPoint.increaseTotal();
                }
                
                // add the point to the list of design points
                designPoints.put(expId, newPoint);
                
                expId++;
            }
            
            logger.info("Now sending all execution requests to the executor....");
            trigger(new ResponseStatus(source, event,
                    ResponseStatus.Status.SUCCESS,
                    "Setting loaded. Now sending all execution requests to the executor."),
                    portStatus);
            
            startTime = System.currentTimeMillis();
        }
    };
    
    private Handler<RequestAbortion> handleAbort = new Handler<RequestAbortion>() {
        public void handle(RequestAbortion event) {
            
            logger.info("Request the ExecutorService to shutdown.");
            //stop all calculating threads
            es.shutdownNow();
            
            
            // Sending ABORT request the CloudExecutionManager
            trigger(new AbortExecution(expId), portExec);
        }
    };
    
    private Handler<ResponseExecution> handleResponse = new Handler<ResponseExecution>() {
        public void handle(ResponseExecution event) {
            // update the experiment result
            
            System.out.print(".");
            
            int cExpId = event.getExpId();
            int cRepId = event.getRepId();
            
            logger.debug("response received. expId={}, repId={}", cExpId,
                    cRepId);
            ExperimentPoint p = designPoints.get(cExpId);
            
            try {
                p.getStatistics().addValue(
                        Double.parseDouble(event.getResult()));
            } catch (Exception e) {
                logger.info("error = {}", event.getResult());
                
                e.printStackTrace();
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.IO_ERROR,
                        "Invalid experiment result received."), portStatus);
                
                // re-send the request.
                requestExpRun(p);
                
                return;
            }
            
            p.increaseCompleted();
            
            if (nFixedRun > 0) {
                
                // fixed-run mode
                if (p.getnCompleted() < nFixedRun) {
                    requestExpRun(p);
                    p.increaseTotal();
                } else {
                    p.setReady(true);
                    // get an unfinished exp and add a new run.
                    for (ExperimentPoint q : designPoints.values()) {
                        if (q.getnTotal() < nFixedRun) {
                            requestExpRun(q);
                            q.increaseTotal();
                            break;
                        }
                    }
                }
                
            } else {
                // confidence mode
                if (p.getStatistics().size() <= 1) {
                    requestExpRun(p);
                    p.increaseTotal();
                    return;
                }
                
                double achievedLength = p.getStatistics().getWidth();
                
                if (achievedLength < confidenceLength) {
                    logger.debug("length achieved={}", achievedLength);
                    p.setReady(true);
                    // get an unfinished exp and add a new run.
                    for (ExperimentPoint q : designPoints.values()) {
                        if (!q.isReady()) {
                            requestExpRun(q);
                            q.increaseTotal();
                            break;
                        }
                    }
                } else {
                    requestExpRun(p);
                    p.increaseTotal();
                }
            }
            
            printExpStat();
            
            if (p.getnCompleted() == p.getnTotal()) {
                System.out.print("!");
                // check if the exp is ready for SK
                boolean isReady = true;
                for (ExperimentPoint x : designPoints.values()) {
                    if (!x.isReady() || (x.getnCompleted() != x.getnTotal()))
                        isReady = false;
                }
                
                if (isReady) {
                    
                    // print the exp execution summary.
                    printExecStatus();
                    
                    logger.info("All execution done. Now ready for Stochastic Kriging.");
                    logger.info("=====================================================");
                    trigger(new NotifyExecCompleted(), portApp);
                }
            }
        }
    };
    
    private void printExpStat() {
        int sumTotal = 0;
        int sumCompleted = 0;
        for (Integer id : designPoints.keySet()) {
            sumTotal += designPoints.get(id).getnTotal();
            sumCompleted += designPoints.get(id).getnCompleted();
        }
        logger.debug("Total={}, Completed={}", sumTotal, sumCompleted);
    }
    
    private Handler<RequestSK> handleSKProcceed = new Handler<RequestSK>() {
        public void handle(RequestSK event) {
            
            krg.reset(event.getSettings());
            try {
                krg.load();
            } catch (MWException e) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.SK_LOAD_FAIL, e.getMessage()),
                        portStatus);
            }
            
            // print the SK settings and input parameters.
            logger.info("===========================================");
            logger.info("Stochastic Kriging now start with the following setting:");
            event.getSettings().printSettings(logger);
            trigger(new ResponseStatus(source, event,
                    ResponseStatus.Status.SUCCESS,
                    "Stochastic Kriging setting loaded. Now start running."),
                    portStatus);
            
            // formulate the parameters for SK solver
            double[][] design = new double[designPoints.size()][];
            double[] values = new double[designPoints.size()];
            double[] stdvs = new double[designPoints.size()];
            int i = 0;
            for (ExperimentPoint x : designPoints.values()) {
                design[i] = x.getDesignPoint();
                values[i] = x.getStatistics().mean();
                stdvs[i] = x.getStatistics().kVariance();
                i++;
            }
            
            // run SK on the current exp result
            try {
                model = krg.SKfit(design, values, stdvs);
            } catch (MWException e) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.SK_RUN_FAIL, e.getMessage()),
                        portStatus);
            }
            
            logger.info("Ready for Simulated Annealing.");
            logger.info("=============================================");
            
            trigger(new NotifySKCompleted(), portApp);
        }
    };
    
    private Handler<RequestGraphDrawing> handleGraph = new Handler<RequestGraphDrawing>() {
        
        @Override
        public void handle(RequestGraphDrawing event) {
            
            if (min.length > 2) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.VISUALSATION_FAILED,
                        "Having more than 2 dimensions. Unable to visualize."),
                        portStatus);
                return;
            }
            double[][] design = new double[designPoints.size()][];
            double[] values = new double[designPoints.size()];
            
            int i = 0;
            for (ExperimentPoint x : designPoints.values()) {
                design[i] = x.getDesignPoint();
                values[i] = x.getStatistics().mean();
                i++;
            }
            // draw the graph
            logger.info("Now drawing...");
            try {
                krg.visualize(design, values, max, min);
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.SUCCESS, "Drawing graph."),
                        portStatus);
            } catch (MWException e) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.VISUALSATION_FAILED,
                        e.getMessage()), portStatus);
            } catch (IOException e) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.IO_ERROR, e.getMessage()),
                        portStatus);
            }
        }
    };
    
    private Handler<RequestMSEMinimization> handleMSEMinimize = new Handler<RequestMSEMinimization>() {
        public void handle(final RequestMSEMinimization event) {
            
            // print info about the Simulated Annealing proccess and
            // input parameters.
            logger.info("============================================================");
            logger.info("Simulated Annealing now run with the following settings: ");
            event.getSettings().display(logger);
            
            anl.reset(event.getSettings());
            try {
                anl.load();
            } catch (MWException e) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.SA_LOAD_FAIL, e.getMessage()),
                        portStatus);
            }
            
            trigger(new ResponseStatus(source, event,
                    ResponseStatus.Status.SUCCESS,
                    "Simulated Annealing setting loaded. Now start running."),
                    portStatus);
            
            // try {
            // newPoints = anl.mseMin(model, max, min, mseThreshold);
            // } catch (MWException e) {
            // trigger(new ResponseStatus(source, event,
            // ResponseStatus.Status.SA_RUN_FAIL, e.getMessage()),
            // portStatus);
            // }
            final HashSet<Future<ExpPoint>> results = new HashSet<Future<ExpPoint>>();
            for (int[] lbVector : coeffMatrix) {
                double[] lb = new double[min.length];
                double[] ub = new double[min.length];
                for (int i = 0; i < min.length; i++) {
                    lb[i] = min[i] + lbVector[i] * step[i];
                    ub[i] = lb[i] + step[i];
                }
                results.add(getExecutorService().submit(new PartitionThread(lb, ub)));
            }
            
            Runnable waitForResult = new Runnable() {
                
                @Override
                public void run() {
                    int count = 0;
                    double[][] temp = new double[4][min.length];
                    for (Future<ExpPoint> fs : results) {
                        try {
                            ExpPoint p = fs.get();
                            if (Math.abs(p.getfMin()) > mseThreshold) {
                                temp[count] = p.getoPoint();
                                count++;
                            }
                        } catch (InterruptedException e) {
                            logger.debug("Calculation aborted.");
                            return;
                        } catch (ExecutionException e) {
                            logger.error("Failed to retrieve result.");
                        }
                    }
                    
                    logger.debug("number of new points: {}", count);
                    
                    if (count == 0) {
                        logger.info("MSE surface minimized.");
                        
                        trigger(new NotifyMinimizationCompleted(), portApp);
                        
                        trigger(new ResponseStatus(source, event,
                                ResponseStatus.Status.SUCCESS, "MSE surface minimized."),
                                portStatus);
                        
                    } else {
                        
                        newPoints = new double[count][min.length];
                        for (int i = 0; i < count; i++) {
                            newPoints[i] = temp[i];
                        }
                        
                        trigger(new NotifyMinimizationStepCompleted(), portApp);
                        
                        trigger(new ResponseStatus(source, event,
                                ResponseStatus.Status.SUCCESS,
                                "New sample points located. Number = "
                                        + newPoints.length), portStatus);
                    }
                    
                }
            };
            
            getExecutorService().submit(waitForResult);
            
            logger.debug("End submitting tasks.");

        }
    };
    
    private Handler<RequestMinimizationContinue> handleMinimizationContinue = new Handler<RequestMinimizationContinue>() {
        public void handle(RequestMinimizationContinue event) {
            // add the new Design Points to list and procced to request for
            // more executions.
            logger.info("Adding new points:");
            for (double[] dataPoint : newPoints) {
                logger.info(Arrays.toString(dataPoint));
                ExperimentPoint newPoint = new ExperimentPoint(expId,
                        initRepId, confidenceLevel);
                newPoint.setDesignPoint(dataPoint);
                logger.debug("New experiment point:{}",
                        Arrays.toString(newPoint.getDesignPoint()));
                
                int nRuns = Math.round((float) nEffCores / newPoints.length);
                for (int count = 0; count < nRuns; count++) {
                    // send the experiment request.
                    newPoint.increaseTotal();
                    requestExpRun(newPoint);
                }
                
                // add the point to the list of design points
                designPoints.put(expId, newPoint);
                
                expId++;
            }
            
            trigger(new ResponseStatus(source, event,
                    ResponseStatus.Status.SUCCESS, "Added " + newPoints.length
                            + " new points. Now request for more executions."),
                    portStatus);
            logger.info("Now sending new execution requests to the executor....");
        }
    };
    
    private Handler<RequestOptimal> handleRequestResult = new Handler<RequestOptimal>() {
        public void handle(RequestOptimal event) {
            logger.info("Now looking for the optimal point...");
            trigger(new ResponseStatus(source, event,
                    ResponseStatus.Status.SUCCESS,
                    "Now looking for the optimal point"), portStatus);
            try {
                trigger(new NotifyExpResult(anl.getOptimal(model, max, min)),
                        portApp);
            } catch (MWException e) {
                trigger(new ResponseStatus(source, event,
                        ResponseStatus.Status.SA_RUN_FAIL, e.getMessage()),
                        portStatus);
            }
        }
    };
    
    private void requestExpRun(ExperimentPoint p) {
        int expId = p.getExpId();
        int repId = p.getRepId();
        String command = baseCommand;
        command += (" " + Integer.toString(seed++));
        for (double b : p.getDesignPoint())
            command += (" " + Double.toString(b));
        trigger(new RequestExecution(expId, repId, command), portExec);
        
        logger.debug("new exec request sent. expId={}, repId={}", expId, repId);
        p.increaseRepId();
    }
    
    private void generateCoeffMatrix(int nDim) {
        if (nDim == 0) {
            int[] temp = new int[min.length];
            for (int i = 0; i < min.length; i++) {
                temp[i] = coeffVector[i];
            }
            coeffMatrix.add(temp);
            // System.out.println(Arrays.toString(coeffVector));
            return;
        }
        
        for (int i = 0; i < N_SEP; i++) {
            coeffVector[nDim - 1] = i;
            generateCoeffMatrix(nDim - 1);
        }
    }
    
    private void printExecStatus() {
        int sum = 0;
        for (ExperimentPoint p : designPoints.values()) {
            sum += p.getStatistics().size();
        }
        logger.info("Number of design points: {}", designPoints.keySet().size());
        System.out.println();
        logger.info("Number of executions: {}", sum);
        
        String msg = "Number of design points: "
                + Integer.toString(designPoints.keySet().size())
                + "\tNumber of executions: "
                + Integer.toString(sum)
                + "\tTotal exec time: "
                + Long.toString((System.currentTimeMillis() - startTime) / 1000)
                + " secs";
        trigger(new ResponseStatus(source, null, ResponseStatus.Status.SUCCESS,
                msg), portStatus);
        logger.debug("exec started at {}", startTime);
        logger.debug("exec ended at {}", System.currentTimeMillis());
    }
    
    private void printExpStatus() {
        logger.info("Experiment started with the following parameters:");
        logger.info("=================================================");
        logger.info("Experiment jarfile: {}", baseCommand);
        logger.info("Number of initial design points: {}", num);
        for (int i = 0; i < min.length; i++) {
            logger.info("Parameter range: [{} {}]", min[i], max[i]);
        }
        logger.info("Confidence level: {}", confidenceLevel);
        logger.info("Minimum confidence length: {}", confidenceLength);
    }
    
    class PartitionThread implements Callable<ExpPoint> {
        double[] lb;
        double[] ub;
        
        public PartitionThread(double[] min, double[] max) {
            this.lb = min;
            this.ub = max;
        }
        
        public ExpPoint call() {
            ExpPoint result = null;
            logger.debug("Start searching for the peak MSE point");
            try {
                result = anl.getPartitionMSE(model, ub, lb);
            } catch (MWException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return result;
        }
    }
    
    public static ExecutorService getExecutorService() {
        if (es.isShutdown()) {
            es = Executors.newCachedThreadPool();
        }
        return es; 
    }
}