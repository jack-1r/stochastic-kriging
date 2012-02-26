package se.sics.stochastic.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Negative;
import se.sics.kompics.Start;

public class SimpleExperimentExecutor extends ComponentDefinition {

	private final Logger logger = LoggerFactory
			.getLogger(SimpleExperimentExecutor.class);

	Negative<PortExecutors> execPort = provides(PortExecutors.class);

	public SimpleExperimentExecutor() {
		subscribe(handleInit, control);
		subscribe(handleRequest, execPort);
		subscribe(handleStart, control);
	}

	public void triggerResult(int expId, int repId, String result) {
		trigger(new ResponseExecution(expId, repId, result), execPort);
	}

	private Handler<InitExecutors> handleInit = new Handler<InitExecutors>() {
		public void handle(InitExecutors event) {
//			logger.debug("initiated.");
		}
	};

	private Handler<Start> handleStart = new Handler<Start>() {
		public void handle(Start event) {
//			logger.debug("started.");
		}
	};

	private Handler<RequestExecution> handleRequest = new Handler<RequestExecution>() {
		public void handle(RequestExecution event) {
			logger.debug("got request.");
			try {
				exec(event.getExpId(), event.getRepId(), event.getCommand());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private void exec(int expId, int repId, String cmd) throws Exception {
		// spawn a new thread to run the requested experiment
		ExecutionThread exec = new ExecutionThread(expId, repId, cmd, this);
		Thread t = new Thread(exec);
		t.start();
	}
}
