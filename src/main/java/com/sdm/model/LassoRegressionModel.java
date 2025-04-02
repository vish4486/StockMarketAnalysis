package com.sdm.model;
import java.util.List;
import java.util.Arrays;

public class LassoRegressionModel implements PredictionModel {
    private double[] weights;
    private boolean trained = false;
    private final double lambda;
    private static final int MAX_ITERATIONS = 1000;
    private static final double TOLERANCE = 1e-4;

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

        weights = new double[numFeatures];
        Arrays.fill(weights, 0.0);

        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {
            final double[] oldWeights = Arrays.copyOf(weights, weights.length);
            for (int j = 0; j < numFeatures; j++) {
                double residual = 0.0;
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
                final double rho = residual / numSamples;
                if (rho < -lambda) {
                    weights[j] = (rho + lambda);
                } else if (rho > lambda) {
                    weights[j] = (rho - lambda);
                } else {
                    weights[j] = 0.0;
                }
            }
            if (hasConverged(oldWeights, weights)) 
            {
                break;
            }
        }
        trained = true;
    }

    private boolean hasConverged(final double[] oldW, final double[] newW) {
        double sum = 0.0;
        for (int i = 0; i < oldW.length; i++) {
            sum += Math.abs(oldW[i] - newW[i]);
        }
        return sum < TOLERANCE;
    }

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
