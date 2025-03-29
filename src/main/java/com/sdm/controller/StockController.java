package com.sdm.controller;
import com.sdm.service.StockDataFetcher;
import com.sdm.service.ModelManager;
import com.sdm.service.ModelEvaluation;
import com.sdm.view.ChartHandler;
import com.sdm.model.PredictionModel;
//import com.sdm.model.ModelFactory;
import com.sdm.model.ModelScore;

import javax.swing.*;

//import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.awt.event.ActionEvent;


public class StockController {
    private final StockDataFetcher stockDataFetcher;
    private final ModelManager modelManager;
    private final ModelEvaluation modelEvaluation;
    private final ChartHandler chartHandler;

    public StockController(List<PredictionModel> allModels) {
        this.stockDataFetcher = new StockDataFetcher();
        this.modelManager = new ModelManager();
        this.modelEvaluation = new ModelEvaluation();
        this.chartHandler = new ChartHandler();

        for (PredictionModel model : allModels) {
            modelManager.registerModel(model);
        }
    }

    public List<Vector<String>> fetchStockData(String symbol, String timeframe) {
        return stockDataFetcher.fetchStockData(symbol, timeframe);
    }

    public double predictFuturePrice(String timeframe) {
        return modelManager.predictBestModel(stockDataFetcher, timeframe, modelEvaluation);
    }



    public void evaluateModel(String timeframe) {
        List<ModelScore> allScores = modelManager.getLastScores();

        if (allScores.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No models were evaluated. Please fetch data and predict first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder summary = new StringBuilder("Model Evaluation Summary (" + timeframe + "):\n\n");
        for (ModelScore score : allScores) {
            summary.append(score.toString()).append("\n");
        }

        JOptionPane.showMessageDialog(null, summary.toString(), "Model Performance", JOptionPane.INFORMATION_MESSAGE);

        // Prompt user for metric choice
        String[] options = {"R²", "MSE", "RMSE", "MAE", "Predicted"};
        String selectedMetric = (String) JOptionPane.showInputDialog(
            null,
            "Choose metric to visualize:",
            "Select Evaluation Metric",
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
            );

        
       if (selectedMetric != null) {

        ModelEvaluation evaluation = new ModelEvaluation();
        final Runnable[] reopenMetricDialog = new Runnable[1];

        reopenMetricDialog[0] = () -> {
            
            String metricAgain = (String) JOptionPane.showInputDialog(
                null,
                "Choose metric to visualize:",
                "Select Evaluation Metric",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                selectedMetric // Optional: preselect last
                );

        if (metricAgain != null) {
            evaluation.plotModelComparisonChart(allScores, metricAgain, reopenMetricDialog[0]); // loop
        }
        };

    evaluation.plotModelComparisonChart(allScores, selectedMetric, reopenMetricDialog[0]);
}

    }


    public void showChart(String symbol, String timeframe, JFrame parentFrame) {
        chartHandler.showTradingViewChart(symbol, timeframe, parentFrame);
    }

    public void saveToCSV(ActionEvent e) {
        stockDataFetcher.saveToCSV();
    }
} 