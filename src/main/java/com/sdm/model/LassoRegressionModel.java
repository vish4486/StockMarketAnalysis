package com.sdm.model;
import java.util.List;
import java.util.Arrays;

public class LassoRegressionModel implements PredictionModel {
    private double[] weights;
    private boolean trained = false;
    private final double lambda;
    private final int maxIterations = 1000;
    private final double tolerance = 1e-4;

    public LassoRegressionModel(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getName() {
        return "LassoRegression (λ=" + lambda + ")";
    }

    @Override
    public boolean supportsUnivariate() {
        return false;
    }

    @Override
    public boolean supportsMultivariate() {
        return true;
    }

    @Override
    public void train(List<double[]> features, List<Double> targets) {
        int n = features.size();
        int m = features.get(0).length;
        double[][] X = new double[n][m];
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            X[i] = features.get(i);
            y[i] = targets.get(i);
        }

        weights = new double[m];
        Arrays.fill(weights, 0.0);

        for (int iter = 0; iter < maxIterations; iter++) {
            double[] oldWeights = Arrays.copyOf(weights, weights.length);
            for (int j = 0; j < m; j++) {
                double residual = 0.0;
                for (int i = 0; i < n; i++) {
                    double dot = 0.0;
                    for (int k = 0; k < m; k++) {
                        if (k != j) dot += X[i][k] * weights[k];
                    }
                    residual += X[i][j] * (y[i] - dot);
                }
                double rho = residual / n;
                if (rho < -lambda) {
                    weights[j] = (rho + lambda);
                } else if (rho > lambda) {
                    weights[j] = (rho - lambda);
                } else {
                    weights[j] = 0.0;
                }
            }
            if (hasConverged(oldWeights, weights)) break;
        }
        trained = true;
    }

    private boolean hasConverged(double[] oldW, double[] newW) {
        double sum = 0.0;
        for (int i = 0; i < oldW.length; i++) {
            sum += Math.abs(oldW[i] - newW[i]);
        }
        return sum < tolerance;
    }

    @Override
    public double predict(double[] inputFeatures) {
        if (!trained) throw new IllegalStateException("Model is not trained");
        double result = 0.0;
        for (int i = 0; i < weights.length; i++) {
            result += weights[i] * inputFeatures[i];
        }
        return result;
    }
}
