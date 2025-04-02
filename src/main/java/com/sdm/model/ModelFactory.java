package com.sdm.model;

import java.util.ArrayList;
import java.util.List;

public final class ModelFactory {

    /*public static List<PredictionModel> getAllModels() {
        List<PredictionModel> models = new ArrayList<>();

        // To Add models here
        models.add(new LinearRegressionModel());
        models.add(new MultiFeatureLinearRegressionModel());
        models.add(new PolynomialRegressionModel(3));              // Univariate Poly
        models.add(new MultivariatePolynomialRegressionModel(3));  // Multivariate Poly with CV
        models.add(new RidgeRegressionModel(0.5));                 // Regularized
        models.add(new LassoRegressionModel(0.1));                 // Regularized

        //  In future in case we need to add models we can add here
        // models.add(new NeuralNetworkModel());
        // models.add(new XGBoostWrapper());

        return models;
    }
    */
    
    private ModelFactory() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    // Only includes models with fixed configuration
    public static List<PredictionModel> getFixedModels() {
        final List<PredictionModel> models = new ArrayList<>();

        models.add(new LinearRegressionModel());
        models.add(new MultiFeatureLinearRegressionModel());
        models.add(new RidgeRegressionModel(0.5));
        models.add(new LassoRegressionModel(0.1));

        return models;
    }
}
