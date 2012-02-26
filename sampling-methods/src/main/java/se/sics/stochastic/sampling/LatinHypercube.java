package se.sics.stochastic.sampling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatinHypercube implements Sampler {

	private Logger logger = LoggerFactory.getLogger(LatinHypercube.class);

	private int dim; // number of dimensions
	private double[] min; // lower bound vector
	private double[] max; // upper bound vector
	private int num; // number of points
	boolean[] isInteger; // true if the param can only take integer value
	private HashMap<Integer, ArrayList<Double>> vars = new HashMap<Integer, ArrayList<Double>>();

	public LatinHypercube(int num, double[] min, double[] max,
			boolean[] isInteger) {
		if (min.length != max.length) {
			System.out
					.println("The number of dimension of both points must agree");
			System.exit(0);
		}

		dim = min.length;
		this.num = num;
		this.min = min;
		this.max = max;
		this.isInteger = isInteger;

		initialize();
	}

	public double[] getNext() {
		double[] value = new double[dim];
		logger.debug("get next value.");

		if (isEmpty())
			return null;

		ArrayList<Double> values;
		for (int i = 0; i < dim; i++) {
			values = vars.get(i);
			Collections.shuffle(values);
			value[i] = values.get(0);
			values.remove(0);
			if (isInteger[i] == true)
				value[i] = Math.round(value[i]);
		}
		return value;
	}

	public boolean isEmpty() {
		if (vars.get(0).size() == 0)
			return true;
		return false;

	}

	private void initialize() {
		for (int i = 0; i < dim; i++) {
			double value, step;
			ArrayList<Double> values = new ArrayList<Double>();

			step = (max[i] - min[i]) / num;
			Random r = new Random();
			for (int j = 0; j < num; j++) {
				value = min[i] + (j + r.nextDouble()) * step;
				values.add(value);
			}
			vars.put(i, values);
		}
	}

}
