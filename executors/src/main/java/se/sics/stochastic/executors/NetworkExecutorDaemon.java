package se.sics.stochastic.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkExecutorDaemon {
    
    static Logger logger = LoggerFactory.getLogger(NetworkExecutorDaemon.class);
    
    private static ExecutorService es = Executors.newCachedThreadPool();
    
    private String serverAddress;
    private int serverPort;
    
    private Socket sk;
    private BufferedReader rcvBuf;
    private OutputStreamWriter out = null;;
    
    public int eCount = 0; // count the number of response sent - debug purpose
                           // only
    public int rCount = 0; // count the number of request received - debug
                           // purpose only
    
    public NetworkExecutorDaemon(String serverAddr, int serverPort) {
        this.serverPort = serverPort;
        this.serverAddress = serverAddr;
        
    }
    
    public static void main(String[] args) {
        NetworkExecutorDaemon comp = new NetworkExecutorDaemon(args[0],
                Integer.parseInt(args[1]));
        
        if (comp.initialize() == -1) {
            logger.info("Failed to initialize the daemon. Now quit.");
            System.exit(-1);
        }
        
        while (true) {
            int returnValue = 0;
            
            try {
                returnValue = comp.startReceive();
            } catch (Exception e) {
                logger.error("Server connection failed. System will exit.", e);
                System.exit(-1);
            }
            
            if (returnValue == (-1)) {
                logger.info("System error.");
                System.exit(-1);
            } else if (returnValue == 1) {
                // if receive funny msg then notice the user and continue
                // listening
                logger.info("Bogus msg received.");
            } else
                logger.info("Request received.");
        }
    }
    
    public int initialize() {
        
        logger.info("Initializing...");
        
        try {
            sk = new Socket();
            // sk.setReceiveBufferSize(INPUT_BUFFER_SZ);
            sk.connect(new InetSocketAddress(InetAddress
                    .getByName(serverAddress), serverPort));
            rcvBuf = new BufferedReader(new InputStreamReader(
                    sk.getInputStream()));
            out = new OutputStreamWriter(sk.getOutputStream(), "UTF-8");
            
            logger.debug("Open new TCP socket at {} and connect to {}",
                    sk.getLocalSocketAddress(), sk.getRemoteSocketAddress());
            logger.debug("Receive buffer size: {}", sk.getReceiveBufferSize());
            
        } catch (Exception e) {
            e.printStackTrace();
            return (-1);
        }
        
        int nCores = Runtime.getRuntime().availableProcessors();
        String sndData = "REGISTER\t" + Integer.toString(nCores) + "\n";
        if (sndMsg(sndData) != 0)
            logger.info("Failed to send the registration msg.");
        
        logger.info(
                "Initialization done. Registered with server at {} port {}",
                serverAddress, serverPort);
        
        return 0;
    }
    
    public int startReceive() {
        String rcvData = null;
        int ret = 0;
        try {
            rcvData = rcvBuf.readLine();
            logger.debug("[Received]: {}", rcvData);
        } catch (IOException e) {
            e.printStackTrace();
            ret = -1;
        }
        
        String[] data = new String(rcvData).split("\t");
        
        // Check the received data to validate the REQUEST message.
        if (data[0].equals("REQUEST")) {
            rCount++;
            logger.info("Request get here so far: {}", rCount);
            
            // start the new thread to handle the received pkt
            NetworkExecutionThread exec = new NetworkExecutionThread(
                    Integer.parseInt(data[1]), data[2], this);
            getExecutorService().submit(exec);
            ret = 0;
        } else if (data[0].equals("ABORT")) {
            // Check the received data to handle ABORT message.
            abort();
            ret = 0;
        } else {
            ret = 1;
        }
        return ret;
    }
    
    private void abort() {
        es.shutdownNow();
    }
    
    public void triggerResult(int expTag, String result) {
        eCount++;
        logger.info("Response so far: {}", eCount);
        
        // send the result back to the server
        String sndData = "RESPONSE\t" + Integer.toString(expTag) + "\t"
                + result + "\n";
        if (sndMsg(sndData) != 0)
            logger.info("Failed to send the response.");
    }
    
    private int sndMsg(String msg) {
        int eVal = 0;
        try {
            out.write(msg);
            out.flush();
            logger.debug("[Sent]: {}", msg);
        } catch (IOException e) {
            e.printStackTrace();
            return (-1);
        }
        return eVal;
    }
    
    public static ExecutorService getExecutorService() {
        if (es.isShutdown()) {
            es = Executors.newCachedThreadPool();
        }
        return es;    
    }
}
