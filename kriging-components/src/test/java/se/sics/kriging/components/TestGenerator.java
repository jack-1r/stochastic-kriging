package se.sics.kriging.components;

import java.util.Arrays;

public class TestGenerator {

	/**
	 * @param args
	 */
	public static int N_DIM = 5;
	public static int N_SEP = 2;
	public static int[] coeffVector = new int[N_DIM];
	public static int count = 0;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		generateCoeffMatrix(N_DIM);
		System.out.println("count=" + Integer.toString(count));
	}

	private static void generateCoeffMatrix(int nDim) {
		if (nDim == 0) {
			System.out.println(Arrays.toString(coeffVector));
			count++;
			return;
		}

		for (int i = 0; i < N_SEP; i++) {
			coeffVector[nDim - 1] = i;
			generateCoeffMatrix(nDim - 1);
		}
	}
}
