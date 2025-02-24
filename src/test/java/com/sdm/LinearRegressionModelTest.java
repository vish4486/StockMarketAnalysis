package com.sdm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class LinearRegressionModelTest {

    private LinearRegressionModel model;

    @BeforeEach
    void setUp() {
        model = new LinearRegressionModel();
        System.out.println("\n [DEBUG] Setting up new LinearRegressionModel instance...");
    }

    @Test
    public void testTrainAndPredict() {
        System.out.println("\n [DEBUG] Running testTrainAndPredict()...");

        List<Double> trainingData = Arrays.asList(100.0, 101.0, 102.0, 103.0, 104.0);
        System.out.println(" Training Data: " + trainingData);

        model.train(trainingData);
        
        // Debugging the internal state after training
        System.out.println(" [DEBUG] Model trained. Now predicting next value...");

        double predictedPrice = model.predictNext();

        System.out.println(" [DEBUG] Predicted Next Price: " + predictedPrice);

        double expectedPrice = 105.0; // Expected next price in a linear trend
        assertEquals(expectedPrice, predictedPrice, 0.1, " ERROR: Prediction is not within expected range!");
    }

    @Test
    public void testTrainWithEmptyData() {
        System.out.println("\n [DEBUG] Running testTrainWithEmptyData()...");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> model.train(Collections.emptyList()));
        
        System.out.println(" [DEBUG] Caught Exception: " + exception.getMessage());

        assertEquals(" Training data cannot be null or empty!", exception.getMessage(), " ERROR: Exception message did not match expected!");
    }

    @Test
    public void testTrainWithNullData() {
        System.out.println("\n [DEBUG] Running testTrainWithNullData()...");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> model.train(null));
        
        System.out.println(" [DEBUG] Caught Exception: " + exception.getMessage());

        assertEquals(" Training data cannot be null or empty!", exception.getMessage(), " ERROR: Exception message did not match expected!");
    }

    @Test
    public void testTrainWithSingleValue() {
        System.out.println("\n [DEBUG] Running testTrainWithSingleValue()...");

        List<Double> trainingData = Arrays.asList(100.0);
        System.out.println("Training Data: " + trainingData);

        model.train(trainingData);
        
        // Debugging the internal state after training
        System.out.println("[DEBUG] Model trained with a single value. Now predicting next value...");

        double predictedPrice = model.predictNext();

        System.out.println("[DEBUG] Predicted Next Price: " + predictedPrice);

        assertEquals(100.0, predictedPrice, 0.1, " ERROR: Prediction should match single input value!");
    }
}
