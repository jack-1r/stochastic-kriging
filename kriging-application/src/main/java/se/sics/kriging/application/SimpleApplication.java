package se.sics.kriging.application;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Init;
import se.sics.kompics.Positive;
import se.sics.kompics.Start;
import se.sics.kriging.application.gui.SimpleKrigingFrame;
import se.sics.kriging.components.NotifyAbortComplete;
import se.sics.kriging.components.NotifyExecCompleted;
import se.sics.kriging.components.NotifyExpResult;
import se.sics.kriging.components.NotifyMinimizationCompleted;
import se.sics.kriging.components.NotifyMinimizationStepCompleted;
import se.sics.kriging.components.NotifySKCompleted;
import se.sics.kriging.components.PortApplication;
import se.sics.kriging.components.PortStatus;
import se.sics.kriging.components.RequestAbortion;
import se.sics.kriging.components.RequestExpStart;
import se.sics.kriging.components.RequestGraphDrawing;
import se.sics.kriging.components.RequestMSEMinimization;
import se.sics.kriging.components.RequestMinimizationContinue;
import se.sics.kriging.components.RequestOptimal;
import se.sics.kriging.components.RequestSK;
import se.sics.kriging.components.ResponseStatus;
import se.sics.stochastic.kriging.SASettings;
import se.sics.stochastic.kriging.SKSettings;

public class SimpleApplication extends ComponentDefinition {
    
    Logger logger = LoggerFactory.getLogger(SimpleApplication.class);
    Positive<PortApplication> portApp = positive(PortApplication.class);
    Positive<PortStatus> portStatus = positive(PortStatus.class);
    SimpleApplication comp;
    SimpleKrigingFrame frmKr;
    boolean isStopped = true;
    boolean isSAStep = false;
    
    public SimpleApplication() {
        subscribe(handleInit, control);
        subscribe(handleStart, control);
        subscribe(handleExecCompleted, portApp);
        subscribe(handleSKCompleted, portApp);
        subscribe(handleContinue, portApp);
        subscribe(handleMinimizationComplete, portApp);
        subscribe(handleResult, portApp);
        subscribe(handleStatus, portStatus);
        subscribe(handleAbortComplete, portApp);
        comp = this;
    }
    
    public boolean isSAStep() {
        return isSAStep;
    }
    
    public boolean isStopped() {
        return isStopped;
    }
    
    private Handler<Init> handleInit = new Handler<Init>() {
        
        public void handle(Init event) {
            logger.debug("initiated.");
            java.awt.EventQueue.invokeLater(new Runnable() {
                
                public void run() {
                    frmKr = new SimpleKrigingFrame(comp);
                    frmKr.pack();
                    frmKr.setVisible(true);
                }
            });
        }
    };
    
    private Handler<Start> handleStart = new Handler<Start>() {
        public void handle(Start event) {
            logger.debug("started.");
        }
    };
    
    private Handler<ResponseStatus> handleStatus = new Handler<ResponseStatus>() {
        public void handle(ResponseStatus event) {
            if (frmKr.isRunning() == false) {
                return;
            }
            // print out the response status
            System.out.println("[" + System.currentTimeMillis() + "] - "
                    + event.getSource());
            System.out.println(event.getStatus() + ": " + event.getMsg());
            // System.out.println(event.getRequest().toString());
            System.out.println();
        }
    };
    
    private Handler<NotifyExecCompleted> handleExecCompleted = new Handler<NotifyExecCompleted>() {
        
        public void handle(NotifyExecCompleted event) {
            // check run-mode and prompt user for input if necessary.
            isStopped = true;
            if (frmKr.isRunning() == false) {
                System.out.println("USER INTERRUPTION. ABORTED.");
                return;
            }
            
            if (!frmKr.getRunMode()) {
                runSK();
            } else {
                frmKr.continueSKEnable(true);
            }
        }
    };
    
    private Handler<NotifySKCompleted> handleSKCompleted = new Handler<NotifySKCompleted>() {
        
        public void handle(NotifySKCompleted event) {
            frmKr.getResultEnable(true);
            isSAStep = false;
            isStopped = true;
            if (frmKr.isRunning() == false) {
                frmKr.finishAbort();
                return;
            }
            
            if (!frmKr.getRunMode()) {
                runSA();
            } else {
//                trigger(new RequestGraphDrawing(), portApp);
                frmKr.continueSKEnable(true);
                frmKr.continueSAEnable(true);
            }
        }
    };
    
    private Handler<NotifyMinimizationStepCompleted> handleContinue = new Handler<NotifyMinimizationStepCompleted>() {
        public void handle(NotifyMinimizationStepCompleted event) {
            isSAStep = true;
            isStopped = true;
            if (frmKr.isRunning() == false) {
                frmKr.finishAbort();
                return;
            }
            
            if (!frmKr.getRunMode()) {
                trigger(new RequestMinimizationContinue(), portApp);
                isStopped = false;
            } else {
                frmKr.continueSAEnable(true);
            }
        }
    };
    
    private Handler<NotifyMinimizationCompleted> handleMinimizationComplete = new Handler<NotifyMinimizationCompleted>() {
        
        public void handle(NotifyMinimizationCompleted event) {
            
            if (frmKr.isRunning() == false) {
                frmKr.finishAbort();
                return;
            }
            
            trigger(new RequestOptimal(), portApp);
            frmKr.reset();
        }
    };
    
    private Handler<NotifyExpResult> handleResult = new Handler<NotifyExpResult>() {
        public void handle(NotifyExpResult event) {
            if (frmKr.isRunning() == false) {
                frmKr.finishAbort();
                return;
            }
            
            isStopped = true;
            System.out.println("Experiment end.");
            System.out.println("Optimal point: "
                    + Arrays.toString(event.getOptimal().getoPoint()));
            System.out.println("Value: "
                    + Double.toString(event.getOptimal().getfMin()));
        }
    };
    
//    private void end() {
//        logger.debug("Shutting down...");
//        KrigingApplication.semaphore.release();
//    }
    
    public void startExperimentfromGUI() {
        System.out.println("Loading setting...");
        final RequestExpStart ExpStart = new RequestExpStart();
        ExpStart.setConfidence(frmKr.getConfidenceInterval()); // 95%
        ExpStart.setLb(frmKr.getMin());
        ExpStart.setUb(frmKr.getMax());
        ExpStart.setIntVector(frmKr.getIntVector());
        ExpStart.setNum(frmKr.getNrInitSamples());
        ExpStart.setRatio(frmKr.getConfidenceLength());
        ExpStart.setThreshold(frmKr.getMSEthreshold());
        ExpStart.setnRuns(frmKr.getnRuns());
        ExpStart.setFixedRun(frmKr.isFixedRunMode());
        ExpStart.setnFixedRun(frmKr.getnFixedRun());

        // serialize the ExpStart object to a file
        logger.debug("Saving the session data to file...");
        ExpStart.setJarPath(frmKr.getJarFullPath());
        SessionDataBundle data = new SessionDataBundle(ExpStart,
                Integer.toString(frmKr.getDimCount()), Integer.toString(frmKr
                        .getMaxEval()), frmKr.getSKAlgor());
        try {
            ObjectOutput os = new ObjectOutputStream(new FileOutputStream(
                    "session.data"));
            os.writeObject(data);
            os.flush();
            os.close();
        } catch (FileNotFoundException e) {
            logger.error("Unable to find the output file");
        } catch (IOException e) {
            logger.error("Unable to save the session. The input data will be lost upon restart the application");
        }
        
        ExpStart.setJarPath(frmKr.getJar());
        System.out.println("Params input done. Starting the experiment.......");
        System.out.println();
        trigger(ExpStart, portApp);
        isStopped = false;
    }
    
    Handler<NotifyAbortComplete> handleAbortComplete = new Handler<NotifyAbortComplete>() {
        @Override
        public void handle(NotifyAbortComplete event) {
            frmKr.finishAbort();
        }
    };
    
    public void runSK() {
        trigger(new RequestSK(new SKSettings(2, frmKr.getSKAlgor(), 1000000)),
                portApp);
        isStopped = false;
    }
    
    public void runSA() {
        logger.debug("Start sending the SA request.");
        trigger(new RequestMSEMinimization(new SASettings(frmKr.getMaxEval())),
                portApp);
        isStopped = false;
        logger.debug("Finish sending the report.");
    }
    
    public void iterateSA() {
        logger.debug("Start sending the SAContinue request.");
        trigger(new RequestMinimizationContinue(new SASettings(
                frmKr.getMaxEval())), portApp);
        isStopped = false;
        logger.debug("Finish sending the SAContinue report.");
    }
    
    public void getResult() {
        logger.debug("Start sending the getResult request.");
        trigger(new RequestOptimal(), portApp);
        trigger(new RequestGraphDrawing(), portApp);
        isStopped = false;
        logger.debug("Finish sending the getResult request.");
    }
    
    public void abort() {
        if (isStopped)
            frmKr.finishAbort();
        else {
            logger.debug("Sending ABORT request to the manager...");
            trigger(new RequestAbortion(), portApp);
        }
    }
}
