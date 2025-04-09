package com.sdm.model;

import java.util.List;


/**
 * PredictionModel is an interface for machine learning regression models
 * that predict future stock prices based on historical data.
 *
 * It supports both univariate and multivariate regression types.
 * Models can override only the methods they support.
 */
public interface PredictionModel {

    
    /**
     * Trains the model using univariate data (e.g., closing prices).
     *
     * @param trainingPrices List of historical prices.
     */
    default void train(List<Double> trainingPrices) {
        throw new UnsupportedOperationException("Univariate training not supported.");
    }

    
    /**
     * Predicts the next value based on univariate training.
     *
     * @return Predicted next value.
     */
    default double predictNext() {
        throw new UnsupportedOperationException("Univariate prediction not supported.");
    }

    
    /**
     * Trains the model using multivariate features (e.g., open, high, low, volume).
     *
     * @param features Matrix of feature vectors.
     * @param targets List of actual target values (e.g., closing prices).
     */
    default void train(List<double[]> features, List<Double> targets) {
        throw new UnsupportedOperationException("Multivariate training not supported.");
    }

    /**
     * Predicts output for given input features (multivariate).
     *
     * @param inputFeatures Input feature vector.
     * @return Predicted value.
     */
    default double predict(double[] inputFeatures) {
        throw new UnsupportedOperationException("Multivariate prediction not supported.");
    }

    
    /**
     * Returns a name or identifier for the model (can be overridden).
     *
     * @return Model name as string.
     */
    default String getName() {
        return this.getClass().getSimpleName();
    }

    
    /**
     * Indicates if the model supports univariate/Multivariate inputs.
     *
     * @return true if univariate/Multivariate is supported, false otherwise.
     */
    default boolean supportsUnivariate() { return false; }
    default boolean supportsMultivariate() { return false; }

}

