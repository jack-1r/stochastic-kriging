package se.sics.kriging.application;

import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Init;
import se.sics.kompics.Kompics;
import se.sics.kriging.components.ExperimentManager;
import se.sics.kriging.components.InitExperiment;
import se.sics.kriging.components.PortApplication;
import se.sics.kriging.components.PortStatus;
import se.sics.stochastic.executors.CloudExecutionManager;
import se.sics.stochastic.executors.InitExecutors;
import se.sics.stochastic.executors.PortExecutors;

public class KrigingApplication {

	private static final Logger logger = LoggerFactory
			.getLogger(KrigingApplication.class);
	public static Semaphore semaphore = new Semaphore(0);

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		logger.info("Application started.");
		Kompics.createAndStart(MainComponent.class,2);

		final int EVENT_COUNT = 1;
		try {
			semaphore.acquire(EVENT_COUNT);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			Kompics.shutdown();
		}
	}

	public static class MainComponent extends ComponentDefinition {

		Component app, mgr, exec;

		public MainComponent() {
			logger.debug("Main created.");
			app = create(SimpleApplication.class);
			mgr = create(ExperimentManager.class);
//			exec = create(SimpleExperimentExecutor.class);
			exec = create(CloudExecutionManager.class);
			connect(app.required(PortApplication.class),
					mgr.provided(PortApplication.class));
			connect(mgr.required(PortExecutors.class),
					exec.provided(PortExecutors.class));
			connect(app.required(PortStatus.class),
					mgr.provided(PortStatus.class));

			trigger(new Init() {
			}, app.control());
			trigger(new InitExperiment(), mgr.control());
			trigger(new InitExecutors(), exec.control());

		}
	}
}
