package com.sdm.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Vector;

class StockDataFetcherTest {

    private StockDataFetcher fetcher;

    @BeforeEach
    void setup() {
        fetcher = new StockDataFetcher();
    }

    //  INTEGRATION TESTS (Real API – tagged with "integration")

    @Test
    @Tag("integration")
    void testSymbolListFetchReal() {
        List<String> symbols = StockDataFetcher.getStockSymbolList();

        assertNotNull(symbols, "Symbol list should not be null");
        assertFalse(symbols.isEmpty(), "Symbol list should not be empty");
        System.out.println(" Symbols fetched: " + symbols.size());
    }

    @Test
    @Tag("integration")
    void testSymbolMappingReal() {
        List<String> displayList = StockDataFetcher.getStockSymbolList();
        String firstDisplay = displayList.get(0);
        String actualSymbol = StockDataFetcher.getSymbolFromSelection(firstDisplay);

        assertNotNull(actualSymbol);
        System.out.println(" Display ➝ Symbol: " + firstDisplay + " ➝ " + actualSymbol);
    }

    @Test
    @Tag("integration")
    void testFetchStockDataReal() {
        List<Vector<String>> stockData = fetcher.fetchStockData("AAPL", "Daily");

        assertNotNull(stockData, "Fetched data should not be null");
        assertFalse(stockData.isEmpty(), "Fetched data should not be empty");

        System.out.println(" Fetched rows: " + stockData.size());
        System.out.println(" First Row: " + stockData.get(0));
    }

    @Test
    @Tag("integration")
    void testScaledFeatureExtractionReal() {
        fetcher.fetchStockData("AAPL", "Daily");

        List<double[]> trainX = fetcher.getScaledTrainFeatures();
        List<double[]> testX = fetcher.getScaledTestFeatures();
        List<Double> trainY = fetcher.getTrainTargets();
        List<Double> testY = fetcher.getTestTargets();
        double[] latest = fetcher.getLatestScaledFeatureVector();

        assertNotNull(trainX);
        assertNotNull(testX);
        assertNotNull(trainY);
        assertNotNull(testY);
        assertNotNull(latest);
        assertEquals(4, latest.length);

        System.out.println(" Train size: " + trainX.size());
        System.out.println(" Test size: " + testX.size());
        System.out.println(" Latest Feature: " + java.util.Arrays.toString(latest));
    }

    @Test
    @Tag("integration")
    void testTrainingPricesAvailableReal() {
        fetcher.fetchStockData("AAPL", "Daily");
        List<Double> prices = fetcher.getTrainingPrices();

        assertNotNull(prices);
        assertFalse(prices.isEmpty(), "Training prices should be available");

        System.out.println(" Training Prices: " + prices.size());
    }

    //  UNIT TEST TEMPLATE (Mocked – placeholder)

    @Test
    @Tag("unit")
    void testUnitMockedParsingBehavior() {
        // You can mock a JSON response or simulate CSV parsing behavior here
        //  Right now this is just a placeholder for your structure
        System.out.println(" Unit test placeholder: mocked parsing or config logic.");
        assertTrue(true); // dummy
    }
}
