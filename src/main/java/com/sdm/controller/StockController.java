package com.sdm.controller;

import com.sdm.model.ModelScore;
import com.sdm.model.PredictionModel;
import com.sdm.service.ModelEvaluation;
import com.sdm.service.ModelManager;
import com.sdm.service.StockDataFetcher;
import com.sdm.view.ChartHandler;

import javax.swing.*;
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

    /**
     * Initializes services and registers prediction models.
     *
     * @param allModels List of prediction models.
     */
    public StockController(final List<PredictionModel> allModels) {
        this.stockDataFetcher = new StockDataFetcher();
        this.modelManager = new ModelManager();
        this.modelEvaluation = new ModelEvaluation();
        this.chartHandler = new ChartHandler();

        allModels.forEach(modelManager::registerModel);
    }

    /**
     * Fetches stock OHLC data.
     *
     * @param symbol Stock ticker.
     * @param timeframe Time granularity.
     * @return Stock data list.
     */
    public List<List<String>> fetchStockData(final String symbol, final String timeframe) {
        return stockDataFetcher.fetchStockData(symbol, timeframe);
    }

    /**
     * Predicts the next stock price.
     *
     * @param timeframe Time granularity.
     * @return Predicted price.
     */
    public double predictFuturePrice(final String timeframe) {
        return modelManager.predictBestModel(stockDataFetcher, timeframe, modelEvaluation);
    }

    /**
     * Evaluates all models and shows results.
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
        final String selectedMetric = chooseMetricDialog();

        if (selectedMetric != null) {
            plotMetricChart(allScores, selectedMetric);
        }
    }

    /**
     * Opens TradingView chart.
     *
     * @param symbol Stock symbol.
     * @param timeframe Time granularity.
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

    // --- Dependency Injection Methods ---

    public void setStockDataFetcher(final StockDataFetcher stockDataFetcher) {
        this.stockDataFetcher = stockDataFetcher;
    }

    public void setChartHandler(final ChartHandler chartHandler) {
        this.chartHandler = chartHandler;
    }

    public void setModelManager(final ModelManager modelManager) {
        this.modelManager = modelManager;
    }

    // --- Private Helpers ---

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
