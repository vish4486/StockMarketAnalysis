package com.sdm.model;
import com.sdm.service.CrossValidator;
import com.sdm.utils.LinearAlgebraUtils;
import com.sdm.utils.PolynomialFeatureExpander;
//import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


/**
 * Implements multivariate polynomial regression with automatic degree selection.
 * Uses cross-validation to find the best polynomial degree (1 to maxDegree).
 * The model can handle multiple input features (e.g., open, high, low, volume),
 * and expands them into polynomial terms before applying least squares fitting.
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class MultivariatePolynomialRegressionModel implements PredictionModel {
    private int bestDegree;    // Chosen polynomial degree via cross-validation
    private double[] weights;  // Learned weights after training
    private boolean trained = false;
    private final int maxDegree;   // Upper bound for polynomial degrees to try
    private static final Logger LOGGER = Logger.getLogger(MultivariatePolynomialRegressionModel.class.getName());

  
    /**
     * Default constructor: for upto max degree 3 polynomial expansion.
     */
    public MultivariatePolynomialRegressionModel() {
        this(3); // Default:  degrees 1 to 3
    }

    public MultivariatePolynomialRegressionModel(final int maxDegree) {
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

    
    /**
     * this trains the model using polynomial feature expansion and cross-validation.
     * For each degree, evaluates performance via R² and picks the best one.
     */
    @Override
    public void train(final List<double[]> features, final List<Double> targets) {
        if (features == null || targets == null || features.isEmpty() || targets.isEmpty()) {
            throw new IllegalArgumentException("Features or targets cannot be null or empty.");
        }

        double bestScore = Double.NEGATIVE_INFINITY;

        for (int d = 1; d <= maxDegree; d++) {
            final double score = CrossValidator.crossValidateR2(features, targets, d, 5);
            //System.out.printf(" Degree %d ➜ CV R² = %.4f%n", d, score);
            LOGGER.info(String.format("Degree %d ➜ CV R² = %.4f", d, score));

            if (score > bestScore) {
                bestScore = score;
                bestDegree = d;
            }
        }

        //System.out.println(" Selected best degree: " + bestDegree + " with R² = " + bestScore);
          LOGGER.info("Selected best degree: " + bestDegree + " with R² = " + bestScore);

        
        // Expand features to polynomial terms of best degree
        final double[][] expanded = new PolynomialFeatureExpander(bestDegree)
                .expand(features)
                .toArray(new double[0][]);

        
        // Fit weights using least squares on expanded feature matrix        
        weights = LinearAlgebraUtils.fitLeastSquares(expanded, targets);
        trained = true;
    }

    
    /**
     * Predicts target value based on new input features after polynomial expansion.
     *
     * @param inputFeatures Raw input feature array (e.g., OHLCV)
     * @return predicted value using learned polynomial model
     */
    @Override
    public double predict(final double[] inputFeatures) {
        if (!trained) {
            throw new IllegalStateException("Model not trained");
        }

       final double[] expanded = new PolynomialFeatureExpander(bestDegree).expandSingle(inputFeatures);
        return LinearAlgebraUtils.dot(weights, expanded);
    }

    
    /**
     * Exposes the degree selected by cross-validation for reporting/inspection.
     */
    public int getSelectedDegree() {
        return bestDegree;
    }
}
