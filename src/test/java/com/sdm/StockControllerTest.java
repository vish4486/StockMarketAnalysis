package com.sdm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import javax.swing.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.awt.event.ActionEvent;
import java.util.Arrays;


class StockControllerTest {
    private StockController stockController;
    private StockDataFetcher stockDataFetcher;
    private PredictionModel predictionModel;
    private ModelEvaluation modelEvaluation;
    private ChartHandler chartHandler;

    @BeforeEach
    void setUp() {
        stockDataFetcher = Mockito.mock(StockDataFetcher.class);
        predictionModel = Mockito.mock(PredictionModel.class);
        modelEvaluation = Mockito.mock(ModelEvaluation.class);
        chartHandler = Mockito.mock(ChartHandler.class);
        
        stockController = new StockController(predictionModel);
    }

    @Test
    void testFetchStockData() {
        System.out.println("\n [StockControllerTest] Running testFetchStockData()...");

        //  Mock **120 records**, but only the latest **60** should be displayed
        List<Vector<String>> mockData = IntStream.range(1, 121)
                .mapToObj(i -> new Vector<>(List.of("2024-02-23", String.valueOf(100 + i), String.valueOf(105 + i), 
                                                    String.valueOf(98 + i), String.valueOf(102 + i), String.valueOf(2000 + i))))
                .collect(Collectors.toList());

        //  Mocking the behavior of StockDataFetcher
        Mockito.when(stockDataFetcher.fetchStockData("AAPL", "Daily")).thenReturn(mockData);

        // Call the actual method
        List<Vector<String>> result = stockController.fetchStockData("AAPL", "Daily");

        // Debugging prints
        System.out.println(" Expected Displayed Rows: 60 | Retrieved: " + result.size());
        result.forEach(row -> System.out.println(" " + row));

        //  Assert we only display the last 60 records
        assertNotNull(result, "Fetched stock data should not be null");
        assertEquals(60, result.size(), " ERROR: Expected 60 rows of stock data!");
    }

   @Test
void testPredictFuturePrice() {
    System.out.println("\n [StockControllerTest] Running testPredictFuturePrice()...");

    //  Mock realistic training data (should be 120 records, but last 60 are used)
    List<Double> mockTrainingData = Arrays.asList(100.0, 101.0, 102.0, 103.0, 104.0, 105.0);

    //  Ensure StockDataFetcher returns training prices
    Mockito.when(stockDataFetcher.getTrainingPrices()).thenReturn(mockTrainingData);

    //  Ensure `train()` method is called
    Mockito.doAnswer(invocation -> {
        System.out.println(" [Mock] Training the model with data: " + mockTrainingData);
        return null;
    }).when(predictionModel).train(mockTrainingData);

    //  Mock a valid prediction result
    Mockito.when(predictionModel.predictNext()).thenReturn(150.0);

    //  Call method under test
    double result = stockController.predictFuturePrice();

    //  Debugging output
    System.out.println("📊 Expected Prediction: 150.0 | Actual: " + result);

    //  Validate prediction
    assertNotNull(result, " ERROR: Predicted price should not be null!");
    assertTrue(result > 0, " ERROR: Predicted price should be positive!");
    assertEquals(150.0, result, " ERROR: Predicted price should be 150!");
}


    @Test
    void testEvaluateModel() {
        List<Double> actualPrices = List.of(100.0, 102.0, 104.0);
        List<Double> predictedPrices = List.of(101.0, 103.0, 105.0);

        Mockito.when(stockDataFetcher.getGridPrices()).thenReturn(actualPrices);
        Mockito.when(stockDataFetcher.getTrainingPrices()).thenReturn(actualPrices);
        Mockito.when(predictionModel.predictNext()).thenReturn(105.0);

        assertDoesNotThrow(() -> stockController.evaluateModel(), "Model evaluation should run without exceptions");
    }

    @Test
    void testShowChart() {
        assertDoesNotThrow(() -> stockController.showChart("AAPL", "Daily", new JFrame()), 
            "Chart should open without errors");
    }

    @Test
    void testSaveToCSV() {
        assertDoesNotThrow(() -> stockController.saveToCSV(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null)), 
            "Saving to CSV should not throw any errors");
    }
}
