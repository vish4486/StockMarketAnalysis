package com.sdm;

import java.util.List;

public class StockPrediction {
    private final PredictionModel model;

    public StockPrediction(PredictionModel model) {
        if (model == null) {
            throw new IllegalArgumentException(" Model cannot be null!");
        }
        this.model = model;
    }

    public void trainModel(List<Double> trainingPrices) {
        if (trainingPrices == null || trainingPrices.isEmpty()) {
            throw new IllegalArgumentException(" Training data cannot be null or empty!");
        }
        model.train(trainingPrices);
    }

    public double predictFuturePrice() {
        return model.predictNext();
    }
}
