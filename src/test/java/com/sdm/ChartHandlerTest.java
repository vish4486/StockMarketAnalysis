package com.sdm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import java.util.List;
import java.util.Arrays;
import java.util.Vector;
import java.util.Collections;



class ChartHandlerTest {
    private ChartHandler chartHandler;
    private JFrame mockFrame;

    @BeforeEach
    void setUp() {
        chartHandler = new ChartHandler();
        mockFrame = new JFrame();
    }

    @Test
    void testShowTradingViewChart() {
        assertDoesNotThrow(() -> chartHandler.showTradingViewChart("AAPL", "Daily", mockFrame), 
            "Chart should open without errors");
    }

    @Test
    void testConvertTimeframe() {
        assertEquals("D", chartHandler.convertTimeframe("daily"), "Should return 'D' for 'daily'");
        assertEquals("W", chartHandler.convertTimeframe("weekly"), "Should return 'W' for 'weekly'");
        assertEquals("M", chartHandler.convertTimeframe("monthly"), "Should return 'M' for 'monthly'");
        assertEquals("D", chartHandler.convertTimeframe("unknown"), "Should default to 'D'");
    }
}
