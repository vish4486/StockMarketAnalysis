package com.sdm.utils;

import java.util.List;


/**
 * Utility class providing basic linear algebra operations like:
 * - Matrix multiplication
 * - Matrix transposition
 * - Matrix inversion (via Gauss-Jordan elimination)
 * - Fitting linear models using Least Squares
 */
public final class LinearAlgebraUtils {

    private LinearAlgebraUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    
    /**
     * Transposes a matrix (flips rows and columns)
     */
    public static double[][] transpose(final double[][] matrix) {
        final int rows = matrix.length;
        final int cols = matrix[0].length;
        final double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    
    /**
     * Multiplies two matrices (A * B)
     */
    public static double[][] multiply(final double[][] matrixA, final double[][] matrixB) {
        final int rows = matrixA.length;
        final int cols = matrixB[0].length;
        final int shared = matrixA[0].length;
        final double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                for (int k = 0; k < shared; k++) {
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
                }
            }
        }
        return result;
    }

    
    /**
     * Multiplies a matrix with a vector (A * x)
     */
    public static double[] multiply(final double[][] matrix, final double[] vector) {
        final int rows = matrix.length;
        final int cols = vector.length;
        final double[] result = new double[rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[i] += matrix[i][j] * vector[j];
            }
        }
        return result;
    }

    
    /**
     * Inverts a square matrix using Gauss-Jordan elimination.
     * Assumes matrix is non-singular.
     */
    public static double[][] invert(final double[][] matrix) {
        final int matrixSize = matrix.length;
        final double[][] augmented = new double[matrixSize][2 * matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, matrixSize);
            augmented[i][i + matrixSize] = 1;
        }
        for (int i = 0; i < matrixSize; i++) {
            final double pivot = augmented[i][i];
            for (int j = 0; j < 2 * matrixSize; j++) {
                augmented[i][j] /= pivot;
            }
            for (int k = 0; k < matrixSize; k++) {
                if (k != i) {
                    final double factor = augmented[k][i];
                    for (int j = 0; j < 2 * matrixSize; j++) {
                        augmented[k][j] -= factor * augmented[i][j];
                    }
                }
            }
        }
        final double[][] inverse = new double[matrixSize][matrixSize];
        for (int i = 0; i < matrixSize; i++) {
            System.arraycopy(augmented[i], matrixSize, inverse[i], 0, matrixSize);
        }
        return inverse;
    }

    
    /**
     * Solves for linear regression weights using the Normal Equation:
     * θ = (XᵀX)^-1 Xᵀy
     */
    public static double[] fitLeastSquares(final double[][] features, final List<Double> targets) {
        final int numSamples = features.length;
        final int numFeatures = features[0].length;
        final double[][] xMatrix = new double[numSamples][numFeatures];
        final double[][] yMatrix = new double[numSamples][1];

        for (int i = 0; i < numSamples; i++) {
            System.arraycopy(features[i], 0, xMatrix[i], 0, numFeatures);
            yMatrix[i][0] = targets.get(i);
        }

        final double[][] xT = transpose(xMatrix);
        final double[][] xTx = multiply(xT, xMatrix);
        final double[][] xTxInv = invert(xTx);
        final double[][] xTy = multiply(xT, yMatrix);
        final double[][] theta = multiply(xTxInv, xTy);

        final double[] weights = new double[numFeatures];
        for (int i = 0; i < numFeatures; i++) {
            weights[i] = theta[i][0];
        }

        return weights;
    }

    
    /**
     * Computes dot product between two vectors
     */
    public static double dot(final double[] vectorA, final double[] vectorB) {
        if (vectorA.length != vectorB.length) {
            throw new IllegalArgumentException("Vector sizes must match");
        }
        double sum = 0;
        for (int i = 0; i < vectorA.length; i++) {
            sum += vectorA[i] * vectorB[i];
        }
        return sum;
    }
}
