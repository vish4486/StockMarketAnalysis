package com.sdm.model;

import com.sdm.app.App;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@SuppressWarnings({"PMD.GuardLogStatement"})
class LinearRegressionModelTest {

    private LinearRegressionModel model;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    // Explicit constructor to satisfy PMD.AtLeastOneConstructor
    public LinearRegressionModelTest() {}

    @BeforeEach
    void setUp() {
        model = new LinearRegressionModel();
        LOGGER.info("[DEBUG] Setting up new LinearRegressionModel instance...");
    }

    @Test
    void testTrainAndPredict() {
        LOGGER.info("[DEBUG] Running testTrainAndPredict()...");

        final List<Double> trainingData = Arrays.asList(100.0, 101.0, 102.0, 103.0, 104.0);
        LOGGER.info("Training Data: " + trainingData);

        model.train(trainingData);
        final double predictedPrice = model.predictNext();
        final double expectedPrice = 105.0;

        LOGGER.info("[DEBUG] Predicted Next Price: " + predictedPrice);
        assertEquals(expectedPrice, predictedPrice, 0.1, "Prediction is not within expected range!");
    }

    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    @Test
    void testTrainWithEmptyData() {
        LOGGER.info("[DEBUG] Running testTrainWithEmptyData()...");

        final Exception exception = assertThrows(IllegalArgumentException.class,
                () -> model.train(Collections.emptyList()));
        LOGGER.info("[DEBUG] Caught Exception: " + exception.getMessage());

        assertEquals("Training data cannot be null or empty!", exception.getMessage(),
                "Exception message did not match expected!");
    }

    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    @Test
    void testTrainWithNullData() {
        LOGGER.info("[DEBUG] Running testTrainWithNullData()...");

        final Exception exception = assertThrows(IllegalArgumentException.class,
                () -> model.train(null));
        LOGGER.info("[DEBUG] Caught Exception: " + exception.getMessage());

        assertEquals("Training data cannot be null or empty!", exception.getMessage(),
                "Exception message did not match expected!");
    }

    @Test
    void testTrainWithSingleValue() {
        LOGGER.info("[DEBUG] Running testTrainWithSingleValue()...");

        final List<Double> trainingData = List.of(100.0);
        LOGGER.info("Training Data: " + trainingData);

        model.train(trainingData);
        final double predictedPrice = model.predictNext();

        LOGGER.info("[DEBUG] Predicted Next Price: " + predictedPrice);
        assertEquals(100.0, predictedPrice, 0.1, "Prediction should match single input value!");
    }
}
