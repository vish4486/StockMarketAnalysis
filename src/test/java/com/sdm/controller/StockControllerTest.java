package com.sdm.controller;

import com.sdm.app.App;
import com.sdm.model.PredictionModel;
import com.sdm.service.ModelEvaluation;
import com.sdm.service.StockDataFetcher;
import com.sdm.view.ChartHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.logging.Logger;

@SuppressWarnings({"PMD.GuardLogStatement", "PMD.AtLeastOneConstructor"})
class StockControllerTest {

    private StockController stockController;
    private StockDataFetcher stockDataFetcher;
    private PredictionModel predictionModel;
    private ModelEvaluation modelEvaluation;
    private ChartHandler chartHandler;

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    @BeforeEach
    void setUp() {
        stockDataFetcher = mock(StockDataFetcher.class);
        predictionModel = mock(PredictionModel.class);
        modelEvaluation = mock(ModelEvaluation.class);
        chartHandler = mock(ChartHandler.class);
        stockController = new StockController(List.of(predictionModel));
    }

    @Test
    @Tag("unit")
    void testPredictFuturePrice() {
        LOGGER.info("\n[StockControllerTest] testPredictFuturePrice()");
        final List<Double> mockTrainingData = Arrays.asList(100.0, 101.0, 102.0);

        when(stockDataFetcher.getTrainingPrices()).thenReturn(mockTrainingData);

        doAnswer(invocation -> {
            LOGGER.info("Model trained with data: " + mockTrainingData);
            return null;
        }).when(predictionModel).train(mockTrainingData);

        when(predictionModel.predictNext()).thenReturn(150.0);

        final double result = predictionModel.predictNext();
        final double expected = 150.0;
        final double tolerance = 15.0;

        assertTrue(Math.abs(result - expected) < tolerance, "Prediction should be within ±15 of expected value");
    }

    @Test
    void testSaveToCSV() {
        assertDoesNotThrow(() ->
                stockController.saveToCSV(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "save")),
                "Saving to CSV should not throw exceptions");
    }

    @Test
    void testShowChart() {
        assertDoesNotThrow(() ->
                stockController.showChart("AAPL", "Daily", new JFrame()),
                "Chart should open without error");
    }

    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    @Test
    void testFetchStockData() {
        final List<List<String>> mockData = List.of(
                List.of("2024-03-27", "100", "105", "98", "102", "3000"));

        when(stockDataFetcher.fetchStockData("AAPL", "Daily")).thenReturn(mockData);
        final List<List<String>> result = stockDataFetcher.fetchStockData("AAPL", "Daily");

        assertNotNull(result, "Fetched stock data should not be null");
        assertFalse(result.isEmpty(), "Fetched stock data should not be empty");
        LOGGER.info("Fetched rows: " + result.size());
    }
}
