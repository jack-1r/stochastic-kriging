package se.sics.stochastic.executors;

import se.sics.kompics.Event;

public class AbortExecution extends Event {
    private int expId;
    
    public AbortExecution(int expId) {
        this.expId = expId;
    }

    public int getExpId() {
        return expId;
    }   
}
