package com.sdm.service;
import java.util.ArrayList;
//import java.util.Comparator;
import java.util.List;

import com.sdm.app.App;
import com.sdm.model.*;

import java.util.logging.Logger;

/**
 * Manages multiple models and selects the best one for prediction based on performance.
 */
@SuppressWarnings({"PMD.GuardLogStatement","PMD.AtLeastOneConstructor"})
public class ModelManager {
    private final List<PredictionModel> models = new ArrayList<>();
    private final List<ModelScore> lastScores = new ArrayList<>();
    private ModelScore bestScore = null;
    private double bestPrediction = -1;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    
    /**
     * Adds a model to the list, with validation to check it supports at least one training mode.
     */
    public void registerModel(final PredictionModel model) {
        if (!model.supportsUnivariate() && !model.supportsMultivariate()) {
            //System.err.println(" Warning: Model " + model.getName() + " does not support any mode!");
            LOGGER.severe("Warning: Model " + model.getName() + " does not support any mode!");
            }
            models.add(model);
            }

   
    /**
     * Trains and evaluates all available models, and returns the best predicted value.
     * It works for both univariate and multivariate models.
     */
    @SuppressWarnings({"PMD.NullAssignment","PMD.AvoidCatchingGenericException"})
    public double predictBestModel(final StockDataFetcher fetcher, final String timeframe, final ModelEvaluation evaluator) {
    resetModelState();
    double result;
    
    if (hasSufficientData(fetcher)) {
        final List<double[]> trainX = fetcher.getScaledTrainFeatures();
        final List<double[]> testX = fetcher.getScaledTestFeatures();
        final List<Double> trainY = fetcher.getTrainTargets();
        final List<Double> testY = fetcher.getTestTargets();
        final double[] latestX = fetcher.getLatestScaledFeatureVector();
        final List<Double> univariate = fetcher.getTrainingPrices();
    
        for (final PredictionModel model : models) {
            try {
                evaluateModel(model, timeframe, evaluator, trainX, trainY, testX, testY, latestX, univariate);
            } catch (Exception e) {
                LOGGER.severe(String.format("Model failed: %s | Reason: %s%n", model.getName(), e.getMessage()));
            }
        }
    
        if (bestScore == null) {
            LOGGER.severe("All models failed or returned no valid prediction.");
            result = -1;
        } else {
            result = bestPrediction;
        }
    } else {
        LOGGER.severe("Insufficient data for model evaluation.");
        result = -1;
    }
    
    return result;
    
}

    /**
     * Clears model state and reloads a fresh set of models.
     * Includes:
     * - Fixed base models (from factory)
     * - Polynomial models of varying degrees (2-5)
     */
    @SuppressWarnings("PMD.NullAssignment")
    private void resetModelState() {
    models.clear();
    lastScores.clear();
    bestScore = null;
    bestPrediction = -1;

    models.addAll(ModelFactory.getFixedModels());

    for (int degree = 2; degree <= 5; degree++) {
        models.add(new PolynomialRegressionModel(degree));
        models.add(new MultivariatePolynomialRegressionModel(degree));
    }
}

    /**
     * Simple helper to verify dataset has enough samples.
     */
    private boolean hasSufficientData(final StockDataFetcher fetcher) {
    return !(fetcher.getScaledTrainFeatures().isEmpty() || fetcher.getTestTargets().isEmpty());
}



    /**
     * Trains and evaluates a single model using the provided data.
     * It decides internally whether to use univariate or multivariate strategy.
     */
    private void evaluateModel(
        final PredictionModel model,
        final String timeframe,
        final ModelEvaluation evaluator,
        final List<double[]> trainX,
        final List<Double> trainY,
        final List<double[]> testX,
        final List<Double> testY,
        final double[] latestX,
        final List<Double> univariate
) {
    double prediction;
    final List<Double> predictedSeries = new ArrayList<>();

    if (model.supportsMultivariate()) {
        model.train(trainX, trainY);
        prediction = model.predict(latestX);

        for (final double[] testRow : testX) {
            predictedSeries.add(model.predict(testRow));
        }

    } else if (model.supportsUnivariate()) {
        model.train(univariate);
        prediction = model.predictNext();
        
        for (final Double ignored : testY) {
            predictedSeries.add(prediction);
        }
        

    } else {
        return; // Skip unsupported models
    }

    final ModelScore score = evaluator.evaluateAndReturn(model.getName(), timeframe, testY, predictedSeries);
    lastScores.add(score);

    if (bestScore == null || score.rSquared > bestScore.rSquared) {
        bestScore = score;
        bestPrediction = prediction;
    }
}


    public List<ModelScore> getLastScores() {
        return lastScores;
    }

    public ModelScore getBestScore() {
        return bestScore;
    }

    public double getBestPrediction() {
        return bestPrediction;
    }
}
