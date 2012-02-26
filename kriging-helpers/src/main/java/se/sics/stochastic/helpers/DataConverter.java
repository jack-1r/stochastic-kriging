package se.sics.stochastic.helpers;

import com.mathworks.toolbox.javabuilder.MWCharArray;
import com.mathworks.toolbox.javabuilder.MWClassID;
import com.mathworks.toolbox.javabuilder.MWComplexity;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

public class DataConverter {

	public static MWNumericArray J2MDouble(double[] input) {
		int k = input.length;
		int[] dims = { k, 1 }, index = { 1, 1 };
		MWNumericArray output = MWNumericArray.newInstance(dims,
				MWClassID.DOUBLE, MWComplexity.REAL);

		for (index[0] = 1; index[0] <= dims[0]; index[0]++) {
			for (index[1] = 1; index[1] <= dims[1]; index[1]++) {
				output.set(index, input[index[0] - 1]);
			}
		}
		return output;
	}

	public static double[] M2JDouble(MWNumericArray input) {
		int k = input.numberOfElements();
		int[] dims = { k, 1 }, index = { 1, 1 };
		double[] output = new double[k];

		for (index[0] = 1; index[0] <= dims[0]; index[0]++) {
			for (index[1] = 1; index[1] <= dims[1]; index[1]++) {
				output[index[0] - 1] = input.getDouble(index);
			}
		}
		return output;
	}

	public static double[][] M2JDouble2(MWNumericArray input) {
		int[] dims = input.getDimensions(), index = { 1, 1 };

		double[][] output = new double[dims[0]][dims[1]];
		for (index[0] = 1; index[0] <= dims[0]; index[0]++) {
			for (index[1] = 1; index[1] <= dims[1]; index[1]++) {
				output[index[0] - 1][index[1] - 1] = input.getDouble(index);
			}
		}
		return output;
	}

	public static MWNumericArray J2MDouble2(double[][] input) {
		int[] dims = { input.length, input[1].length}, index = { 1, 1 };

		MWNumericArray output = MWNumericArray.newInstance(dims,
				MWClassID.DOUBLE, MWComplexity.REAL);

		for (index[0] = 1; index[0] <= dims[0]; index[0]++) {
			for (index[1] = 1; index[1] <= dims[1]; index[1]++) {
				output.set(index, input[index[0] - 1][index[1] - 1]);
			}
		}
		System.out.println(Integer.toString(output.getDimensions()[0]) + ":" + Integer.toString(output.getDimensions()[1]));
		return output;
	}
	
	public static MWCharArray JString2M(String input) {
		MWCharArray output = new MWCharArray(input);
		return output;
	}
}
