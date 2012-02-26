package se.sics.stochastic.executors;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;
import se.sics.stochastic.executors.gui.NetExecutorListGUI;

public class CloudExecutionManager extends ComponentDefinition {
    private final Logger logger = LoggerFactory
            .getLogger(CloudExecutionManager.class);
    
    Negative<PortExecutors> execPort = provides(PortExecutors.class);
    
    ServerSocket sk = null;
    Random r = new Random();
    public static int mCount = 0;
    
    private HashMap<Integer, NetExecutorInstance> execList; // list of available
                                                            // executors
    private HashMap<Integer, ExperimentTag> expList; // list of the experiments
                                                     // in processing
    private NetExecutorListGUI frmExecutorList = null; // the GUI that list all
                                                       // executors
    
    public CloudExecutionManager() {
        subscribe(handleInit, control);
        subscribe(handleRequest, execPort);
        subscribe(handleStart, control);
        subscribe(handleAbort, execPort);
    }
    
    private Handler<AbortExecution> handleAbort = new Handler<AbortExecution>() {
        public void handle(AbortExecution event) {
            abortExec(event.getExpId());
        }
    };
    
    private Handler<InitExecutors> handleInit = new Handler<InitExecutors>() {
        public void handle(InitExecutors event) {
            // logger.debug("initiated.");
            execList = new HashMap<Integer, NetExecutorInstance>();
            expList = new HashMap<Integer, ExperimentTag>();
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    frmExecutorList = new NetExecutorListGUI();
                    frmExecutorList.pack();
                    frmExecutorList.setVisible(true);
                }
            });
        }
    };
    
    private Handler<Start> handleStart = new Handler<Start>() {
        public void handle(Start event) {
            // start the thread to open the socket and listen for
            // incoming message.
            if (sk == null)
                if (startListening() == -1)
                    logger.info("Can not start the network listening thread.");
                else
                    logger.info("Network listening thread started");
        }
    };
    
    private Handler<RequestExecution> handleRequest = new Handler<RequestExecution>() {
        public void handle(RequestExecution event) {
            logger.debug("got request.");
            // Put the experiment to the queue and call the scheduler to
            // get the suitable executor
            int expTag = r.nextInt();
            String cmd = event.getCommand();
            
            // - get the available executor
            NetExecutorInstance exec = getAvailableExec();
            if (exec == null) {
                logger.info("No avaiable executor.");
                return;
            }
            
            // - send the request msg (and probably wait for ACKed)
            if (requestExecution(exec, expTag, cmd) == 0)
                logger.debug("Request for execution sent.");
            else
                logger.debug("Unable to send exp request.");
            
            // - (set the timer for the exp)
            
            // - put the exp to the expList
            expList.put(
                    expTag,
                    new ExperimentTag(event.getExpId(), event.getRepId(), event
                            .getCommand(), exec));
        }
    };
    
    private int abortExec(int expId) {
        // send ABORT message to all clients
        logger.debug("Sending abort message to all clients.");
        
        HashMap<Integer, NetExecutorInstance> enabledList = new HashMap<Integer, NetExecutorInstance>();
        
        for (int i : execList.keySet()) {
            if (frmExecutorList.isEnabled(i)) {
                enabledList.put(i, execList.get(i));
            }
        }
        
        for (NetExecutorInstance d : enabledList.values()) {
            String sndData = "ABORT\t" + Integer.toString(expId) + "\n";
            try {
                new DataOutputStream(d.getClientSK().getOutputStream())
                        .writeBytes(sndData);
                logger.debug("[Sent]: msg={}", sndData);
            } catch (IOException e) {
                logger.error("Failed to send ABORT signal to client at : {}", d
                        .getClientSK().getRemoteSocketAddress().toString());
            }
        }
        
        return 0;
    }
    
    private int startListening() {
        logger.info("Initializing...");
        
        try {
            sk = new ServerSocket(6969);
            logger.debug("Open TCP server socket at {}", sk
                    .getLocalSocketAddress().toString());
        } catch (IOException e) {
            logger.info("Can not open new TCP server socket");
            e.printStackTrace();
            return (-1);
        }
        
        // spawn a thread to listen for incomming connection.
        new Thread(new Runnable() {
            
            @Override
            public void run() {
                while (true) {
                    try {
                        final Socket newClientSK = sk.accept();
                        logger.debug("[Connection requested]:from={}",
                                newClientSK.getRemoteSocketAddress());
                        logger.debug("Sending buffer sz: {}",
                                newClientSK.getSendBufferSize());
                        
                        // spawn new thread to deal with new Connection
                        new Thread(new Runnable() {
                            
                            BufferedReader rcvBuf = null;
                            
                            @Override
                            public void run() {
                                try {
                                    rcvBuf = new BufferedReader(
                                            new InputStreamReader(newClientSK
                                                    .getInputStream(), "UTF-8"));
                                } catch (IOException e1) {
                                    e1.printStackTrace();
                                    return;
                                }
                                while (true) {
                                    // - listen to msg from the client.
                                    String rcvData = null;
                                    try {
                                        rcvData = rcvBuf.readLine();
                                        logger.debug(
                                                "[Received]: from= {}, msg={}",
                                                newClientSK
                                                        .getRemoteSocketAddress(),
                                                rcvData);
                                    } catch (IOException e) {
                                        logger.info(
                                                "Unable to listen for msg from {}",
                                                newClientSK
                                                        .getRemoteSocketAddress());
                                        e.printStackTrace();
                                        return;
                                    }
                                    
                                    String[] data = rcvData.split("\t");
                                    if ("REGISTER".equals(data[0])) {
                                        registerNewDaemon(newClientSK,
                                                Integer.parseInt(data[1]),
                                                r.nextInt());
                                    } else if ("RESPONSE".equals(data[0])) {
                                        processResult(
                                                Integer.parseInt(data[1]),
                                                data[2]);
                                        mCount++;
                                        logger.debug("Triggered response: {}",
                                                mCount);
                                    } else {
                                        logger.debug("Bogus msg received.");
                                    }
                                }
                            }
                        }).start();
                        
                    } catch (IOException e) {
                        logger.info("Unable to listen on {}",
                                sk.getLocalSocketAddress());
                        e.printStackTrace();
                        return;
                    }
                    
                }
                
            }
        }).start();        
        return 0;
    }
    
    private synchronized int requestExecution(NetExecutorInstance exec,
            int expTag, String cmd) {
        String sndData = "REQUEST\t" + Integer.toString(expTag) + "\t" + cmd
                + "\n";
        try {
            new DataOutputStream(exec.getClientSK().getOutputStream())
                    .writeBytes(sndData);
            logger.debug("[Sent]: msg={}", sndData);
        } catch (IOException e) {
            e.printStackTrace();
        }
        exec.increaseCount();
        return 0;
    }
    
    private NetExecutorInstance getAvailableExec() {
        NetExecutorInstance exec = null;
        HashMap<Integer, NetExecutorInstance> enabledList = new HashMap<Integer, NetExecutorInstance>();
        
        for (int i : execList.keySet()) {
            if (frmExecutorList.isEnabled(i)) {
                enabledList.put(i, execList.get(i));
            }
        }
        
        int count = Integer.MAX_VALUE;
        for (int i : enabledList.keySet()) {
            if (enabledList.get(i).getExpCount() < count) {
                exec = enabledList.get(i);
                count = enabledList.get(i).getExpCount();
            }
        }
        return exec; /* return the executor's ID */
    }
    
    private void registerNewDaemon(Socket clientSocket, int nCores, int id) {
        
        // check and add the new entry to the executor list
        NetExecutorInstance newExec = new NetExecutorInstance(clientSocket,
                nCores);
        execList.put(id, newExec);
        logger.info("Daemon registered: {}",
                clientSocket.getRemoteSocketAddress());
        
        // update the GUI
        frmExecutorList.addExec(id, newExec);
        frmExecutorList.invalidate();        
    }
    
    private synchronized void processResult(int expTag, String result) {
        logger.debug("Result received. tag={}", expTag);
        
        ExperimentTag exp = expList.get(expTag);
        
        // - Send the result back to ExpMgr component
        trigger(new ResponseExecution(exp.getExpId(), exp.getRepId(), result),
                execPort);
        
        // - Decrease the exec's counter
        exp.getExec().decreaseCount();
        
        // - Remove the tag from expList
        expList.remove(expTag);
    }
}

class ExperimentTag {
    
    private int expId;
    private int repId;
    private String cmd;
    private NetExecutorInstance exec;
    
    public ExperimentTag(int expId, int repId, String cmd,
            NetExecutorInstance exec) {
        this.exec = exec;
        this.expId = expId;
        this.repId = repId;
        this.cmd = cmd;
    }
    
    public int getExpId() {
        return expId;
    }
    
    public int getRepId() {
        return repId;
    }
    
    public String getCmd() {
        return cmd;
    }
    
    public NetExecutorInstance getExec() {
        return exec;
    }
}
