package com.sdm.model;
import com.sdm.utils.LinearAlgebraUtils;
import java.util.List;

@SuppressWarnings({"PMD.ShortVariable","PMD.LongVariable","PMD.AssignmentToNonFinalStatic"})
public class PolynomialRegressionModel implements PredictionModel {
    private double[] coefficients;
    private final int degree;
    private boolean trained = false;

    private final int modelId; //  unique model instance ID
    private static int counter = 1;

    public PolynomialRegressionModel(final int degree) {
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
    public void train(final List<Double> prices) {
        final int sampleCount = prices.size();
        final double[][] xMatrix = new double[sampleCount][degree + 1];
        final double[][] yMatrix = new double[sampleCount][1];

        for (int i = 0; i < sampleCount; i++) {
            final double xVal = i;
            for (int j = 0; j <= degree; j++) {
                xMatrix[i][j] = Math.pow(xVal, j);
            }
            yMatrix[i][0] = prices.get(i);
        }

       
       final double[][] xT = LinearAlgebraUtils.transpose(xMatrix);
       final double[][] xTx = LinearAlgebraUtils.multiply(xT, xMatrix);
       final double[][] xTxInv = LinearAlgebraUtils.invert(xTx);
       final double[][] xTy = LinearAlgebraUtils.multiply(xT, yMatrix);
       final double[][] theta = LinearAlgebraUtils.multiply(xTxInv, xTy);


        coefficients = new double[degree + 1];
        for (int i = 0; i <= degree; i++) {
            coefficients[i] = theta[i][0];
        }

        trained = true;
    }

    @Override
    public double predictNext() {
        if (!trained) 
        {
            throw new IllegalStateException("Model is not trained");
        }
        final double xVal = coefficients.length; // Predict next index
        double result = 0;
        for (int i = 0; i < coefficients.length; i++) {
            result += coefficients[i] * Math.pow(xVal, i);
        }
        return result;
    }
}
