package com.sdm.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class MultivariatePolynomialRegressionModelTest {

    private MultivariatePolynomialRegressionModel model;

    @BeforeEach
    void setUp() {
        model = new MultivariatePolynomialRegressionModel(5); // Max degree is 5 because your system tries 2-5
    }

    @Test
    void train_WithValidData_ShouldTrainSuccessfullyAndPredictReasonably() {
        List<double[]> features = List.of(
                new double[]{1.0, 2.0},
                new double[]{2.0, 3.0},
                new double[]{3.0, 4.0},
                new double[]{4.0, 5.0},
                new double[]{5.0, 6.0},
                new double[]{6.0, 7.0},
                new double[]{7.0, 8.0},
                new double[]{8.0, 9.0},
                new double[]{9.0, 10.0},
                new double[]{10.0, 11.0}
        );
        List<Double> targets = List.of(5.0, 7.0, 9.0, 11.0, 13.0, 15.0, 17.0, 19.0, 21.0, 23.0);
    
        model.train(features, targets);
    
        double[] newInput = {11.0, 12.0};
        double predicted = model.predict(newInput);
    
        assertFalse(Double.isNaN(predicted), "Predicted value should not be NaN");
        assertFalse(Double.isInfinite(predicted), "Predicted value should not be infinite");
    }
    


    @Test
    void predict_BeforeTraining_ShouldThrowIllegalStateException() {
        double[] input = {1.0, 2.0};
        Exception exception = assertThrows(IllegalStateException.class, () -> model.predict(input));
        assertEquals("Model not trained", exception.getMessage());
    }

    @Test
    void train_WithEmptyData_ShouldThrowException() {
        List<double[]> emptyFeatures = Collections.emptyList();
        List<Double> emptyTargets = Collections.emptyList();

        assertThrows(IllegalArgumentException.class, () -> model.train(emptyFeatures, emptyTargets));
    }

    @Test
    void train_WithNullData_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> model.train(null, null));
    }
}
