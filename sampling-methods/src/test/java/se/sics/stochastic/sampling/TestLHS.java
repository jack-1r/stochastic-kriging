package se.sics.stochastic.sampling;

import java.util.Arrays;

import junit.framework.TestCase;

public class TestLHS extends TestCase {

	Sampler s;

	public TestLHS() {
	}

	public void setUp() {
		int num = 5;
		double[] min = { 0, 5 }, max = { 4, 11 };
		boolean[] isInteger = { true, true };
		s = new LatinHypercube(num, min, max, isInteger);
	}

	public void tearDown() {
	}

	public void testLHS() {
		double[] result = s.getNext();
		while (result != null) {
			System.out.println(Arrays.toString(result));
			result = s.getNext();
		}
	}
}
