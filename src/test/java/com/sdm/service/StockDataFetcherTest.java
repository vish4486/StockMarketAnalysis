package com.sdm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StockDataFetcherTest {

    private StockDataFetcher fetcher;

    private static final String INTEGRATION_TAG = "integration";

    public StockDataFetcherTest() {}

    @BeforeEach
    void setup() {
        fetcher = new StockDataFetcher();
    }

    @Test
    @Tag(INTEGRATION_TAG)
    void testSymbolListFetchReal() {
        final List<String> symbols = StockDataFetcher.getStockSymbolList();
        assertTrue(symbols != null && !symbols.isEmpty(), "Fetched symbol list should not be null or empty");
    }

    @Test
    @Tag(INTEGRATION_TAG)
    void testSymbolMappingReal() {
        final List<String> displayList = StockDataFetcher.getStockSymbolList();
        assertFalse(displayList.isEmpty(), "Display list should not be empty before mapping");
        final String firstDisplay = displayList.get(0);
        final String actualSymbol = StockDataFetcher.getSymbolFromSelection(firstDisplay);
        assertNotNull(actualSymbol, "Mapped symbol should not be null");
    }

    @Test
    @Tag(INTEGRATION_TAG)
    void testFetchStockDataReal() {
        final List<?> stockData = fetcher.fetchStockData("AAPL", "Daily");
        assertFalse(stockData.isEmpty(), "Stock data fetched should not be empty");
    }

    @Test
    @Tag(INTEGRATION_TAG)
    void testScaledFeatureExtractionReal() {
        fetcher.fetchStockData("AAPL", "Daily");

        final List<double[]> trainX = fetcher.getScaledTrainFeatures();
        final List<double[]> testX = fetcher.getScaledTestFeatures();
        final List<Double> trainY = fetcher.getTrainTargets();
        final List<Double> testY = fetcher.getTestTargets();
        final double[] latest = fetcher.getLatestScaledFeatureVector();

        assertAll("Check scaled features and targets",
            () -> assertNotNull(trainX, "Train features must not be null"),
            () -> assertNotNull(testX, "Test features must not be null"),
            () -> assertNotNull(trainY, "Train targets must not be null"),
            () -> assertNotNull(testY, "Test targets must not be null"),
            () -> assertNotNull(latest, "Latest feature vector must not be null"),
            () -> assertEquals(4, latest.length, "Feature vector length must be 4")
        );
    }

    

    @Test
    @Tag(INTEGRATION_TAG)
    void testTrainingPricesAvailableReal() {
        List<List<String>> stockData = fetcher.fetchStockData("AAPL", "Daily");
        assertFalse(stockData.isEmpty(), "Fetched stock data must not be empty, otherwise training prices can't be verified");

        final List<Double> prices = fetcher.getTrainingPrices();
        assertNotNull(prices, "Training prices list must not be null");
        assertFalse(prices.isEmpty(), "Training prices list must not be empty if stock data was fetched");
    }
       
}
