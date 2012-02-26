package se.sics.stochastic.kriging;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathworks.toolbox.javabuilder.MWException;

import junit.framework.TestCase;

public class TestSA extends TestCase {
	Logger logger = LoggerFactory.getLogger(TestSA.class);

	StochasticKriging krg;
	SimulatedAnnealing sa;

	public TestSA() {

	}

	@Override
	public void setUp() {
		krg = new StochasticKriging(); // use the default setting
		sa = new SimulatedAnnealing();
	}

	@Override
	public void tearDown() {

	}

	public void testSettingLoad() {
		try {
			sa.load();
			assertTrue(true);
		} catch (MWException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertFalse(true);
		}
	}

	//TODO: this test is to be removed because the method is no longer used.
	public void testMseMin() {
		double[][] design = {{1.1}, {1.325}, {1.550}, {1.775}, {2}};
		double[] values = { 9.1872, 2.3793, 1.1691, 0.7157, 0.5189 };
		double[] stdvs = { 0.0170, 0.0021, 0.0005, 0.0002, 0.0002 };
		double[] maxX = { 2 };
		double[] minX = { 1 };
		double threshold = -0.01;
		try {
			krg.load();
			String model = krg.SKfit(design, values, stdvs);
			logger.debug(model);
			sa.load();
			double[][] result = sa.mseMin(model, maxX, minX, threshold);
			for (double[] p : result) {
				logger.debug(Arrays.toString(p));
			}
			assertTrue(true);
		} catch (MWException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}

	}
}
