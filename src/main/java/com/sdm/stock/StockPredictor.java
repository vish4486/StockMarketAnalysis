package com.sdm.stock;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import java.util.List;

public class StockPredictor {
    public static double predictNextPrice(List<double[]> stockData, int daysAhead) {
        if (stockData.size() < 2) {
            return stockData.isEmpty() ? 0 : stockData.get(stockData.size() - 1)[3]; // Close price
        }

        SimpleRegression regression = new SimpleRegression();
        
        for (int i = 0; i < stockData.size(); i++) {
            regression.addData(i, stockData.get(i)[3]); // Close price
        }

        double prediction = regression.predict(stockData.size() + daysAhead);
        return Double.isNaN(prediction) ? stockData.get(stockData.size() - 1)[3] : prediction;
    }
}
