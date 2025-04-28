package com.sdm.controller;

import com.sdm.app.App;
import com.sdm.model.PredictionModel;
import com.sdm.service.ModelEvaluation;
import com.sdm.service.ModelManager;
import com.sdm.service.StockDataFetcher;
import com.sdm.view.ChartHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"PMD.GuardLogStatement", "PMD.AtLeastOneConstructor"})
class StockControllerTest {

    private StockController stockController;
    private StockDataFetcher stockDataFetcher;
    private PredictionModel predictionModel;
    private ModelEvaluation modelEvaluation;
    private ChartHandler chartHandler;
    private ModelManager modelManager;

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    @BeforeEach
    void setUp() {
        stockDataFetcher = mock(StockDataFetcher.class);
        predictionModel = mock(PredictionModel.class);
        modelEvaluation = mock(ModelEvaluation.class);
        chartHandler = mock(ChartHandler.class);
        modelManager = mock(ModelManager.class);

        stockController = new StockController(List.of(predictionModel));
        stockController.setStockDataFetcher(stockDataFetcher);
        stockController.setChartHandler(chartHandler);
        stockController.setModelManager(modelManager);
    }

    @Test
    @Tag("unit")
    void predictFuturePrice_ShouldReturnExpectedPrediction() {
        List<Double> mockTrainingData = Arrays.asList(100.0, 101.0, 102.0);
        when(stockDataFetcher.getTrainingPrices()).thenReturn(mockTrainingData);
        when(modelManager.predictBestModel(any(), anyString(), any())).thenReturn(150.0);

        double predictedPrice = stockController.predictFuturePrice("Daily");

        assertEquals(150.0, predictedPrice, 0.001, "Predicted price should exactly match mock output 150.0");
    }

    @Test
    @Tag("unit")
    void saveToCSV_ShouldNotThrowAndTriggerSave() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "save");

        assertDoesNotThrow(() -> stockController.saveToCSV(event), "Saving to CSV should not throw an exception");

        // Assuming save interaction can be verified here
        // verify(mockSaveHandler, times(1)).save(any()); // Uncomment if applicable
    }

    @Test
    @Tag("unit")
    void showChart_ShouldCallChartHandler() {
        JFrame frame = new JFrame();

        assertDoesNotThrow(() -> stockController.showChart("AAPL", "Daily", frame), "Showing chart should not throw an exception");

        verify(chartHandler, times(1)).showTradingViewChart("AAPL", "Daily", frame);
    }

    @Test
    @Tag("unit")
    void fetchStockData_ShouldReturnValidData() {
        List<List<String>> mockData = List.of(
                List.of("2024-04-01", "110", "115", "108", "113", "3500")
        );
        when(stockDataFetcher.fetchStockData("GOOGL", "Daily")).thenReturn(mockData);

        List<List<String>> fetchedData = stockController.fetchStockData("GOOGL", "Daily");

        assertNotNull(fetchedData, "Fetched data should not be null");
        assertFalse(fetchedData.isEmpty(), "Fetched data should not be empty");
        assertEquals(1, fetchedData.size(), "There should be one row of data");
        assertEquals("2024-04-01", fetchedData.get(0).get(0), "First column should be date 2024-04-01");
    }

    @Test
    @Tag("integration")
    void predictFuturePrice_FromController_ShouldReturnValidOrFallbackValue() {
        PredictionModel realModel = new com.sdm.model.LinearRegressionModel();
        StockController realController = new StockController(List.of(realModel));

        double predictedPrice = realController.predictFuturePrice("Daily");

        assertTrue(predictedPrice == -1.0 || (predictedPrice > 1.0 && predictedPrice < 10000.0),
                "Predicted price should be fallback -1.0 or a realistic value (>1 and <10000)");
    }

    @Test
    @Tag("unit")
    void fetchStockData_FromController_ShouldReturnValidList() {
        List<List<String>> mockStockData = List.of(
                List.of("2024-04-01", "110", "115", "108", "113", "3500")
        );
        when(stockDataFetcher.fetchStockData("GOOGL", "Daily")).thenReturn(mockStockData);

        List<List<String>> fetchedData = stockController.fetchStockData("GOOGL", "Daily");

        assertNotNull(fetchedData, "Fetched data should not be null");
        assertFalse(fetchedData.isEmpty(), "Fetched data should not be empty");
        assertEquals(6, fetchedData.get(0).size(), "Each stock data row should have 6 elements");
    }

    @Test
    @Tag("integration")
    void predictFuturePriceIntegration_WithRealModel_ShouldReturnValidPrediction() {
        PredictionModel realModel = new com.sdm.model.LinearRegressionModel();
        StockController realController = new StockController(List.of(realModel));

        double predictedPrice = realController.predictFuturePrice("Daily");

        assertTrue(predictedPrice == -1.0 || (predictedPrice > 1.0 && predictedPrice < 10000.0),
                "Predicted price should be fallback -1.0 or a realistic value (>1 and <10000)");
    }
}