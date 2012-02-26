package se.sics.kriging.application;

import java.io.Serializable;

import se.sics.kriging.components.RequestExpStart;

public class SessionDataBundle implements Serializable {
    
    /**
     * 
     */
    private static final long serialVersionUID = -5057024258392866999L;
    private RequestExpStart data;
    private String dimCount;
    private String maxEval;
    private int skAlgor;
    
    public SessionDataBundle(RequestExpStart data, String dimCount, String maxEval, int skAlgor) {
        this.data = data;
        this.dimCount = dimCount;
        this.maxEval = maxEval;
        this.skAlgor = skAlgor;
    }

    public String getMaxEval() {
        return maxEval;
    }

    public int getSkAlgor() {
        return skAlgor;
    }

    public RequestExpStart getData() {
        return data;
    }

    public String getDimCount() {
        return dimCount;
    }
}
