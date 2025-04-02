package com.sdm.service;

import com.sdm.app.App;
import com.sdm.model.ModelScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
@SuppressWarnings("PMD.GuardLogStatement")
class ModelEvaluationTest {

    private ModelEvaluation evaluator;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    // Added to satisfy PMD.AtLeastOneConstructor
    public ModelEvaluationTest() {}

    @BeforeEach
    void setup() {
        evaluator = new ModelEvaluation();
    }

    @Test
    void testCalculateMsePositiveResult() {
        final List<Double> actual = List.of(100.0, 102.0, 104.0);
        final List<Double> predicted = List.of(98.0, 101.0, 107.0);

        final double mse = evaluator.calculateMSE(actual, predicted);
        LOGGER.info("MSE: " + mse);

        assertTrue(mse > 0, "MSE should be positive");
    }

    @Test
    void testCalculateRSquaredWithinValidRange() {
        final List<Double> actual = List.of(100.0, 102.0, 104.0);
        final List<Double> predicted = List.of(98.0, 101.0, 107.0);

        final double rSquared = evaluator.calculateRSquared(actual, predicted);
        LOGGER.info("R² = " + rSquared);

        assertTrue(rSquared <= 1.0, "R² should not exceed 1");
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    void testEvaluateAndReturnCreatesModelScoreWithValidInput() {
        final List<Double> actual = List.of(100.0, 102.0, 104.0, 106.0);
        final List<Double> predicted = List.of(101.0, 101.5, 104.5, 105.0);

        final ModelScore score = evaluator.evaluateAndReturn("LinearRegression", "Daily", actual, predicted);
        LOGGER.info("ModelScore: " + score);

        assertNotNull(score, "Score should not be null");
        assertEquals("LinearRegression", score.modelName, "Model name mismatch");
        assertEquals("Daily", score.timeframe, "Timeframe mismatch");
        assertTrue(score.rSquared <= 1.0, "R² should be less than or equal to 1");
        assertTrue(score.mse >= 0, "MSE should be non-negative");
    }

    @Test
    void testEvaluateAndReturnThrowsExceptionForInvalidSize() {
        final List<Double> actual = List.of(100.0, 101.0);
        final List<Double> predicted = List.of(100.0);  // Mismatched size

        assertThrows(IllegalArgumentException.class,
                () -> evaluator.evaluateAndReturn("InvalidTest", "Daily", actual, predicted),
                "Should throw exception on size mismatch");
    }
}
