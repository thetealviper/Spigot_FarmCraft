package me.TheTealViper.farmcraft.Utils;

public class MultiplyMatrices {

	public static double[][] multiplyMatrices(double[][] A, double[][] B) {
		int m1 = A.length;
		int n1 = A[0].length;
//		int m2 = B.length;
		int n2 = B[0].length;

		double[][] results = new double[m1][n2];
		for(int row1 = 0; row1 < m1; row1++) {
			for(int col2 = 0; col2 < n2; col2++) {
				double dotProductSum = 0;
				for(int dotProductPairIndex = 0; dotProductPairIndex < n1; dotProductPairIndex++) {
					dotProductSum += A[row1][dotProductPairIndex] * B[dotProductPairIndex][col2];
				}
				results[row1][col2] = dotProductSum;
			}
		}

		return results;
	}
	
}
