package se.sics.stochastic.helpers;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
	private double[][] studentTable = {
			{ 0, 6.314, 2.920, 2.353, 2.132, 2.015, 1.943, 1.895, 1.860, 1.833,
					1.812, 1.796, 1.782, 1.771, 1.761, 1.753, 1.746, 1.740,
					1.734, 1.729, 1.725, 1.721, 1.717, 1.714, 1.711, 1.708,
					1.706, 1.703, 1.701, 1.699, 1.697, 1.684, 1.671, 1.664,
					1.660, 1.646, 1.645 },
			{ 0, 12.710, 4.303, 3.182, 2.776, 2.571, 2.447, 2.365, 2.306,
					2.262, 2.228, 2.201, 2.179, 2.160, 2.145, 2.131, 2.120,
					2.110, 2.101, 2.093, 2.086, 2.080, 2.074, 2.069, 2.064,
					2.060, 2.056, 2.052, 2.048, 2.045, 2.042, 2.021, 2.000,
					1.990, 1.984, 1.962, 1.960 },
			{ 0, 31.820, 6.965, 4.541, 3.747, 3.365, 3.143, 2.998, 2.896,
					2.821, 2.764, 2.718, 2.681, 2.650, 2.624, 2.602, 2.583,
					2.567, 2.552, 2.539, 2.528, 2.518, 2.508, 2.500, 2.492,
					2.485, 2.479, 2.473, 2.467, 2.462, 2.457, 2.423, 2.390,
					2.374, 2.364, 2.330, 2.326 },
			{ 0, 63.660, 9.925, 5.841, 4.604, 4.032, 3.707, 3.499, 3.355,
					3.250, 3.169, 3.106, 3.055, 3.012, 2.977, 2.947, 2.921,
					2.898, 2.878, 2.861, 2.845, 2.831, 2.819, 2.807, 2.797,
					2.787, 2.779, 2.771, 2.763, 2.756, 2.750, 2.704, 2.660,
					2.639, 2.626, 2.581, 2.576 },
			{ 0, 318.310, 22.327, 10.215, 7.173, 5.893, 5.208, 4.785, 4.501,
					4.297, 4.144, 4.025, 3.930, 3.852, 3.787, 3.733, 3.686,
					3.646, 3.610, 3.579, 3.552, 3.527, 3.505, 3.485, 3.467,
					3.450, 3.435, 3.421, 3.408, 3.396, 3.385, 3.307, 3.232,
					3.195, 3.174, 3.098, 3.090 },
			{ 0, 636.620, 31.599, 12.924, 8.610, 6.869, 5.959, 5.408, 5.041,
					4.781, 4.587, 4.437, 4.318, 4.221, 4.140, 4.073, 4.015,
					3.965, 3.922, 3.883, 3.850, 3.819, 3.792, 3.768, 3.745,
					3.725, 3.707, 3.690, 3.674, 3.659, 3.646, 3.551, 3.460,
					3.416, 3.390, 3.300, 3.291 }

	};


	private int confidence; //0->0.95; 1->0.975; 2->0.99; 3->0.995

	private List<Double> values;

	public Statistics(int d) {
		this.confidence = d;
		this.values = new ArrayList<Double>();
	}

	public void addValue(double value) {
		this.values.add(value);
	}
	
	public int size() {
		return values.size();
	}

	public double mean() {
		if (this.values.isEmpty())
			throw new RuntimeException("Zero measures");

		double sum = 0;
		for (double d : this.values)
			sum += d;

		return sum / (double)this.values.size();
	}
	
	public double variance() {
		return squaredS()*(this.values.size() - 1)/this.values.size();
	}
	
	public double kVariance() {
		return variance()/this.values.size();
	}

	public double sStdv() {
		return Math.sqrt(squaredS());
	}

	public double getWidth() {
		int n = this.values.size();
		double t = 0;
		if (n <=30)
			t = studentTable[confidence][n - 1];
		else if ((n>30) && (n<=40))
			t = studentTable[confidence][30];
		else if ((n>40) && (n<=50))
			t = studentTable[confidence][31];
		else if ((n>50) && (n<=60))
			t = studentTable[confidence][32];
		else if ((n>60) && (n<=80))
			t = studentTable[confidence][33];
		else if ((n>80) && (n<=100))
			t = studentTable[confidence][34];
		else if ((n>100) && (n<=120))
			t = studentTable[confidence][35];
		else if (n>120)
			t = studentTable[confidence][36];
		
		return t * Math.sqrt(squaredS() / n);
	}

	public double getRelativeWidth() {
		return getWidth() / mean();
	}

	private double squaredS() {
		if (this.values.size() < 2)
			throw new RuntimeException("Less than two measures");

		double total = 0;

		double xm = mean();

		for (double xi : this.values)
			total += (xi - xm) * (xi - xm);

		return total / ((double)this.values.size() - 1);
	}
}
