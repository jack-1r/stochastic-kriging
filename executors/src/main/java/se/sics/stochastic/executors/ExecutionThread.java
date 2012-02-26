package se.sics.stochastic.executors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionThread implements Runnable {
    
    private final Logger logger = LoggerFactory
            .getLogger(ExecutionThread.class);
    
    int expId;
    int repId;
    String cmd;
    String result;
    SimpleExperimentExecutor exec;
    
    public ExecutionThread(int expId, int repId, String cmd,
            SimpleExperimentExecutor exec) {
        logger.debug("thread spawned");
        this.expId = expId;
        this.repId = repId;
        this.cmd = cmd;
        this.exec = exec;
    }
    
    public void run() {
		logger.debug("exp started. expId={} repId={}", expId, repId);
		String[] command = cmd.split(" ");
		Process p = null;
		try {
			logger.debug(System.getProperty("user.dir"));
			logger.debug(System.getProperty("java.class.path"));
			logger.debug("command={}", cmd);
			p = new ProcessBuilder(command).start();
			InputStream stderr = p.getErrorStream();
			InputStream stdout = p.getInputStream();

//			Exec stdoutGobbler = new Exec(stdout, "STDOUT");
			Exec errorGobbler = new Exec(stderr, "ERROR");
//			stdoutGobbler.start();
			errorGobbler.start();
			logger.debug("forked process p.");
			int eVal=0;
			try {
				eVal=p.waitFor();
				logger.debug("exitValue={}", eVal);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			try {
			    BufferedReader bf = new BufferedReader(new InputStreamReader(stdout));
				result = bf.readLine();
				String next = "";
				while ((next = bf.readLine()) != null) {
				    result = next;
				}
				exec.triggerResult(expId, repId, result);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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

class Exec extends Thread {
    InputStream is;
    String type;
    OutputStream os;
    
    Exec(InputStream is, String type) {
        this(is, type, null);
    }
    
    Exec(InputStream is, String type, OutputStream redirect) {
        this.is = is;
        this.type = type;
        this.os = redirect;
    }
    
    public void run() {
        try {
            PrintWriter pw = null;
            if (os != null) {
                pw = new PrintWriter(os);
            }
            
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            
            String line = null;
            while ((line = br.readLine()) != null) {
                if (pw != null) {
                    pw.println(line);
                    pw.flush();
                }
                
                System.out.println(type + ">" + line);
            }
            
            if (pw != null) {
                pw.flush();
            }
        } catch (IOException ioe) { /* Forward to handler */
        }
    }
}
