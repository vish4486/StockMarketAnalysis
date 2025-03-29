package com.sdm.model;
import com.sdm.service.CrossValidator;
import com.sdm.utils.LinearAlgebraUtils;
import com.sdm.utils.PolynomialFeatureExpander;
//import java.util.ArrayList;
import java.util.List;


public class MultivariatePolynomialRegressionModel implements PredictionModel {
    private int bestDegree;
    private double[] weights;
    private boolean trained = false;
    private final int maxDegree;
  

    public MultivariatePolynomialRegressionModel() {
        this(3); // Default:  degrees 1 to 3
    }

    public MultivariatePolynomialRegressionModel(int maxDegree) {
        this.maxDegree = maxDegree;
        
    }

    @Override
    public String getName() {
        return "MultivariatePolyRegression (maxDeg=" + maxDegree + ")";
    }

    @Override
    public boolean supportsMultivariate() {
        return true;
    }

    @Override
    public boolean supportsUnivariate() {
        return false;
    }

    
   public void train(List<double[]> features, List<Double> targets) {
        if (features == null || targets == null || features.isEmpty() || targets.isEmpty()) {
            throw new IllegalArgumentException("Features or targets cannot be null or empty.");
        }

        double bestScore = Double.NEGATIVE_INFINITY;

        for (int d = 1; d <= maxDegree; d++) {
            double score = CrossValidator.crossValidateR2(features, targets, d, 5);
            System.out.printf(" Degree %d ➜ CV R² = %.4f%n", d, score);

            if (score > bestScore) {
                bestScore = score;
                bestDegree = d;
            }
        }

        System.out.println(" Selected best degree: " + bestDegree + " with R² = " + bestScore);

        double[][] expanded = new PolynomialFeatureExpander(bestDegree)
                .expand(features)
                .toArray(new double[0][]);

        weights = LinearAlgebraUtils.fitLeastSquares(expanded, targets);
        trained = true;
    }

    @Override
    public double predict(double[] inputFeatures) {
        if (!trained) throw new IllegalStateException("Model not trained");

        double[] expanded = new PolynomialFeatureExpander(bestDegree).expandSingle(inputFeatures);
        return LinearAlgebraUtils.dot(weights, expanded);
    }

    public int getSelectedDegree() {
        return bestDegree;
    }
}
