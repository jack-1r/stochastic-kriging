package se.sics.stochastic.executors;

import java.util.concurrent.Semaphore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.sics.kompics.Component;
import se.sics.kompics.ComponentDefinition;
import se.sics.kompics.Handler;
import se.sics.kompics.Kompics;
import se.sics.kompics.Start;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ExecutorTest extends TestCase {

	private static final Logger logger = LoggerFactory
			.getLogger(ExecutorTest.class);
	private static final int EVENT_COUNT = 2;
	private static Semaphore semaphore = new Semaphore(0);
	private boolean testStatus = true;

	/**
	 * Create the test case1
	 * 
	 * @param testName
	 *            name of the test case
	 */
	public ExecutorTest(String testName) {
		super(testName);
	}

	public void pass() {
		ExecutorTest.semaphore.release();
	}

	public void fail(boolean release) {
		testStatus = false;
		ExecutorTest.semaphore.release();
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite() {
		return new TestSuite(ExecutorTest.class);
	}

	/**
	 * Rigourous Test :-)
	 */
	public void testExecution() {
		TestExecutionMainComponent.testObj = this;
		Kompics.createAndStart(TestExecutionMainComponent.class);

		try {
			ExecutorTest.semaphore.acquire(EVENT_COUNT);
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

	public static class TestExecutionMainComponent extends ComponentDefinition {

		Component exec;
		public static ExecutorTest testObj = null;

		public TestExecutionMainComponent() {
			exec = create(SimpleExperimentExecutor.class);
			subscribe(handleStart, control);
			subscribe(handleResponse, exec.getPositive(PortExecutors.class));
		}

		public Handler<Start> handleStart = new Handler<Start>() {

			@Override
			public void handle(Start event) {
				logger.debug("started.");
				trigger(new InitExecutors(), exec.control());
				String cmd = "java -jar cyclon-experiment-run-0.4.2.7-jar-with-dependencies.jar 123456 15 10";

				trigger(new Start(), exec.control());
				trigger(new RequestExecution(0, 0, cmd),
						exec.getPositive(PortExecutors.class));
				trigger(new RequestExecution(0, 1, cmd),
						exec.getPositive(PortExecutors.class));
			}
		};
		public Handler<ResponseExecution> handleResponse = new Handler<ResponseExecution>() {

			@Override
			public void handle(ResponseExecution event) {
				logger.debug("exec terminated. expId={} repId={}",
						event.getExpId(), event.getRepId());
				logger.debug("result={}", event.getResult());
				// ExecutorTest.assertTrue("exec succeeded.", true);
				testObj.pass();
			}
		};
	}
}
