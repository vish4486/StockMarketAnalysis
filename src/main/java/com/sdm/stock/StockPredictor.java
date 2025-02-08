package com.sdm.stock;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.util.List;

public class StockPredictor {
    /**
     * Predicts future stock prices using Linear Regression.
     * @param stockData List of historical stock data.
     * @param daysAhead Number of days ahead to predict.
     * @return Predicted stock price.
     */
    public static double predictNextPrice(List<StockRecord> stockData, int daysAhead) {
        if (stockData.isEmpty()) {
            throw new IllegalArgumentException("No stock data available for prediction.");
        }

        SimpleRegression regression = new SimpleRegression();

        // Convert dates into numerical values for regression (using index as x-axis)
        for (int i = 0; i < stockData.size(); i++) {
            regression.addData(i, stockData.get(i).getClose());
        }

        // Predict stock price for the next 'daysAhead' days
        return regression.predict(stockData.size() + daysAhead);
    }
}

