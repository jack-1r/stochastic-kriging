package se.sics.stochastic.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkExecutionThread implements Runnable {
    
    private final Logger logger = LoggerFactory
            .getLogger(NetworkExecutionThread.class);
    
    int expTag;
    String cmd;
    String result;
    NetworkExecutorDaemon exec;
    
    public NetworkExecutionThread(int expTag, String cmd,
            NetworkExecutorDaemon exec) {
        logger.debug("thread spawned");
        this.expTag = expTag;
        this.cmd = cmd;
        this.exec = exec;
    }
    
    public void run() {
        logger.debug("exp started. expTag={}", expTag);
        String[] command = cmd.split(" ");
        Process p = null;
        try {
            logger.debug(System.getProperty("user.dir"));
            logger.debug(System.getProperty("java.class.path"));
            logger.debug("command={}", cmd);
            p = new ProcessBuilder(command).start();
            InputStream stderr = p.getErrorStream();
            InputStream stdout = p.getInputStream();
            
            // Exec stdoutGobbler = new Exec(stdout, "STDOUT");
            Exec errorGobbler = new Exec(stderr, "ERROR");
            // stdoutGobbler.start();
            errorGobbler.start();
            logger.debug("forked process p.");
            int eVal = 0;
            
            eVal = p.waitFor();
            logger.debug("exitValue={}", eVal);
            
            result = new BufferedReader(new InputStreamReader(stdout))
                    .readLine();
            exec.triggerResult(expTag, result);
            
        } catch (InterruptedException e) {
            logger.debug("Thread interupted. Destroying the sub-process...");
            p.destroy();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } finally {
            try {
                p.getInputStream().close();
                p.getErrorStream().close();
                p.getOutputStream().close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
    }
}
