package se.sics.stochastic.kriging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mathworks.toolbox.javabuilder.MWException;

import junit.framework.TestCase;

public class TestSK extends TestCase {
	Logger logger = LoggerFactory.getLogger(TestSK.class);
	
	StochasticKriging krg;
	
	public TestSK() {
		
	}
	
	@Override
	public void setUp() {
		krg = new StochasticKriging();		// use the default setting
	}
	
	@Override
	public void tearDown() {
		
	}
	
	public void testSettingLoad() {
		try {
			krg.load();
			assertTrue(true);
		} catch (MWException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertFalse(true);
		}
	}
	
	public void testFitting() {
		double[][] design = {{1.1}, {1.325}, {1.550}, {1.775}, {2}};
		double[] values = {9.1872, 2.3793, 1.1691, 0.7157, 0.5189};
		double[] stdvs = {0.0170, 0.0021, 0.0005, 0.0002, 0.0002};
		try {
			krg.load();
			String model = krg.SKfit(design, values, stdvs);
			logger.debug(model);
			assertTrue(true);
		} catch (MWException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		
	}
}
