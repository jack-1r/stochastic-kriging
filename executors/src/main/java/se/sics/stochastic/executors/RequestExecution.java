package se.sics.stochastic.executors;

import se.sics.kompics.Event;

public class RequestExecution extends Event {
	private String command;
	private int expId;
	private int repId;

	public int getRepId() {
		return repId;
	}

	public RequestExecution(int expId, int repId, String cmd) {
		this.expId = expId;
		this.repId = repId;
		this.command = cmd;
	}

	public String getCommand() {
		return command;
	}

	public int getExpId() {
		return expId;
	}

}
