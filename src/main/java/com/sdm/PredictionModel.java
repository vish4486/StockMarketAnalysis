package com.sdm;

import java.util.List;

public interface PredictionModel {
    void train(List<Double> trainingPrices);
    double predictNext();
}
