package com.sdm.view;

import com.sdm.utils.ConfigLoader;

import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 * this class Handles creation and management of a JavaFX chart viewer inside a Swing JFrame,
 * displaying interactive TradingView charts.
 */
@SuppressWarnings({"PMD.GuardLogStatement","PMD.AtLeastOneConstructor"})
public class ChartHandler {
    private static final Logger LOGGER = Logger.getLogger(ChartHandler.class.getName());

     // State management for chart window
    private JFrame chartFrame;
    private boolean isChartOpen = false;

    // JavaFX-in-Swing integration
    private JFXPanel jfxPanel;
    private WebView webView;
    private WebEngine webEngine;

    // External parameters
    private static final String TRADING_VIEW_URL = ConfigLoader.getTradingViewUrl(); //   Move URL to ConfigLoader
    private static final Map<String, String> TIMEFRAME_MAP = createTimeframeMap(); //  Replace switch with map

    

    /**
     * Displays a TradingView chart for the given symbol and timeframe.
     * This creates a new JavaFX chart in a Swing JFrame.
     */
    public void showTradingViewChart(final String symbol, final String timeframe, final JFrame mainAppFrame) {
        LOGGER.info("Chart button clicked | Symbol: " + symbol + " | Timeframe: " + timeframe);

        final String intervalCode = convertTimeframe(timeframe); // Shortened name
        final String chartUrl = TRADING_VIEW_URL + "?symbol=" + symbol + "&interval=" + intervalCode;

        if (isChartOpen && chartFrame != null) {
            // Chart already open – just refresh the chart content
            updateExistingChart(chartUrl);
            return;
        }

        // Disable the main app while chart window is active
        if (mainAppFrame != null) {
            toggleMainFrame(mainAppFrame, false); // Added braces
        }

        // Create a new chart window from scratch
        createNewChartWindow(symbol, timeframe, chartUrl, mainAppFrame);
    }

    
    /**
     * Refreshes an already open chart window with a new chart URL.
     */
    private void updateExistingChart(final String chartUrl) {
        LOGGER.info("🔄 Chart is already open. Updating WebView...");
        chartFrame.toFront();
        Platform.runLater(() -> {
            webEngine.load(null); // Clear previous content
            webEngine.load(chartUrl); // Load new content
        });
    }

    
    /**
     * Creates a new JFrame and embeds a JavaFX WebView to load the TradingView chart.
     */
    private void createNewChartWindow(final String symbol, final String timeframe, final String chartUrl, final JFrame mainAppFrame) {
        LOGGER.info("🆕 Creating a new chart window...");
        isChartOpen = true;

        // Required to initialize JavaFX inside Swing
        jfxPanel = new JFXPanel();
        chartFrame = new JFrame("TradingView Chart - " + symbol + " [" + timeframe + "]");
        chartFrame.setSize(1200, 800);
        chartFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        chartFrame.setLayout(new BorderLayout());
        chartFrame.add(jfxPanel, BorderLayout.CENTER);
        chartFrame.setVisible(true);

        
        Platform.setImplicitExit(false); // Ensure JavaFX thread stays alive

         // Custom cleanup when chart window is closed
        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) { // renamed 'e' to 'event'
                handleChartWindowClosing(mainAppFrame);
            }
        });

        initializeWebView(chartUrl);
    }
    
    
    /**
     * Initializes the JavaFX WebView to embed the chart inside the Swing frame.
     */
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void initializeWebView(final String chartUrl) {
        Platform.runLater(() -> {
            try {
                webView = new WebView();
                webEngine = webView.getEngine();
                webEngine.load(chartUrl);
                jfxPanel.setScene(new Scene(new StackPane(webView)));
            } catch (RuntimeException re) {
                LOGGER.log(Level.SEVERE, "Error initializing WebView", re); // replaced printStackTrace
            }
        });
    }

    
    /**
     * Cleans up and restores the main app frame when chart window is closed.
     */
    private void handleChartWindowClosing(final JFrame mainAppFrame) {
        LOGGER.info("User manually closed chart window. Resetting state...");
        isChartOpen = false;
        chartFrame.dispose();
        chartFrame = null; //  explicit null assignment

        
        // Clear the JavaFX view
        Platform.runLater(() -> {
            if (webView != null) {
                webView.getEngine().load(null); // Clear WebView content
                webView = null; //  explicit null assignment
            }
        });

        // Re-enable the main application window
        if (mainAppFrame != null) {
            toggleMainFrame(mainAppFrame, true); // Added braces
        }
    }


    /**
     * Enables or disables the main application window to prevent interaction during chart viewing.
     */
    private void toggleMainFrame(final JFrame mainFrame, final boolean enable) {
        LOGGER.info(enable ? "Re-enabling main application window..." : "Disabling main application window...");
        mainFrame.setEnabled(enable);
    }

   
    /**
     * Converts a descriptive timeframe (e.g., "Weekly") to a TradingView-compatible code (e.g., "W").
     */
    public String convertTimeframe(final String timeframe) {
        return TIMEFRAME_MAP.getOrDefault(timeframe.toLowerCase(Locale.ROOT).trim(), "D"); // used Locale
    }

    
    /**
     * Static initializer to map human-readable timeframes to TradingView codes.for future work as well
     */
    private static Map<String, String> createTimeframeMap() {
        final Map<String, String> map = new HashMap<>();
        map.put("1min", "1");
        map.put("5min", "5");
        map.put("15min", "15");
        map.put("30min", "30");
        map.put("1hour", "60");
        map.put("4hour", "240");
        map.put("1day", "D");
        map.put("1week", "W");
        map.put("1month", "M");
        map.put("weekly", "W");
        map.put("monthly", "M");
        return map;
    }
}
