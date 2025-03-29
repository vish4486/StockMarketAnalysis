package com.sdm.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")  //  Marked as unit test for CI
class ChartHandlerTest {

    private ChartHandler chartHandler;

    @BeforeEach
    void setUp() {
        chartHandler = new ChartHandler();
    }

    @Test
    void testShowTradingViewChart_doesNotThrow() {
        String symbol = "AAPL";
        String timeframe = "Daily";
        JFrame frame = new JFrame();  // Dummy parent frame

        // Ensure it doesn't crash (visual inspection needed separately)
        assertDoesNotThrow(() ->
                chartHandler.showTradingViewChart(symbol, timeframe, frame),
                "Chart rendering should not throw exception"
        );
    }

    // we could expand here with additional tests in the future,
    // e.g., validating constructed URL or simulating iframe component
}
