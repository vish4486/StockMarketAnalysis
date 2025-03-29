package com.sdm.service;
import java.util.ArrayList;
//import java.util.Comparator;
import java.util.List;
import com.sdm.model.*;
import com.sdm.service.ModelEvaluation;
import com.sdm.service.StockDataFetcher;

/**
 * Manages multiple models and selects the best one for prediction based on performance.
 */
public class ModelManager {
    private final List<PredictionModel> models = new ArrayList<>();
    private final List<ModelScore> lastScores = new ArrayList<>();
    private ModelScore bestScore = null;
    private double bestPrediction = -1;

    public void registerModel(PredictionModel model) {
        if (!model.supportsUnivariate() && !model.supportsMultivariate()) {
            System.err.println(" Warning: Model " + model.getName() + " does not support any mode!");
            }
            models.add(model);
            }

   

   public double predictBestModel(StockDataFetcher fetcher, String timeframe, ModelEvaluation evaluator) {
        models.clear();
        lastScores.clear();
        bestScore = null;
        bestPrediction = -1;

        models.addAll(ModelFactory.getFixedModels());

        for (int degree = 2; degree <= 5; degree++) {
            models.add(new PolynomialRegressionModel(degree));
            models.add(new MultivariatePolynomialRegressionModel(degree));
        }

        List<double[]> trainX = fetcher.getScaledTrainFeatures();
        List<double[]> testX = fetcher.getScaledTestFeatures();
        List<Double> trainY = fetcher.getTrainTargets();
        List<Double> testY = fetcher.getTestTargets();
        double[] latestX = fetcher.getLatestScaledFeatureVector();
        List<Double> univariate = fetcher.getTrainingPrices();

        if (trainX.isEmpty() || testY.isEmpty()) {
            System.err.println(" Insufficient data for model evaluation.");
            return -1;
        }

        for (PredictionModel model : models) {
            try {
                double prediction;

                if (model.supportsMultivariate()) {
                    model.train(trainX, trainY);
                    prediction = model.predict(latestX);

                    List<Double> predictedSeries = new ArrayList<>();
                    for (double[] testRow : testX) {
                        predictedSeries.add(model.predict(testRow));
                    }

                    ModelScore score = evaluator.evaluateAndReturn(model.getName(), timeframe, testY, predictedSeries);
                    lastScores.add(score);

                    if (bestScore == null || score.rSquared > bestScore.rSquared) {
                        bestScore = score;
                        bestPrediction = prediction;
                    }

                } else if (model.supportsUnivariate()) {
                    model.train(univariate);
                    prediction = model.predictNext();

                    List<Double> predictedSeries = new ArrayList<>();
                    for (int i = 0; i < testY.size(); i++) {
                        predictedSeries.add(prediction);
                    }

                    ModelScore score = evaluator.evaluateAndReturn(model.getName(), timeframe, testY, predictedSeries);
                    lastScores.add(score);

                    if (bestScore == null || score.rSquared > bestScore.rSquared) {
                        bestScore = score;
                        bestPrediction = prediction;
                    }
                }

            } catch (Exception e) {
                System.err.printf(" Model failed: %s | Reason: %s%n", model.getName(), e.getMessage());
            }
        }

        if (bestScore == null) {
            System.err.println("All models failed or returned no valid prediction.");
            return -1;
        }

        return bestPrediction;
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
