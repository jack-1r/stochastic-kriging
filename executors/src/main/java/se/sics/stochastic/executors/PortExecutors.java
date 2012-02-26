package se.sics.stochastic.executors;

import se.sics.kompics.PortType;

public class PortExecutors extends PortType {{
	request(RequestExecution.class);
	indication(ResponseExecution.class);
	request(AbortExecution.class);
}}
