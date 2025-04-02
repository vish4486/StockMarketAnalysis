package com.sdm.service;

import com.sdm.app.App;
import com.sdm.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD.GuardLogStatement")
@Tag("unit")
class ModelManagerTest {

    private ModelManager manager;
    private ModelEvaluation evaluator;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private static final String DAILY = "Daily";

    // Satisfy PMD.AtLeastOneConstructor
    public ModelManagerTest() {}

    @BeforeEach
    void setup() {
        manager = new ModelManager();
        evaluator = new ModelEvaluation();
    }

    @Test
    @Tag("unit")
    void testRegisterModelSupportsUnivariate() {
        LOGGER.info("\n[ModelManagerTest] Running testRegisterModel_supportsUnivariate...");

        final PredictionModel mockModel = Mockito.mock(PredictionModel.class);
        Mockito.when(mockModel.supportsUnivariate()).thenReturn(true);
        Mockito.when(mockModel.supportsMultivariate()).thenReturn(false);
        Mockito.when(mockModel.getName()).thenReturn("MockUnivariateModel");

        manager.registerModel(mockModel);

        final StockDataFetcher fetcher = Mockito.mock(StockDataFetcher.class);
        final List<Double> trainPrices = List.of(100.0, 102.0, 104.0);
        final List<Double> testPrices = List.of(106.0, 108.0);
        Mockito.when(fetcher.getTrainingPrices()).thenReturn(trainPrices);
        Mockito.when(fetcher.getTestTargets()).thenReturn(testPrices);

        Mockito.doNothing().when(mockModel).train(Mockito.anyList());
        Mockito.when(mockModel.predictNext()).thenReturn(110.0);

        final ModelEvaluation mockedEval = Mockito.mock(ModelEvaluation.class);
        Mockito.when(mockedEval.evaluateAndReturn(
                Mockito.anyString(), Mockito.anyString(), Mockito.anyList(), Mockito.anyList()
        )).thenReturn(new ModelScore("MockUnivariateModel", DAILY, 0.9, 1.0, 1.0, 1.0, 110.0));

        final double predicted = manager.predictBestModel(fetcher, DAILY, mockedEval);

        assertFalse(Double.isNaN(predicted), "Prediction should not be NaN");
        LOGGER.info("Mocked prediction worked. Predicted: " + predicted);
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    @Tag("integration")
    void testPredictionWithLinearRegression() {
        manager.registerModel(new LinearRegressionModel());

        final StockDataFetcher fetcher = new StockDataFetcher();
        fetcher.fetchStockData("AAPL", DAILY);
        final double prediction = manager.predictBestModel(fetcher, DAILY, evaluator);

        assertTrue(prediction > 0, "Prediction should be greater than 0");
        assertNotNull(manager.getBestScore(), "Best score should be assigned");
    }

    @Test
    @Tag("unit")
    void testMultipleModelRegistration() {
        final PredictionModel model1 = Mockito.mock(PredictionModel.class);
        final PredictionModel model2 = Mockito.mock(PredictionModel.class);
        Mockito.when(model1.supportsUnivariate()).thenReturn(true);
        Mockito.when(model2.supportsMultivariate()).thenReturn(true);

        manager.registerModel(model1);
        manager.registerModel(model2);

        assertNotNull(manager.getLastScores(), "Last scores should not be null");
        LOGGER.info("Multiple models registered successfully.");
    }
}
