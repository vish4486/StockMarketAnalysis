package com.sdm.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")  //  Marked as unit test for CI
class ChartHandlerTest {

    private ChartHandler chartHandler;

    // PMD: AtLeastOneConstructor
    public ChartHandlerTest() {}

    
    @BeforeEach
    void setUp() {
        chartHandler = new ChartHandler();
    }

    @Test
    void testShowTradingViewChartDoesNotThrow() {
        final String symbol = "AAPL";
        final String timeframe = "Daily";
        final JFrame frame = new JFrame();


        // Ensure it doesn't crash (visual inspection needed separately)
        assertDoesNotThrow(() ->
                chartHandler.showTradingViewChart(symbol, timeframe, frame),
                "Chart rendering should not throw exception"
        );
    }

    // we could expand here with additional tests in the future,
    // e.g., validating constructed URL or simulating iframe component
}
