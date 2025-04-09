package com.sdm.model;

import java.util.List;
import java.util.stream.IntStream;
import java.util.logging.Logger;



/**
 * A very basic univariate linear regression model.
 * 
 * this  fits a straight line (y = mx + b) to a given set of prices and can
 * predict the next value based on the learned slope and intercept.
 */
@SuppressWarnings("PMD.GuardLogStatement")
public class LinearRegressionModel implements PredictionModel {
    private static final Logger LOGGER = Logger.getLogger(LinearRegressionModel.class.getName());
    
    private static final int MINIMUM_DATA_SIZE = 1; //If training data has only 1 element, no regression is done 
     /** Default regression values used before training */
    private static final double DEFAULT_SLOPE = 0.0;
    private static final double DEFAULT_INTERCEPT = 0.0;

    private double slope;
    private double intercept;
    private int trainingSize; // Store only the size

    // Constructor added to Construct a new LinearRegressionModel with default parameters.
    public LinearRegressionModel() {
        this.slope = DEFAULT_SLOPE;
        this.intercept = DEFAULT_INTERCEPT;
        this.trainingSize = 0;
    }

    /**
     * Trains the model using a list of historical prices.
     * Applies simple linear regression to fit the best line.
     *
     * @param trainingPrices List of closing stock prices
     */
    @Override
    public void train(final List<Double> trainingPrices) {
        if (trainingPrices == null || trainingPrices.isEmpty()) {
            throw new IllegalArgumentException("Training data cannot be null or empty!");
        }

        trainingSize = trainingPrices.size(); // Save size
        LOGGER.info("Training started with " + trainingSize + " data points.");

        if (trainingSize == MINIMUM_DATA_SIZE) {
            // Special case: Only one data point, so no slope
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

    
    /**
     * to Predict the next price based on the learned slope and intercept.
     *
     * @return the predicted price for the next time step
     */
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


    /**
     * Computes the slope and intercept using least squares linear regression.
     *
     * @param inputIndices The x-axis values (e.g., 0, 1, 2,...)
     * @param priceValues  The actual y-values (closing prices)
     */
    private void calculateRegression(final List<Integer> inputIndices, final List<Double> priceValues) {
        final int count = inputIndices.size();
        final double sumX = inputIndices.stream().mapToDouble(Integer::doubleValue).sum();
        final double sumY = priceValues.stream().mapToDouble(Double::doubleValue).sum();
        final double sumXY = IntStream.range(0, count).mapToDouble(i -> inputIndices.get(i) * priceValues.get(i)).sum();
        final double sumX2 = inputIndices.stream().mapToDouble(i -> i * i).sum();

        // to avoid division by zero for denominator
        final double denominator = (count * sumX2 - sumX * sumX);
        if (denominator == 0) {
            slope = DEFAULT_SLOPE;
            intercept = priceValues.get(0); // Return first value
            return;
        }

        // to Apply least squares formulas
        slope = (count * sumXY - sumX * sumY) / denominator;
        intercept = (sumY - slope * sumX) / count;
    }

    /**
     * This model only supports univariate data.
     *
     * @return true, as it works with single-feature input
     */
    @Override
    public boolean supportsUnivariate() {
        return true;
    }
}
