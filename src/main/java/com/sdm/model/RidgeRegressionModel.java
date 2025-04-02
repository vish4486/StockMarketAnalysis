package com.sdm.model;
import com.sdm.utils.LinearAlgebraUtils;
import java.util.List;

@SuppressWarnings({"PMD.ShortVariable","PMD.LongVariable"})
public class RidgeRegressionModel implements PredictionModel {
    private double[] weights;
    private boolean trained = false;
    private final double lambda;

    public RidgeRegressionModel(final double lambda) {
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
    public void train(final List<double[]> features, final List<Double> targets) {
        final int sampleCount = features.size();
        final int featureCount = features.get(0).length + 1;

        final double[][] xMatrix = new double[sampleCount][featureCount];
        final double[][] yMatrix = new double[sampleCount][1];

        for (int i = 0; i < sampleCount; i++) {
            xMatrix[i][0] = 1.0; // bias
            for (int j = 0; j < featureCount - 1; j++) {
                xMatrix[i][j + 1] = features.get(i)[j];
            }
            yMatrix[i][0] = targets.get(i);
        }

        final double[][] xT = LinearAlgebraUtils.transpose(xMatrix);
        final double[][] xTx = LinearAlgebraUtils.multiply(xT, xMatrix);

        for (int i = 0; i < xTx.length; i++) {
            xTx[i][i] += lambda; // L2 penalty
        }

        final double[][] xTy = LinearAlgebraUtils.multiply(xT, yMatrix);
        final double[][] theta = LinearAlgebraUtils.multiply(LinearAlgebraUtils.invert(xTx), xTy);

        weights = new double[featureCount];
        for (int i = 0; i < featureCount; i++) {
            weights[i] = theta[i][0];
        }

        trained = true;
    }

    @Override
    public double predict(final double[] inputFeatures) {
        if (!trained) 
        {
            throw new IllegalStateException("Model is not trained");
        }

        double result = weights[0]; // bias
        for (int i = 0; i < inputFeatures.length; i++) {
            result += weights[i + 1] * inputFeatures[i];
        }
        return result;
    }
}
