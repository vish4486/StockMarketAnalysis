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

import java.awt.event.ActionEvent;


public class StockController {
    private final StockDataFetcher stockDataFetcher;
    private final ModelManager modelManager;
    private final ModelEvaluation modelEvaluation;
    private final ChartHandler chartHandler;

    public StockController(final List<PredictionModel> allModels) {
        this.stockDataFetcher = new StockDataFetcher();
        this.modelManager = new ModelManager();
        this.modelEvaluation = new ModelEvaluation();
        this.chartHandler = new ChartHandler();

        for (final PredictionModel model : allModels) {
            modelManager.registerModel(model);
        }
    }

   /*  public List<Vector<String>> fetchStockData(String symbol, String timeframe) {
        return stockDataFetcher.fetchStockData(symbol, timeframe);
    }
        */
    public List<List<String>> fetchStockData(final String symbol, final String timeframe) {
        return stockDataFetcher.fetchStockData(symbol, timeframe);
    }

    public double predictFuturePrice(final String timeframe) {
        return modelManager.predictBestModel(stockDataFetcher, timeframe, modelEvaluation);
    }



    public void evaluateModel(final String timeframe) {
        final List<ModelScore> allScores = modelManager.getLastScores();

        if (allScores.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No models were evaluated. Please fetch data and predict first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        final StringBuilder summary = new StringBuilder("Model Evaluation Summary (" + timeframe + "):\n\n");
        for (final ModelScore score : allScores) {
            summary.append(score.toString()).append("\n");
        }

        JOptionPane.showMessageDialog(null, summary.toString(), "Model Performance", JOptionPane.INFORMATION_MESSAGE);

        // Prompt user for metric choice
        final String[] options = {"R²", "MSE", "RMSE", "MAE", "Predicted"};
        final String selectedMetric = (String) JOptionPane.showInputDialog(
            null,
            "Choose metric to visualize:",
            "Select Evaluation Metric",
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
            );

        
       if (selectedMetric != null) {

        final ModelEvaluation evaluation = new ModelEvaluation();
        @SuppressWarnings("PMD.LongVariable")
        final Runnable[] reopenMetricDialog = new Runnable[1];

        reopenMetricDialog[0] = () -> {
            
            final String metricAgain = (String) JOptionPane.showInputDialog(
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


    public void showChart(final String symbol, final String timeframe, final JFrame parentFrame) {
        chartHandler.showTradingViewChart(symbol, timeframe, parentFrame);
    }

    public void saveToCSV(ActionEvent event) {
        stockDataFetcher.saveToCSV();
    }
} 