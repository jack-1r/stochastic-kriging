package se.sics.kriging.components;

import se.sics.kompics.Event;

public class ResponseStatus extends Event {
	
	public static enum Status {
		SUCCESS, SK_LOAD_FAIL, SA_LOAD_FAIL, SK_RUN_FAIL, SA_RUN_FAIL, IO_ERROR, VISUALSATION_FAILED, WARNING
	};

	private String source;
	private Event request = null;
	private Status status;
	private String msg;

	public ResponseStatus(String source, Event request, Status status, String msg) {
		this.request = request;
		this.status = status;
		this.msg = msg;
		this.source = source;
	}
	
	public String getSource() {
		return source;
	}

	public Event getRequest() {
		return request;
	}

	public Status getStatus() {
		return status;
	}

	public String getMsg() {
		return msg;
	}
}
