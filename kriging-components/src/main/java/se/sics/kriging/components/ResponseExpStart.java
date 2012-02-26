package se.sics.kriging.components;

import se.sics.kompics.Response;

public class ResponseExpStart extends Response {

    public static enum Status {SUCCESS, FAIL, SK_LOAD_PROBLEM, SA_LOAD_PROBLEM};
    private final Status status;
    private final String msg;
    
    public ResponseExpStart(RequestExpStart request, Status status) {
        this(request,status,"");
    }
    
    public ResponseExpStart(RequestExpStart request, Status status, String msg) {
        super(request);
        this.status = status;
        this.msg = msg;
    }

    public Status getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
	
}
