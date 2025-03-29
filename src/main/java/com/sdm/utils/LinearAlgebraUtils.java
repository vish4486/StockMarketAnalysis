package com.sdm.utils;
import java.util.List;


public class LinearAlgebraUtils {

    public static double[][] transpose(double[][] a) {
        int rows = a.length, cols = a[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result[j][i] = a[i][j];
        return result;
    }

    public static double[][] multiply(double[][] a, double[][] b) {
        int rows = a.length, cols = b[0].length, shared = a[0].length;
        double[][] result = new double[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                for (int k = 0; k < shared; k++)
                    result[i][j] += a[i][k] * b[k][j];
        return result;
    }

    public static double[] multiply(double[][] a, double[] x) {
        int rows = a.length, cols = x.length;
        double[] result = new double[rows];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                result[i] += a[i][j] * x[j];
        return result;
    }

    public static double[][] invert(double[][] matrix) {
        int n = matrix.length;
        double[][] augmented = new double[n][2 * n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmented[i], 0, n);
            augmented[i][i + n] = 1;
        }
        for (int i = 0; i < n; i++) {
            double pivot = augmented[i][i];
            for (int j = 0; j < 2 * n; j++) augmented[i][j] /= pivot;
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmented[k][i];
                    for (int j = 0; j < 2 * n; j++) {
                        augmented[k][j] -= factor * augmented[i][j];
                    }
                }
            }
        }
        double[][] inv = new double[n][n];
        for (int i = 0; i < n; i++)
            System.arraycopy(augmented[i], n, inv[i], 0, n);
        return inv;
    }

    public static double[] fitLeastSquares(double[][] features, List<Double> targets) {
    int n = features.length;
    int m = features[0].length;

    double[][] X = new double[n][m];
    double[][] y = new double[n][1];

    for (int i = 0; i < n; i++) {
        System.arraycopy(features[i], 0, X[i], 0, m);
        y[i][0] = targets.get(i);
    }

    double[][] Xt = transpose(X);
    double[][] XtX = multiply(Xt, X);
    double[][] XtX_inv = invert(XtX);
    double[][] XtY = multiply(Xt, y);
    double[][] theta = multiply(XtX_inv, XtY);

    double[] weights = new double[m];
    for (int i = 0; i < m; i++) {
        weights[i] = theta[i][0];
    }

    return weights;
}


    public static double dot(double[] a, double[] b) {
        if (a.length != b.length) throw new IllegalArgumentException("Vector sizes must match");
        double sum = 0;
        for (int i = 0; i < a.length; i++) sum += a[i] * b[i];
        return sum;
    }
}
