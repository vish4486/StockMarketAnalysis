package com.sdm.controller;
import com.sdm.service.StockDataFetcher;
import com.sdm.service.ModelManager;
import com.sdm.service.ModelEvaluation;
import com.sdm.view.ChartHandler;
import com.sdm.model.PredictionModel;
import com.sdm.model.ModelScore;
import javax.swing.*;
import java.util.List;
import java.awt.event.ActionEvent;

/**
 * this class acts as the main coordinator between the UI (view) and the underlying
 * business logic (services/models). 
 * 
 * Responsibilities:
 * - to fetch stock data
 * - to trigger predictions
 * - to evaluate model performance
 * - to delegate chart rendering and file saving
 */
public class StockController {

    // Services and UI handler components declaration
    private final StockDataFetcher stockDataFetcher;
    private final ModelManager modelManager;
    private final ModelEvaluation modelEvaluation;
    private final ChartHandler chartHandler;

    
    /**
     * here Constructor initializes all necessary services and registers
     * the machine learning models to the manager.
     *
     * @param allModels A list of prediction models (e.g., Linear, Ridge, etc.)
     */
    public StockController(final List<PredictionModel> allModels) {
        this.stockDataFetcher = new StockDataFetcher();
        this.modelManager = new ModelManager();
        this.modelEvaluation = new ModelEvaluation();
        this.chartHandler = new ChartHandler();

        // Register all provided models for later use
        for (final PredictionModel model : allModels) {
            modelManager.registerModel(model);
        }
    }

   /*  public List<Vector<String>> fetchStockData(String symbol, String timeframe) {
        return stockDataFetcher.fetchStockData(symbol, timeframe);
    }
        */
    /**
     * Fetch stock OHLC data for a given symbol and timeframe.
     *
     * @param symbol Stock ticker (e.g., AAPL)
     * @param timeframe Daily, Weekly, etc.
     * @return Parsed list of OHLC data to be shown in table
     */    
    public List<List<String>> fetchStockData(final String symbol, final String timeframe) {
        return stockDataFetcher.fetchStockData(symbol, timeframe);
    }

    /**
     * to Trigger prediction using the best available model for the selected timeframe.
     *
     * @param timeframe Time granularity for prediction
     * @return Predicted price as a double
     */
    public double predictFuturePrice(final String timeframe) {
        return modelManager.predictBestModel(stockDataFetcher, timeframe, modelEvaluation);
    }


    /**
     * Evaluate all models that were run last, show results in a pop-up.
     * Also, allows the user to visualize any one metric.
     *
     * @param timeframe Used for display context (e.g., "Daily")
     */
    public void evaluateModel(final String timeframe) {
        final List<ModelScore> allScores = modelManager.getLastScores();

        if (allScores.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No models were evaluated. Please fetch data and predict first.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Build evaluation result summary
        final StringBuilder summary = new StringBuilder("Model Evaluation Summary (" + timeframe + "):\n\n");
        for (final ModelScore score : allScores) {
            summary.append(score.toString()).append("\n");
        }

        // Display model metrics
        JOptionPane.showMessageDialog(null, summary.toString(), "Model Performance", JOptionPane.INFORMATION_MESSAGE);

        // allow user for metric choice
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

        // Recursive setup to allow re-opening metric selection
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

        // Show initial metric chart
        evaluation.plotModelComparisonChart(allScores, selectedMetric, reopenMetricDialog[0]);
}

    }


    /**
     * to delegate to chart handler to open TradingView iframe with selected symbol.
     *
     * @param symbol       Stock ticker
     * @param timeframe    Daily, Weekly, etc.
     * @param parentFrame  Needed for positioning the popup
     */
    public void showChart(final String symbol, final String timeframe, final JFrame parentFrame) {
        chartHandler.showTradingViewChart(symbol, timeframe, parentFrame);
    }

    /**
     * to save last fetched stock data to CSV file.
     *
     * @param event ActionEvent (from UI, not used here)
     */
    public void saveToCSV(ActionEvent event) {
        stockDataFetcher.saveToCSV();
    }
} 