package com.sdm.model;
import com.sdm.utils.LinearAlgebraUtils;

//import java.util.ArrayList;
import java.util.List;

/**
 * Linear Regression with multiple features: open, high, low, volume ➝ close
 * Uses the Normal Equation:
 *     θ = (XᵗX)⁻¹Xᵗy
 */
@SuppressWarnings({"PMD.ShortVariable","PMD.LongVariable","PMD.GuardLogStatement","PMD.AtLeastOneConstructor"})
public class MultiFeatureLinearRegressionModel implements PredictionModel {
    private double[] weights;  // θ vector including bias/intercept
    private boolean trained = false;

    
    /**
     * This is NOT supported for multivariate models.
     * Enforces correct API usage.
     */
    @Override
    public void train(List<Double> trainingPrices) {
        throw new UnsupportedOperationException("Use train(List<double[]>, List<Double>) instead.");
    }
    

    /**
     * Trains the multivariate linear regression model using normal equation.
     *
     * @param features Input features (each sample is a double[] with N features)
     * @param targets  Corresponding true target values (1D list)
     */
    @Override
    public void train(final List<double[]> features, final List<Double> targets) {
        final int numSamples = features.size();
        final int numFeaturesWithBias = features.get(0).length + 1; // +1 for bias

        final double[][] xMatrix = new double[numSamples][numFeaturesWithBias];
        final double[][] yMatrix = new double[numSamples][1];

        for (int i = 0; i < numSamples; i++) {
            xMatrix[i][0] = 1.0; // bias
            for (int j = 0; j < numFeaturesWithBias - 1; j++) {
                xMatrix[i][j + 1] = features.get(i)[j];
            }
            yMatrix[i][0] = targets.get(i);
        }

        /*
        double[][] Xt = transpose(X);
        double[][] XtX = multiply(Xt, X);
        double[][] XtX_inv = invert(XtX);
        double[][] XtY = multiply(Xt, y);
        double[][] theta = multiply(XtX_inv, XtY);
        */
        final double[][] xT = LinearAlgebraUtils.transpose(xMatrix);
        final double[][] xTx = LinearAlgebraUtils.multiply(xT, xMatrix);
        final double[][] xTxInv = LinearAlgebraUtils.invert(xTx);
        final double[][] xTy = LinearAlgebraUtils.multiply(xT, yMatrix);
        final double[][] theta = LinearAlgebraUtils.multiply(xTxInv, xTy);

        weights = new double[numFeaturesWithBias];
        for (int i = 0; i < numFeaturesWithBias; i++) {
            weights[i] = theta[i][0];
        }

        trained = true;
    }

    
    /**
     * This overload is not supported — requires feature input for prediction.
     */
    @Override
    public double predictNext() {
        throw new UnsupportedOperationException("Use predict(double[]) with input features instead.");
    }
    
    
    /**
     * Predicts the target variable using the trained weights and input features.
     *
     * @param inputFeatures Array of input features for the current sample
     * @return Predicted target value (e.g., next closing price)
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
