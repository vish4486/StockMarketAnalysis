package com.sdm.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class RidgeRegressionModelTest {

    private RidgeRegressionModel model;

    @BeforeEach
    void setUp() {
        model = new RidgeRegressionModel(0.1); // Light regularization
    }

    @Test
    void train_WithValidData_ShouldTrainAndPredictSuccessfully() {
        List<double[]> features = List.of(
                new double[]{1.0, 2.0},
                new double[]{2.0, 3.0},
                new double[]{3.0, 4.0},
                new double[]{5.0, 2.0},
                new double[]{6.0, 3.0},
                new double[]{7.0, 5.0}
        );
        List<Double> targets = List.of(5.0, 6.0, 9.0, 11.0, 13.0, 17.0);

        model.train(features, targets);

        double[] newInput = {8.0, 6.0};
        double predicted = model.predict(newInput);

        assertFalse(Double.isNaN(predicted), "Predicted value should not be NaN");
        assertFalse(Double.isInfinite(predicted), "Predicted value should not be infinite");
        assertTrue(predicted > 0.0 && predicted < 100.0, "Predicted value should be a realistic positive number");
    }

    @Test
    void predict_BeforeTraining_ShouldThrowIllegalStateException() {
        double[] input = {1.0, 2.0};
        Exception exception = assertThrows(IllegalStateException.class, () -> model.predict(input));
        assertEquals("Model is not trained", exception.getMessage());
    }

    
    @Test
    void train_WithNullData_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> model.train(null, null));
    }
}
