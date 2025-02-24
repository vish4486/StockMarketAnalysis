package com.sdm;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.Collections;


class PredictionModelTest {

    @Test
    void testModelInterfaceImplementation() {
        PredictionModel model = new LinearRegressionModel();
        List<Double> trainingData = Arrays.asList(100.0, 102.0, 104.0);
        
        assertDoesNotThrow(() -> model.train(trainingData), "Model training should not throw exceptions");
        double prediction = model.predictNext();
        assertTrue(prediction > 0, "Predicted value should be a valid number");
    }
}
