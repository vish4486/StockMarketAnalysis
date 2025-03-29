package com.sdm.model;
import com.sdm.utils.LinearAlgebraUtils;

//import java.util.ArrayList;
import java.util.List;

/**
 * Linear Regression with multiple features: open, high, low, volume ➝ close
 */
public class MultiFeatureLinearRegressionModel implements PredictionModel {
    private double[] weights; // Including bias/intercept
    private boolean trained = false;

    @Override
    public void train(List<Double> trainingPrices) {
        throw new UnsupportedOperationException("Use train(List<double[]>, List<Double>) instead.");
    }

    public void train(List<double[]> features, List<Double> targets) {
        int n = features.size();
        int m = features.get(0).length + 1; // +1 for bias

        double[][] X = new double[n][m];
        double[][] y = new double[n][1];

        for (int i = 0; i < n; i++) {
            X[i][0] = 1.0; // bias
            for (int j = 0; j < m - 1; j++) {
                X[i][j + 1] = features.get(i)[j];
            }
            y[i][0] = targets.get(i);
        }

        /*
        double[][] Xt = transpose(X);
        double[][] XtX = multiply(Xt, X);
        double[][] XtX_inv = invert(XtX);
        double[][] XtY = multiply(Xt, y);
        double[][] theta = multiply(XtX_inv, XtY);
        */
        double[][] Xt = LinearAlgebraUtils.transpose(X);
        double[][] XtX = LinearAlgebraUtils.multiply(Xt, X);
        double[][] XtX_inv = LinearAlgebraUtils.invert(XtX);
        double[][] XtY = LinearAlgebraUtils.multiply(Xt, y);
        double[][] theta = LinearAlgebraUtils.multiply(XtX_inv, XtY);

        weights = new double[m];
        for (int i = 0; i < m; i++) {
            weights[i] = theta[i][0];
        }

        trained = true;
    }

    @Override
    public double predictNext() {
        throw new UnsupportedOperationException("Use predict(double[]) with input features instead.");
    }

    public double predict(double[] inputFeatures) {
        if (!trained) throw new IllegalStateException("Model is not trained");

        double result = weights[0]; // bias
        for (int i = 0; i < inputFeatures.length; i++) {
            result += weights[i + 1] * inputFeatures[i];
        }
        return result;
    }

    

    @Override
    public boolean supportsMultivariate() {
        return true;
    }

    @Override
    public boolean supportsUnivariate() {
        return false;
    }

    @Override
    public String getName() {
        return "MultiFeatureLinearRegressionModel";
    }

}
