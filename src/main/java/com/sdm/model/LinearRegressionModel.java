package com.sdm.model;

import java.util.List;
import java.util.stream.IntStream;
import java.util.logging.Logger;

@SuppressWarnings("PMD.GuardLogStatement")
public class LinearRegressionModel implements PredictionModel {
    private static final Logger LOGGER = Logger.getLogger(LinearRegressionModel.class.getName());
    
    private static final int MINIMUM_DATA_SIZE = 1;
    private static final double DEFAULT_SLOPE = 0.0;
    private static final double DEFAULT_INTERCEPT = 0.0;

    private double slope;
    private double intercept;
    private int trainingSize; // Store only the size

    // Constructor added
    public LinearRegressionModel() {
        this.slope = DEFAULT_SLOPE;
        this.intercept = DEFAULT_INTERCEPT;
        this.trainingSize = 0;
    }

    @Override
    public void train(final List<Double> trainingPrices) {
        if (trainingPrices == null || trainingPrices.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty!");
        }

        trainingSize = trainingPrices.size(); // Save size
        LOGGER.info("Training started with " + trainingSize + " data points.");

        if (trainingSize == MINIMUM_DATA_SIZE) {
            // Special case: Only one data point, no slope
            slope = DEFAULT_SLOPE;
            intercept = trainingPrices.get(0);
            return;
        }

        // Generate X values (0, 1, 2, ..., trainingSize - 1)
        final List<Integer> xValues = IntStream.range(0, trainingSize).boxed().toList();

        // Calculate regression
        calculateRegression(xValues, trainingPrices);
        LOGGER.info("Training completed. Slope: " + slope + ", Intercept: " + intercept);

    }

    @Override
    public double predictNext() {
        if (slope == DEFAULT_SLOPE && intercept == DEFAULT_INTERCEPT) {
        throw new IllegalStateException("ERROR: Model is not trained properly.");
    }

    final double prediction;
    if (trainingSize == MINIMUM_DATA_SIZE) {
        LOGGER.info("Only one data point, returning same value: " + intercept);
        prediction = intercept;
    } else {
        final int nextIndex = trainingSize;
        prediction = slope * nextIndex + intercept;
        LOGGER.info("Predicted Next Value: " + prediction);
    }

    return prediction;
}


    private void calculateRegression(final List<Integer> inputIndices, final List<Double> priceValues) {
        final int count = inputIndices.size();
        final double sumX = inputIndices.stream().mapToDouble(Integer::doubleValue).sum();
        final double sumY = priceValues.stream().mapToDouble(Double::doubleValue).sum();
        final double sumXY = IntStream.range(0, count).mapToDouble(i -> inputIndices.get(i) * priceValues.get(i)).sum();
        final double sumX2 = inputIndices.stream().mapToDouble(i -> i * i).sum();

        // Avoid division by zero
        final double denominator = (count * sumX2 - sumX * sumX);
        if (denominator == 0) {
            slope = DEFAULT_SLOPE;
            intercept = priceValues.get(0); // Return first value
            return;
        }

        slope = (count * sumXY - sumX * sumY) / denominator;
        intercept = (sumY - slope * sumX) / count;
    }

    @Override
    public boolean supportsUnivariate() {
        return true;
    }
}
