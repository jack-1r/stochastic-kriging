package se.sics.stochastic.executors;

import se.sics.kompics.Event;

public class ResponseExecution extends Event {
	private int expId;
	private int repId;
	private String result;

	public ResponseExecution(int expId, int repId, String result) {
		this.expId = expId;
		this.repId = repId;
		this.result = result;
	}

	public int getRepId() {
		return repId;
	}

	public int getExpId() {
		return expId;
	}

	public String getResult() {
		return result;
	}

}
