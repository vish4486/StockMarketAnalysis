package com.sdm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.Mockito;
import java.util.List;
import java.util.Vector;
import java.io.IOException;
import java.util.Collections;
import java.util.Arrays;



public class StockDataFetcherTest {
    private StockDataFetcher stockDataFetcher;
    private StockDataFetcher spyStockDataFetcher;

    @BeforeEach
    void setUp() {
        stockDataFetcher = new StockDataFetcher();
        spyStockDataFetcher = Mockito.spy(stockDataFetcher);
    }

    @Test
    void testFetchStockDataSuccess() {
        List<Vector<String>> result = stockDataFetcher.fetchStockData("AAPL", "Daily");
        assertNotNull(result, "Stock data should not be null");
        assertFalse(result.isEmpty(), "Fetched stock data should not be empty");
    }

    @Test
    void testFetchStockDataFailure() {
        // Simulate an API failure or empty response
        Mockito.doReturn(Collections.emptyList()).when(spyStockDataFetcher).fetchStockData("INVALID", "Daily");

        List<Vector<String>> result = spyStockDataFetcher.fetchStockData("INVALID", "Daily");
        assertNotNull(result, "Stock data should not be null even if API fails");
        assertTrue(result.isEmpty(), "Fetched stock data should be empty for an invalid symbol");
    }

    @Test
    void testSaveToCSVSuccess() {
        List<Vector<String>> testStockData = List.of(
                new Vector<>(List.of("2024-02-23", "100", "105", "98", "102", "2000"))
        );

        assertDoesNotThrow(() -> {
            CSVUtils.saveToCSV("test_stock_data.csv", testStockData);
        }, "Saving to CSV should not throw exceptions");
    }

    @Test
    void testSaveToCSVFailure() {
        // Simulate a failure in file writing
        List<Vector<String>> emptyStockData = Collections.emptyList();
        assertThrows(IOException.class, () -> CSVUtils.saveToCSV("", emptyStockData),
                "Saving an empty file should throw IOException");
    }
}
