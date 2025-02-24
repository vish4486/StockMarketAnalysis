package com.sdm;

import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.awt.event.ActionEvent;


public class StockController {
    private final StockDataFetcher stockDataFetcher;
    private final PredictionModel predictionModel;
    private final ModelEvaluation modelEvaluation;
    private final ChartHandler chartHandler;

    public StockController(PredictionModel predictionModel) {
        this.stockDataFetcher = new StockDataFetcher();
        this.predictionModel = predictionModel; //  for supporting ANY ML Model!
        this.modelEvaluation = new ModelEvaluation();
        this.chartHandler = new ChartHandler();
    }

    public List<Vector<String>> fetchStockData(String symbol, String timeframe) {
        return stockDataFetcher.fetchStockData(symbol, timeframe);
    }

    public double predictFuturePrice() {
        List<Double> trainingData = stockDataFetcher.getTrainingPrices();

        if (trainingData.isEmpty()) {
            JOptionPane.showMessageDialog(null, " Not enough data for prediction!", "Error", JOptionPane.ERROR_MESSAGE);
            return -1; // Error case
        }

        predictionModel.train(trainingData);
        return predictionModel.predictNext();
    }

    public void evaluateModel() {
        List<Double> actualPrices = stockDataFetcher.getGridPrices();
        List<Double> trainingData = stockDataFetcher.getTrainingPrices();

        if (trainingData.isEmpty() || actualPrices.isEmpty()) {
            JOptionPane.showMessageDialog(null, " Not enough data to evaluate the model!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        predictionModel.train(trainingData);
        /*List<Double> predictedPrices = actualPrices.stream()
                .map(price -> predictionModel.predictNext()) //  Uses Generic Prediction Model!
                .toList();*/

         List<Double> predictedPrices = new ArrayList<>();
        for (int i = 0; i < actualPrices.size(); i++) {
             predictedPrices.add(predictionModel.predictNext()); //  No unused lambda warning
                  }

        if (actualPrices.size() != predictedPrices.size()) {
            JOptionPane.showMessageDialog(null, " Error: Actual and predicted lists must have the same length!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        modelEvaluation.evaluate(actualPrices, predictedPrices);
    }

    public void showChart(String symbol, String timeframe, JFrame parentFrame) {
        chartHandler.showTradingViewChart(symbol, timeframe, parentFrame);
    }

    public void saveToCSV(ActionEvent e) {
        stockDataFetcher.saveToCSV();
    }
}
