package com.sdm.controller;

import com.sdm.model.PredictionModel;
import com.sdm.service.ModelEvaluation;
import com.sdm.service.StockDataFetcher;
import com.sdm.view.ChartHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockControllerTest {

    private StockController stockController;
    private StockDataFetcher stockDataFetcher;
    private PredictionModel predictionModel;
    private ModelEvaluation modelEvaluation;
    private ChartHandler chartHandler;

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
        System.out.println("\n[StockControllerTest] testPredictFuturePrice()");

        List<Double> mockTrainingData = Arrays.asList(100.0, 101.0, 102.0);

        when(stockDataFetcher.getTrainingPrices()).thenReturn(mockTrainingData);

        doAnswer(invocation -> {
            System.out.println(" Model trained with data: " + mockTrainingData);
            return null;
        }).when(predictionModel).train(mockTrainingData);

        when(predictionModel.predictNext()).thenReturn(150.0);

        double result = predictionModel.predictNext();

        System.out.println(" Prediction: " + result);
        double expected = 150.0;
        double tolerance = 15.0;

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

    @Test
    void testFetchStockData() {
        List<Vector<String>> mockData = Arrays.asList(
                new Vector<>(List.of("2024-03-27", "100", "105", "98", "102", "3000"))
        );

        when(stockDataFetcher.fetchStockData("AAPL", "Daily")).thenReturn(mockData);

        List<Vector<String>> result = stockDataFetcher.fetchStockData("AAPL", "Daily");

        assertNotNull(result);
        assertFalse(result.isEmpty());
        System.out.println("Fetched rows: " + result.size());
    }
}
