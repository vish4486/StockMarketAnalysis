package com.sdm;
import java.util.List;
import java.util.Arrays;
import java.util.Vector;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

class ModelEvaluationTest {
    private ModelEvaluation modelEvaluation;

    @BeforeEach
    void setUp() {
        modelEvaluation = new ModelEvaluation();
    }

    @Test
    void testCalculateMSE() {
        List<Double> actual = Arrays.asList(100.0, 102.0, 104.0);
        List<Double> predicted = Arrays.asList(101.0, 103.0, 105.0);
        double mse = modelEvaluation.calculateMSE(actual, predicted);
        assertTrue(mse > 0, "MSE should be greater than zero");
    }

    @Test
    void testCalculateRSquared() {
        List<Double> actual = Arrays.asList(100.0, 102.0, 104.0);
        List<Double> predicted = Arrays.asList(101.0, 103.0, 105.0);
        double rSquared = modelEvaluation.calculateRSquared(actual, predicted);
        assertTrue(rSquared >= 0 && rSquared <= 1, "R² should be between 0 and 1");
    }

    @Test
    void testInvalidDataHandling() {
        List<Double> actual = Arrays.asList();
        List<Double> predicted = Arrays.asList();
        assertFalse(modelEvaluation.isValidData(actual, predicted), "Should return false for empty lists");
    }
}
