package se.sics.kriging.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import se.sics.kriging.components.ExperimentManager;
import se.sics.kriging.components.InitExperiment;
import se.sics.kriging.components.PortApplication;
import se.sics.kriging.components.RequestExpStart;
import se.sics.kriging.components.NotifyExecCompleted;
import se.sics.kriging.components.NotifyMinimizationCompleted;
import se.sics.kriging.components.RequestMSEMinimization;
import se.sics.kriging.components.RequestSK;
import se.sics.stochastic.executors.SimpleExperimentExecutor;
import se.sics.stochastic.executors.InitExecutors;
import se.sics.stochastic.executors.PortExecutors;

import junit.framework.TestCase;

public class TestManager extends TestCase {

	private static final Logger logger = LoggerFactory
			.getLogger(TestManager.class);
	private static final int EVENT_COUNT = 1;
	private Semaphore semaphore = new Semaphore(0);
	private boolean testStatus = true;

	public TestManager() {

	}

	public void setUp() {

	}

	public void tearDown() {

	}

	public void pass() {
		semaphore.release();
	}

	public void fail(boolean release) {
		semaphore.release();
	}

	public void testManager() {
		TestMainComponent.testObj = this;
		Kompics.createAndStart(TestMainComponent.class);

		try {
			semaphore.acquire(EVENT_COUNT);
			System.out.println("Finished test.");
		} catch (InterruptedException e) {
			assert (false);
		} finally {
			Kompics.shutdown();
		}

		if (testStatus == false) {
			assertTrue(false);
		}
		logger.debug("shut down.");
	}

	public static class TestMainComponent extends ComponentDefinition {
		public static TestManager testObj = null;

		Component mgr, exec;

		public TestMainComponent() {
			mgr = create(ExperimentManager.class);
			exec = create(SimpleExperimentExecutor.class);

			connect(mgr.required(PortExecutors.class),
					exec.provided(PortExecutors.class));
			subscribe(handleStart, control);
			subscribe(handleSKProcceed, mgr.getPositive(PortApplication.class));
			subscribe(handleSKCompleted, mgr.getPositive(PortApplication.class));
			subscribe(handleResult, mgr.getPositive(PortApplication.class));
		}

		public Handler<Start> handleStart = new Handler<Start>() {
			public void handle(Start event) {
				logger.debug("test started.");
				trigger(new InitExecutors(), exec.control());
				trigger(new Start(), exec.control());

				String jarPath = "BraninExperiment-0.0.1-SNAPSHOT-jar-with-dependencies.jar";
				jarPath = System.getProperty("user.dir")
						+ System.getProperty("file.separator") + jarPath;
				int confidenceLevel = 0; // 95%
				double confidenceLength = 0.4;
				int num = 10;
				double threshold = -10;
				double[] min = { -5, 0 };
				double[] max = { 10, 15 };
				boolean[] isInteger = { true, true };
				trigger(new InitExperiment(), mgr.control());
				RequestExpStart ExpStart = new RequestExpStart();
				ExpStart.setJarPath(jarPath);
				ExpStart.setConfidence(confidenceLevel); // 95%
				ExpStart.setLb(min);
				ExpStart.setUb(max);
				ExpStart.setNum(num);
				ExpStart.setRatio(confidenceLength);
				ExpStart.setThreshold(threshold);
				ExpStart.setIntVector(isInteger);
				trigger(ExpStart, mgr.getPositive(PortApplication.class));
			}
		};

		public Handler<NotifyExecCompleted> handleSKProcceed = new Handler<NotifyExecCompleted>() {
			public void handle(NotifyExecCompleted event) {
				trigger(new RequestSK(), mgr.getPositive(PortApplication.class));
			}
		};

		public Handler<NotifySKCompleted> handleSKCompleted = new Handler<NotifySKCompleted>() {
			public void handle(NotifySKCompleted event) {
				System.out.print("<c> to proceed, others to re-run SK:");
				try {
					BufferedReader r = new BufferedReader(new InputStreamReader(
							System.in));
					String command = r.readLine();
					if (command.equals("c")) {
						trigger(new RequestMSEMinimization(),
								mgr.getPositive(PortApplication.class));
					} else 
						trigger(new RequestSK(), mgr.getPositive(PortApplication.class));
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		};

		public Handler<NotifyMinimizationCompleted> handleResult = new Handler<NotifyMinimizationCompleted>() {
			public void handle(NotifyMinimizationCompleted event) {
				System.out.println("MSE minimized. Exp comleted.");
				testObj.pass();
			}
		};
	}
}
