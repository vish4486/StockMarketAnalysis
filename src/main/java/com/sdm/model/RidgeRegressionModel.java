package com.sdm.model;
import com.sdm.utils.LinearAlgebraUtils;
import java.util.List;

public class RidgeRegressionModel implements PredictionModel {
    private double[] weights;
    private boolean trained = false;
    private final double lambda;

    public RidgeRegressionModel(double lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getName() {
        return "RidgeRegression (λ=" + lambda + ")";
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
        int m = features.get(0).length + 1;

        double[][] X = new double[n][m];
        double[][] y = new double[n][1];

        for (int i = 0; i < n; i++) {
            X[i][0] = 1.0; // bias
            for (int j = 0; j < m - 1; j++) {
                X[i][j + 1] = features.get(i)[j];
            }
            y[i][0] = targets.get(i);
        }

        double[][] Xt = LinearAlgebraUtils.transpose(X);
        double[][] XtX = LinearAlgebraUtils.multiply(Xt, X);

        for (int i = 0; i < XtX.length; i++) {
            XtX[i][i] += lambda; // L2 penalty
        }

        double[][] XtY = LinearAlgebraUtils.multiply(Xt, y);
        double[][] theta = LinearAlgebraUtils.multiply(LinearAlgebraUtils.invert(XtX), XtY);

        weights = new double[m];
        for (int i = 0; i < m; i++) {
            weights[i] = theta[i][0];
        }

        trained = true;
    }

    @Override
    public double predict(double[] inputFeatures) {
        if (!trained) throw new IllegalStateException("Model is not trained");

        double result = weights[0]; // bias
        for (int i = 0; i < inputFeatures.length; i++) {
            result += weights[i + 1] * inputFeatures[i];
        }
        return result;
    }
}
