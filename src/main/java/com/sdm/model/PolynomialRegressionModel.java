package com.sdm.model;
import com.sdm.utils.LinearAlgebraUtils;
import java.util.List;


public class PolynomialRegressionModel implements PredictionModel {
    private double[] coefficients;
    private int degree;
    private boolean trained = false;

    private final int modelId; //  unique model instance ID
    private static int counter = 1;

    public PolynomialRegressionModel(int degree) {
        this.degree = degree;
        this.modelId = counter++;
    }

    @Override
    public String getName() {
        return "PolynomialRegression (deg=" + degree + ") #" + modelId;
    }

    @Override
    public boolean supportsUnivariate() {
        return true;
    }

    @Override
    public boolean supportsMultivariate() {
        return false;
    }

    @Override
    public void train(List<Double> prices) {
        int n = prices.size();
        double[][] X = new double[n][degree + 1];
        double[][] y = new double[n][1];

        for (int i = 0; i < n; i++) {
            double xVal = i;
            for (int j = 0; j <= degree; j++) {
                X[i][j] = Math.pow(xVal, j);
            }
            y[i][0] = prices.get(i);
        }

       
       double[][] Xt = LinearAlgebraUtils.transpose(X);
       double[][] XtX = LinearAlgebraUtils.multiply(Xt, X);
       double[][] XtX_inv = LinearAlgebraUtils.invert(XtX);
       double[][] XtY = LinearAlgebraUtils.multiply(Xt, y);
       double[][] theta = LinearAlgebraUtils.multiply(XtX_inv, XtY);


        coefficients = new double[degree + 1];
        for (int i = 0; i <= degree; i++) {
            coefficients[i] = theta[i][0];
        }

        trained = true;
    }

    @Override
    public double predictNext() {
        if (!trained) throw new IllegalStateException("Model is not trained");
        double xVal = coefficients.length; // Predict next index
        double result = 0;
        for (int i = 0; i < coefficients.length; i++) {
            result += coefficients[i] * Math.pow(xVal, i);
        }
        return result;
    }
}
