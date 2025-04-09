package com.sdm.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class responsible for providing instances of prediction models.
 * This is a utility class and should not be instantiated as it is final
 */
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
    
    /**
     * Private constructor prevents instantiation of this utility class.
     * Enforces design principle that this class only contains static methods.
     */
    private ModelFactory() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    
    /**
     * Provides a fixed list of preconfigured machine learning models 
     * used for stock price prediction. These models are registered to the 
     * controller via ModelManager.
     *
     * @return List of initialized PredictionModel instances
     */
    public static List<PredictionModel> getFixedModels() {
        final List<PredictionModel> models = new ArrayList<>();

        models.add(new LinearRegressionModel());
        models.add(new MultiFeatureLinearRegressionModel());
        models.add(new RidgeRegressionModel(0.5));
        models.add(new LassoRegressionModel(0.1));

        return models;
    }
}
