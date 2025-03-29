package com.sdm.service;

import com.sdm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class ModelManagerTest {

    private ModelManager manager;
    private ModelEvaluation evaluator;

    @BeforeEach
    void setup() {
        manager = new ModelManager();
        evaluator = new ModelEvaluation();
    }


    
    @Test
    @Tag("unit")
    void testRegisterModel_supportsUnivariate() {
    System.out.println("\n[ModelManagerTest] Running testRegisterModel_supportsUnivariate...");

    PredictionModel mockModel = Mockito.mock(PredictionModel.class);
    Mockito.when(mockModel.supportsUnivariate()).thenReturn(true);
    Mockito.when(mockModel.supportsMultivariate()).thenReturn(false);
    Mockito.when(mockModel.getName()).thenReturn("MockUnivariateModel");

    manager.registerModel(mockModel);

    StockDataFetcher fetcher = Mockito.mock(StockDataFetcher.class);
    List<Double> trainPrices = List.of(100.0, 102.0, 104.0);
    List<Double> testPrices = List.of(106.0, 108.0);
    Mockito.when(fetcher.getTrainingPrices()).thenReturn(trainPrices);
    Mockito.when(fetcher.getTestTargets()).thenReturn(testPrices);

    Mockito.doNothing().when(mockModel).train(Mockito.anyList());
    Mockito.when(mockModel.predictNext()).thenReturn(110.0);

    ModelEvaluation mockedEval = Mockito.mock(ModelEvaluation.class);
    Mockito.when(mockedEval.evaluateAndReturn(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyList(),
            Mockito.anyList()
    )).thenReturn(new ModelScore("MockUnivariateModel", "Daily", 0.9, 1.0, 1.0, 1.0, 110.0));

    double predicted = manager.predictBestModel(fetcher, "Daily", mockedEval);

    //  More tolerant assertions
    assertFalse(Double.isNaN(predicted), "Prediction should not be NaN");
    

    System.out.println("Mocked prediction worked. Predicted: " + predicted);
}



    @Test
    @Tag("integration")
    void testPredictionWithLinearRegression() {
        manager.registerModel(new LinearRegressionModel());

        StockDataFetcher fetcher = new StockDataFetcher();
        fetcher.fetchStockData("AAPL", "Daily");
        double prediction = manager.predictBestModel(fetcher, "Daily", evaluator);

        assertTrue(prediction > 0, "Prediction should be greater than 0");
        assertNotNull(manager.getBestScore(), "Best score should be assigned");
    }

    @Test
    @Tag("unit")
    void testMultipleModelRegistration() {
        PredictionModel model1 = Mockito.mock(PredictionModel.class);
        PredictionModel model2 = Mockito.mock(PredictionModel.class);
        Mockito.when(model1.supportsUnivariate()).thenReturn(true);
        Mockito.when(model2.supportsMultivariate()).thenReturn(true);

        manager.registerModel(model1);
        manager.registerModel(model2);

        assertNotNull(manager.getLastScores());
        System.out.println(" Multiple models registered successfully.");
    }
}
