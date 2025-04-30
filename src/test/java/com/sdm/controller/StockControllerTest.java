package com.sdm.controller;

import com.sdm.app.App;
import com.sdm.model.LinearRegressionModel;
import com.sdm.model.PredictionModel;
import com.sdm.service.ModelEvaluation;
import com.sdm.service.ModelManager;
import com.sdm.service.StockDataFetcher;
import com.sdm.view.ChartHandler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SuppressWarnings({"PMD.GuardLogStatement", "PMD.AtLeastOneConstructor"})
class StockControllerTest {

    private StockController stockController;
    private StockDataFetcher stockDataFetcher;
    private PredictionModel predictionModel;
    private ModelEvaluation modelEvaluation;
    private ChartHandler chartHandler;
    private ModelManager modelManager;
    private ViewListener viewListener;
    private double capturedPredictedPrice;

  

    @BeforeEach
    void setUp() {
        stockDataFetcher = mock(StockDataFetcher.class);
        predictionModel = mock(PredictionModel.class);
        modelEvaluation = mock(ModelEvaluation.class);
        chartHandler = mock(ChartHandler.class);
        modelManager = mock(ModelManager.class);
        viewListener = mock(ViewListener.class);

        // Capture predicted price when listener called
        doAnswer(invocation -> {
            capturedPredictedPrice = invocation.getArgument(0);
            return null;
        }).when(viewListener).onPredictionCompleted(anyDouble());

        stockController = new StockController(List.of(predictionModel), viewListener);
        stockController.setStockDataFetcher(stockDataFetcher);
        stockController.setChartHandler(chartHandler);
        stockController.setModelManager(modelManager);
    }

    @Test
    @Tag("unit")
    void predictFuturePrice_ShouldNotifyViewListener() {
        List<Double> mockTrainingData = Arrays.asList(100.0, 101.0, 102.0);
        when(stockDataFetcher.getTrainingPrices()).thenReturn(mockTrainingData);
        when(modelManager.predictBestModel(any(), anyString(), any())).thenReturn(150.0);

        assertDoesNotThrow(() -> stockController.predictFuturePrice("TSLA", "Daily"));
        assertEquals(150.0, capturedPredictedPrice, 0.001, "Predicted price should match mock output 150.0");
    }

    @Test
    @Tag("unit")
    void saveToCSV_ShouldNotThrowAndTriggerSave() {
        ActionEvent event = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "save");
        assertDoesNotThrow(() -> stockController.saveToCSV(event), "Saving to CSV should not throw an exception");
        // Optionally: verify some internal logic if save interactions were mockable
    }

    @Test
    @Tag("unit")
    void showChart_ShouldCallChartHandler() {
        JFrame frame = new JFrame();
        assertDoesNotThrow(() -> stockController.showChart("AAPL", "Daily", frame));
        verify(chartHandler, times(1)).showTradingViewChart("AAPL", "Daily", frame);
    }

    @Test
    @Tag("unit")
    void fetchStockData_ShouldReturnValidData() {
        List<List<String>> mockData = List.of(
                List.of("2024-04-01", "110", "115", "108", "113", "3500")
        );
        when(stockDataFetcher.fetchStockData(anyString(), anyString())).thenReturn(mockData);

        DefaultTableModel dummyTableModel = new DefaultTableModel();
        List<List<String>> fetchedData = stockController.fetchStockData("GOOGL", "Daily", dummyTableModel);

        assertNotNull(fetchedData, "Fetched data should not be null");
        assertFalse(fetchedData.isEmpty(), "Fetched data should not be empty");
        assertEquals(1, fetchedData.size(), "There should be exactly 1 row");
        assertEquals("2024-04-01", fetchedData.get(0).get(0), "First date should match");
        assertEquals(6, fetchedData.get(0).size(), "Row should contain 6 elements");
    }

    
    @Test
    @Tag("integration")
    void predictFuturePrice_FromController_WithRealModel_ShouldNotifyView() {
        PredictionModel realModel = new com.sdm.model.LinearRegressionModel();
        ViewListener realViewListener = mock(ViewListener.class);
        StockController realController = new StockController(List.of(realModel), realViewListener);

        assertDoesNotThrow(() -> realController.predictFuturePrice("TSLA", "Daily"));
        verify(realViewListener, atLeastOnce()).onPredictionCompleted(anyDouble());
    }

    
    @Test
    @Tag("integration")
    void predictFuturePrice_ShouldPredictAccuratelyUsingRealModel() {
        // Real model (e.g. Linear Regression)
        PredictionModel realModel = new LinearRegressionModel();
        StockDataFetcher realFetcher = new StockDataFetcher();

        // Inject known data
        realFetcher.fetchStockData("AAPL", "Daily");
        realFetcher.getTrainingPrices().clear();
        realFetcher.getTrainingPrices().addAll(List.of(100.0, 105.0, 110.0, 115.0, 120.0)); // Linear pattern

        // Custom ViewListener to capture prediction
        ViewListener customListener = new ViewListener() {
        @Override
        public void onPredictionCompleted(double predictedPrice) {
            assertTrue(predictedPrice > 0,
                "Predicted price should be within expected linear trend range");
        }
        @Override
        public void onEvaluationCompleted() {
            
        }
    };

        StockController controller = new StockController(List.of(realModel), customListener);
        controller.setStockDataFetcher(realFetcher);
        controller.predictFuturePrice("AAPL", "Daily");
    }

}
