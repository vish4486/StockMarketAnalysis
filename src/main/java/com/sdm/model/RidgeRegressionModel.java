package com.sdm.model;
import com.sdm.utils.LinearAlgebraUtils;
import java.util.List;


/**
 * Implements Ridge Regression (L2-regularized linear regression).
 * Useful for reducing overfitting in multivariate linear models.
 */
@SuppressWarnings({"PMD.ShortVariable","PMD.LongVariable"})
public class RidgeRegressionModel implements PredictionModel {
    private double[] weights;  //coefeccient including bias
    private boolean trained = false;
    private final double lambda;  //regularisation strength

    
    /**
     * Constructor with lambda for L2 penalty.
     *
     * @param lambda Regularization strength.
     */
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

    
    /**
     * Trains the Ridge Regression model.
     *
     * @param features Multivariate input features.
     * @param targets Output/target values.
     */
    @Override
    public void train(final List<double[]> features, final List<Double> targets) {

        if (features == null || targets == null || features.isEmpty() || targets.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty!");
        }
        final int sampleCount = features.size();
        final int featureCount = features.get(0).length + 1;  //+1 added for bias

        final double[][] xMatrix = new double[sampleCount][featureCount];
        final double[][] yMatrix = new double[sampleCount][1];

        // build X and Y matrices
        for (int i = 0; i < sampleCount; i++) {
            xMatrix[i][0] = 1.0; // bias
            for (int j = 0; j < featureCount - 1; j++) {
                xMatrix[i][j + 1] = features.get(i)[j];
            }
            yMatrix[i][0] = targets.get(i);
        }

        // evaluate regularized normal equation: θ = (XᵀX + λI)⁻¹ XᵀY
        final double[][] xT = LinearAlgebraUtils.transpose(xMatrix);
        final double[][] xTx = LinearAlgebraUtils.multiply(xT, xMatrix);

        // Apply L2 penalty to diagonal (skip bias term if needed)
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

    
    /**
     * Predicts output for a given input using learned weights.
     *
     * @param inputFeatures Feature vector (excluding bias).
     * @return Predicted target value.
     */
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
