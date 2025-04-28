package com.sdm.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class MultiFeatureLinearRegressionModelTest {

    private MultiFeatureLinearRegressionModel model;

    @BeforeEach
    void setUp() {
        model = new MultiFeatureLinearRegressionModel();
    }

    @Test
    void train_WithValidData_ShouldTrainSuccessfully() {
    List<double[]> features = List.of(
            new double[]{1.0, 5.0},
            new double[]{2.0, 3.0},
            new double[]{3.0, 8.0},
            new double[]{4.0, 2.0},
            new double[]{5.0, 7.0}
    );
    List<Double> targets = List.of(10.0, 12.0, 20.0, 15.0, 25.0);

    model.train(features, targets);

    double[] newInput = {6.0, 9.0};
    double predicted = model.predict(newInput);

    assertFalse(Double.isNaN(predicted), "Predicted value should not be NaN after training");
    assertFalse(Double.isInfinite(predicted), "Predicted value should not be infinite after training");
    assertTrue(predicted > 0.0 && predicted < 1000.0, "Predicted value should be realistic (between 0 and 1000)");
    }
    

    @Test
    void train_UnivariateMethod_ShouldThrowUnsupportedOperationException() {
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> model.train(List.of(1.0, 2.0, 3.0)));
        assertEquals("Use train(List<double[]>, List<Double>) instead.", exception.getMessage());
    }

    @Test
    void predictNext_ShouldThrowUnsupportedOperationException() {
        Exception exception = assertThrows(UnsupportedOperationException.class, () -> model.predictNext());
        assertEquals("Use predict(double[]) with input features instead.", exception.getMessage());
    }

    @Test
    void predict_BeforeTraining_ShouldThrowIllegalStateException() {
        double[] input = {1.0, 2.0};
        Exception exception = assertThrows(IllegalStateException.class, () -> model.predict(input));
        assertEquals("Model is not trained", exception.getMessage());
    }
}
