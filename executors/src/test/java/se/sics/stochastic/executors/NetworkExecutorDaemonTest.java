package se.sics.stochastic.executors;

import junit.framework.TestCase;

public class NetworkExecutorDaemonTest extends TestCase{
    
    NetworkExecutorDaemon daemon;
    
    @Override
    public void setUp() {
        daemon = new NetworkExecutorDaemon("localhost", 6969);
        
    }
    
    public void testAbortExecution() {
        
    }
}
