package com.sdm;

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
import java.util.Map;

public class ChartHandler {
    private JFrame chartFrame;
    private boolean isChartOpen = false;
    private JFXPanel jfxPanel;
    private WebView webView;
    private WebEngine webEngine;

    private static final String TRADING_VIEW_URL = ConfigLoader.getTradingViewUrl(); //   Move URL to ConfigLoader
    private static final Map<String, String> TIMEFRAME_MAP = createTimeframeMap(); //  Replace switch with map

    public void showTradingViewChart(String symbol, String timeframe, JFrame mainAppFrame) {
        System.out.println("\n Chart button clicked | Symbol: " + symbol + " | Timeframe: " + timeframe);

        String tradingViewTimeframe = convertTimeframe(timeframe);
        String chartUrl = TRADING_VIEW_URL + "?symbol=" + symbol + "&interval=" + tradingViewTimeframe;

        if (isChartOpen && chartFrame != null) {
            updateExistingChart(chartUrl);
            return;
        }

        if (mainAppFrame != null) toggleMainFrame(mainAppFrame, false);

        createNewChartWindow(symbol, timeframe, chartUrl, mainAppFrame);
    }

    private void updateExistingChart(String chartUrl) {
        System.out.println("🔄 Chart is already open. Updating WebView...");
        chartFrame.toFront();
        Platform.runLater(() -> {
            webEngine.load(null); // Clear previous content
            webEngine.load(chartUrl); // Load new content
        });
    }

    private void createNewChartWindow(String symbol, String timeframe, String chartUrl, JFrame mainAppFrame) {
        System.out.println("🆕 Creating a new chart window...");
        isChartOpen = true;
        jfxPanel = new JFXPanel();
        chartFrame = new JFrame("TradingView Chart - " + symbol + " [" + timeframe + "]");
        chartFrame.setSize(1200, 800);
        chartFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        chartFrame.setLayout(new BorderLayout());
        chartFrame.add(jfxPanel, BorderLayout.CENTER);
        chartFrame.setVisible(true);

        Platform.setImplicitExit(false); // Ensure JavaFX thread stays alive

        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                handleChartWindowClosing(mainAppFrame);
            }
        });

        initializeWebView(chartUrl);
    }

    private void initializeWebView(String chartUrl) {
        Platform.runLater(() -> {
            try {
                webView = new WebView();
                webEngine = webView.getEngine();
                webEngine.load(chartUrl);
                jfxPanel.setScene(new Scene(new StackPane(webView)));
            } catch (Exception e) {
                System.err.println("Error initializing WebView: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private void handleChartWindowClosing(JFrame mainAppFrame) {
        System.out.println(" User manually closed chart window. Resetting state...");
        isChartOpen = false;
        chartFrame.dispose();
        chartFrame = null;

        Platform.runLater(() -> {
            if (webView != null) {
                webView.getEngine().load(null); // Clear WebView content
                webView = null;
            }
        });

        if (mainAppFrame != null) toggleMainFrame(mainAppFrame, true);
    }

    private void toggleMainFrame(JFrame mainFrame, boolean enable) {
        System.out.println(enable ? "Re-enabling main application window..." : "Disabling main application window...");
        mainFrame.setEnabled(enable);
    }

    public String convertTimeframe(String timeframe) {
        return TIMEFRAME_MAP.getOrDefault(timeframe.toLowerCase().trim(), "D");
    }

    private static Map<String, String> createTimeframeMap() {
        Map<String, String> map = new HashMap<>();
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

