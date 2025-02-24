package com.sdm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import java.util.List;
import java.util.Arrays;
import java.util.Vector;
import java.util.Collections;


class StockPredictionTest {
    private StockPrediction stockPrediction;
    private PredictionModel mockModel;

    @BeforeEach
    void setUp() {
        mockModel = Mockito.mock(PredictionModel.class);
        stockPrediction = new StockPrediction(mockModel);
    }

    @Test
    void testTrainModel() {
        List<Double> trainingData = Arrays.asList(100.0, 101.0, 102.0);
        assertDoesNotThrow(() -> stockPrediction.trainModel(trainingData), "Training should not throw exceptions");
    }

    @Test
    void testPredictFuturePrice() {
        Mockito.when(mockModel.predictNext()).thenReturn(105.0);
        double predictedPrice = stockPrediction.predictFuturePrice();
        assertEquals(105.0, predictedPrice, "Predicted price should match mocked model output");
    }
}
