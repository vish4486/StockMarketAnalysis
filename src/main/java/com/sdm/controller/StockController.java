package com.sdm.controller;

import com.sdm.model.ModelScore;
import com.sdm.model.PredictionModel;
import com.sdm.service.ModelEvaluation;
import com.sdm.service.ModelManager;
import com.sdm.service.StockDataFetcher;
import com.sdm.view.ChartHandler;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * Coordinates between UI and backend services:
 * - Fetches stock data
 * - Triggers predictions
 * - Evaluates model performance
 * - Delegates chart rendering and file saving
 */
public class StockController {

    private StockDataFetcher stockDataFetcher;
    private ModelManager modelManager;
    private final ModelEvaluation modelEvaluation;
    private ChartHandler chartHandler;
    private final ViewListener viewListener;

    /**
     * Initializes services and registers prediction models.
     *
     * @param allModels    List of prediction models.
     * @param viewListener Listener to update the view after operations.
     */
    public StockController(final List<PredictionModel> allModels, final ViewListener viewListener) {
        this.stockDataFetcher = new StockDataFetcher();
        this.modelManager = new ModelManager();
        this.modelEvaluation = new ModelEvaluation();
        this.chartHandler = new ChartHandler();
        this.viewListener = viewListener;

        allModels.forEach(modelManager::registerModel);
    }

    /**
     * Fetches stock OHLC data.
     *
     * @param symbol    Stock ticker.
     * @param timeframe Time granularity.
     * @param tableModel Table model to populate.
     * @return Stock data list.
     */
    public List<List<String>> fetchStockData(final String symbol, final String timeframe, final DefaultTableModel tableModel) {
        List<List<String>> stockData = stockDataFetcher.fetchStockData(symbol, timeframe);
        tableModel.setRowCount(0);

        for (List<String> row : stockData) {
            tableModel.addRow(row.toArray());
        }
        return stockData;
    }

    /**
     * Predicts the next stock price and notifies the view.
     *
     * @param timeframe Time granularity.
     */
    public void predictFuturePrice(final String symbol, final String timeframe) {
        double predictedPrice = modelManager.predictBestModel(stockDataFetcher, timeframe, modelEvaluation);
        if (viewListener != null) {
            viewListener.onPredictionCompleted(predictedPrice);
        }
    }
    

    /**
     * Evaluates all models, shows results, and notifies the view.
     *
     * @param timeframe Context timeframe.
     */
    public void evaluateModel(final String timeframe) {
        final List<ModelScore> allScores = modelManager.getLastScores();

        if (allScores.isEmpty()) {
            showInfoDialog("No models were evaluated. Please fetch data and predict first.", "Info");
            return;
        }

        showEvaluationSummary(allScores, timeframe);
        viewListener.onEvaluationCompleted(); // Notify App after evaluation

        final String selectedMetric = chooseMetricDialog();
        if (selectedMetric != null) {
            plotMetricChart(allScores, selectedMetric);
        }
    }

    /**
     * Opens TradingView chart.
     *
     * @param symbol      Stock symbol.
     * @param timeframe   Time granularity.
     * @param parentFrame Parent window.
     */
    public void showChart(final String symbol, final String timeframe, final JFrame parentFrame) {
        chartHandler.showTradingViewChart(symbol, timeframe, parentFrame);
    }

    /**
     * Saves stock data to CSV.
     *
     * @param event Trigger event.
     */
    public void saveToCSV(final ActionEvent event) {
        stockDataFetcher.saveToCSV();
    }

    // --- Dependency Injection Methods (if needed) ---

    public void setStockDataFetcher(final StockDataFetcher stockDataFetcher) {
        this.stockDataFetcher = stockDataFetcher;
    }

    public void setChartHandler(final ChartHandler chartHandler) {
        this.chartHandler = chartHandler;
    }

    public void setModelManager(final ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    // --- Private Helper Methods ---

    private void showEvaluationSummary(final List<ModelScore> scores, final String timeframe) {
        final StringBuilder summary = new StringBuilder("Model Evaluation Summary (" + timeframe + "):\n\n");
        scores.forEach(score -> summary.append(score.toString()).append("\n"));
        showInfoDialog(summary.toString(), "Model Performance");
    }

    private String chooseMetricDialog() {
        final String[] options = {"R²", "MSE", "RMSE", "MAE", "Predicted"};
        return (String) JOptionPane.showInputDialog(
                null,
                "Choose metric to visualize:",
                "Select Evaluation Metric",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );
    }

    private void plotMetricChart(final List<ModelScore> scores, final String selectedMetric) {
        modelEvaluation.plotModelComparisonChart(scores, selectedMetric, () -> {
            final String metricAgain = chooseMetricDialog();
            if (metricAgain != null) {
                plotMetricChart(scores, metricAgain);
            }
        });
    }

    private void showInfoDialog(final String message, final String title) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
