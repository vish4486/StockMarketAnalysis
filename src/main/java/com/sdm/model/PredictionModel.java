package com.sdm.model;

import java.util.List;

public interface PredictionModel {

    default void train(List<Double> trainingPrices) {
        throw new UnsupportedOperationException("Univariate training not supported.");
    }

    default double predictNext() {
        throw new UnsupportedOperationException("Univariate prediction not supported.");
    }

    default void train(List<double[]> features, List<Double> targets) {
        throw new UnsupportedOperationException("Multivariate training not supported.");
    }

    default double predict(double[] inputFeatures) {
        throw new UnsupportedOperationException("Multivariate prediction not supported.");
    }

    default String getName() {
        return this.getClass().getSimpleName();
    }

    default boolean supportsUnivariate() { return false; }

    default boolean supportsMultivariate() { return false; }

}

