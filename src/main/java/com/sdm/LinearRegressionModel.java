package com.sdm;

import java.util.List;
import java.util.stream.IntStream;

public class LinearRegressionModel implements PredictionModel {
    private double slope;
    private double intercept;
    private int trainingSize; //  Store only the size

    @Override
    public void train(List<Double> trainingPrices) {
        if (trainingPrices == null || trainingPrices.isEmpty()) {
            throw new IllegalArgumentException(" Training data cannot be null or empty!");
        }

        trainingSize = trainingPrices.size(); // Save size
        System.out.println(" [LinearRegressionModel] Training started with " + trainingSize + " data points.");

        if (trainingSize == 1) {
            //  Special case: Only one data point, no slope
            slope = 0;
            intercept = trainingPrices.get(0);
            return;
        }

        // Generate X values (0, 1, 2, ..., trainingSize - 1)
        List<Integer> xValues = IntStream.range(0, trainingSize).boxed().toList();

        // Calculate regression
        calculateRegression(xValues, trainingPrices);
        System.out.println(" [LinearRegressionModel] Training completed. Slope: " + slope + ", Intercept: " + intercept);
    }

   @Override
public double predictNext() {
    if (slope == 0 && intercept == 0) {
        throw new IllegalStateException(" ERROR: Model is not trained properly.");
    }

    // 🔹 Handle case when only one data point exists
    if (trainingSize == 1) {
        System.out.println("🔹 [LinearRegressionModel] Only one data point, returning same value: " + intercept);
        return intercept;
    }

    int nextIndex = trainingSize; // Last index used for prediction
    double prediction = slope * nextIndex + intercept; //  Declare the variable

    System.out.println(" [LinearRegressionModel] Predicted Next Value: " + prediction);
    
    return prediction;
}


    private void calculateRegression(List<Integer> x, List<Double> y) {
        int n = x.size();
        double sumX = x.stream().mapToDouble(Integer::doubleValue).sum();
        double sumY = y.stream().mapToDouble(Double::doubleValue).sum();
        double sumXY = IntStream.range(0, n).mapToDouble(i -> x.get(i) * y.get(i)).sum();
        double sumX2 = x.stream().mapToDouble(i -> i * i).sum();

        // Avoid division by zero
        double denominator = (n * sumX2 - sumX * sumX);
        if (denominator == 0) {
            slope = 0;
            intercept = y.get(0); // Return first value
            return;
        }

        slope = (n * sumXY - sumX * sumY) / denominator;
        intercept = (sumY - slope * sumX) / n;
    }
}
