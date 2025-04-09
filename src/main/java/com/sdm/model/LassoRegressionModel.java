package com.sdm.model;
import java.util.List;
import java.util.Arrays;


/**
 * Implements Lasso Regression (L1 regularized linear regression) using
 * coordinate descent. Useful for feature selection and preventing overfitting.
 */
public class LassoRegressionModel implements PredictionModel {
    private double[] weights; //model weights learned while training
    private boolean trained = false; 
    private final double lambda;  // Regularization strength (higher = more penalty)
     // Convergence settings for coordinate descent
    private static final int MAX_ITERATIONS = 1000;
    private static final double TOLERANCE = 1e-4;

    /**
     * Constructor for the Lasso model with a custom lambda (regularization).
     *
     * @param lambda regularization strength (L1 penalty)
     */
    public LassoRegressionModel(final double lambda) {
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
    
    
    /**
     * Trains the model using coordinate descent algorithm for Lasso Regression
     *
     * @param features input features as List of double arrays
     * @param targets  target values (true labels)
     */
    @SuppressWarnings({ "PMD.CognitiveComplexity", "PMD.CyclomaticComplexity" })
    @Override
    public void train(final List<double[]> features,final List<Double> targets) {
        final int numSamples = features.size();
        final int numFeatures = features.get(0).length;
        final double[][] xMatrix = new double[numSamples][numFeatures];
        final double[] targetValues = new double[numSamples];

        for (int i = 0; i < numSamples; i++) {
            xMatrix[i] = features.get(i);
            targetValues[i] = targets.get(i);
        }

        // Initialize all weights to zero
        weights = new double[numFeatures];
        Arrays.fill(weights, 0.0);

        // Coordinate Descent main loop(Gradient Descent approach)
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            final double[] oldWeights = Arrays.copyOf(weights, weights.length);

            // Update each weight dimension-wise (coordinate)
            for (int j = 0; j < numFeatures; j++) {
                double residual = 0.0;
                 // Calculate residual error for feature j by removing its contribution
                for (int i = 0; i < numSamples; i++) {
                    double dot = 0.0;
                    for (int k = 0; k < numFeatures; k++) {
                        if (k != j)
                        {
                            dot += xMatrix[i][k] * weights[k];
                        }
                    }
                    residual += xMatrix[i][j] * (targetValues[i] - dot);
                }

                // Average the residual
                final double rho = residual / numSamples;

                // (L1 shrinkage)
                if (rho < -lambda) {
                    weights[j] = (rho + lambda);
                } else if (rho > lambda) {
                    weights[j] = (rho - lambda);
                } else {
                    weights[j] = 0.0;
                }
            }

            // Check for convergence (weights not changing significantly)
            if (hasConverged(oldWeights, weights)) 
            {
                break;
            }
        }
        trained = true;
    }

    
    /**
     * Checks if weights have converged within the tolerance.
     *
     * @param oldW previous weight vector
     * @param newW new weight vector
     * @return true if sum of absolute differences < tolerance
     */
    private boolean hasConverged(final double[] oldW, final double[] newW) {
        double sum = 0.0;
        for (int i = 0; i < oldW.length; i++) {
            sum += Math.abs(oldW[i] - newW[i]);
        }
        return sum < TOLERANCE;
    }

    
    /**
     * Predicts a value given a new feature input.
     *
     * @param inputFeatures feature array for the new data point
     * @return predicted value
     */
    @Override
    public double predict(final double[] inputFeatures) {
        if (!trained) 
        {
            throw new IllegalStateException("Model is not trained");
        }
        double result = 0.0;
        for (int i = 0; i < weights.length; i++) {
            result += weights[i] * inputFeatures[i];
        }
        return result;
    }
}
