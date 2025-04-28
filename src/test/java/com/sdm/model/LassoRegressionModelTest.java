package com.sdm.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class LassoRegressionModelTest {

    private LassoRegressionModel model;

    @BeforeEach
    void setUp() {
        model = new LassoRegressionModel(0.01);
    }

    @Test
    void train_WithValidData_ShouldTrainAndPredictWithoutCrashing() {
    model = new LassoRegressionModel(0.01); // small lambda

    List<double[]> features = List.of(
            new double[]{1.0, 2.0},
            new double[]{2.0, 1.5},
            new double[]{3.0, 4.0},
            new double[]{5.0, 2.0},
            new double[]{6.0, 3.0},
            new double[]{7.0, 5.0}
    );
    List<Double> targets = List.of(5.0, 6.0, 9.0, 11.0, 13.0, 17.0);

    model.train(features, targets);

    double[] newInput = {8.0, 6.0};

    // Only check: prediction must not throw exception
    assertDoesNotThrow(() -> model.predict(newInput),
            "Prediction after training should not throw exception");  
    }


    @Test
    void predict_BeforeTraining_ShouldThrowIllegalStateException() {
        double[] input = {1.0, 2.0};
        Exception exception = assertThrows(IllegalStateException.class, () -> model.predict(input));
        assertEquals("Model is not trained", exception.getMessage());
    }

    @Test
    void train_WithEmptyData_ShouldThrowException() {
        List<double[]> emptyFeatures = Collections.emptyList();
        List<Double> emptyTargets = Collections.emptyList();

        assertThrows(IndexOutOfBoundsException.class, () -> model.train(emptyFeatures, emptyTargets));
    }

    @Test
    void train_WithNullData_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> model.train(null, null));
    }
}
