package com.sdm.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class PolynomialRegressionModelTest {

    private PolynomialRegressionModel model;

    @BeforeEach
    void setUp() {
        model = new PolynomialRegressionModel(2); // degree 2 (quadratic)
    }

    @Test
    void train_WithValidData_ShouldPredictNextSuccessfully() {
        List<Double> trainingData = Arrays.asList(1.0, 4.0, 9.0, 16.0, 25.0); // Quadratic y = x^2

        model.train(trainingData);

        double predicted = model.predictNext(); // Predict at x=5

        assertFalse(Double.isNaN(predicted), "Predicted value should not be NaN");
        assertFalse(Double.isInfinite(predicted), "Predicted value should not be infinite");
        assertTrue(predicted > 0.0 && predicted < 1000.0, "Predicted value should be a realistic positive number");
    }

    @Test
    void predictNext_BeforeTraining_ShouldThrowIllegalStateException() {
        Exception exception = assertThrows(IllegalStateException.class, () -> model.predictNext());
        assertEquals("Model is not trained", exception.getMessage());
    }

    @Test
    void train_WithEmptyData_ShouldThrowException() {
        List<Double> emptyData = Collections.emptyList();
        assertThrows(Exception.class, () -> model.train(emptyData));
    }

    @Test
    void train_WithNullData_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> model.train(null));
    }
}