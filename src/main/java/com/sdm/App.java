package com.sdm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;

public class App extends JFrame {

    private static final String[] TIMEFRAMES = {"Daily", "Weekly", "Monthly"};

    private JTextField symbolField;
    private JButton fetchButton, saveButton, predictButton, evaluateButton, chartButton;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> timeframeDropdown;
    private final StockController stockController;

    public App() {
        setTitle("Stock Data Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        PredictionModel predictionModel = new LinearRegressionModel();
        stockController = new StockController(predictionModel);
        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Stock Symbol:"));
        symbolField = new JTextField(10);
        //symbolField.addActionListener(e -> handleSymbolField());
        symbolField.addActionListener(this::handleSymbolField);



        inputPanel.add(symbolField);

        timeframeDropdown = new JComboBox<>(TIMEFRAMES);
        inputPanel.add(new JLabel("Timeframe:"));
        inputPanel.add(timeframeDropdown);

        fetchButton = new JButton("Fetch Data");
        fetchButton.addActionListener(this::fetchStockData);
        inputPanel.add(fetchButton);

        panel.add(inputPanel, BorderLayout.NORTH);

        String[] columns = {"Date", "Open", "High", "Low", "Close", "Volume"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save to CSV");
        predictButton = new JButton("Predict Price");
        evaluateButton = new JButton("Evaluate Model");
        chartButton = new JButton("Chart");

        disableActionButtons();

        // Using method references (no more warnings!)
        saveButton.addActionListener(stockController::saveToCSV);
        predictButton.addActionListener(this::predictFuturePrice);
        evaluateButton.addActionListener(this::evaluateModel);
        chartButton.addActionListener(this::openChart);

        buttonPanel.add(saveButton);
        buttonPanel.add(predictButton);
        buttonPanel.add(evaluateButton);
        buttonPanel.add(chartButton);

        // Refresh Button (Separate Panel)
        JPanel refreshPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh Screen");
        refreshButton.addActionListener(e -> refreshScreen());
        refreshPanel.add(refreshButton);

        buttonPanel.add(refreshPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        System.out.println("Action panel added to main UI.");

        //panel.add(buttonPanel, BorderLayout.SOUTH);
        add(panel);
    }

    private void fetchStockData(ActionEvent event) {
        String symbol = symbolField.getText().toUpperCase().trim();
        String timeframe = (String) timeframeDropdown.getSelectedItem();

        if (symbol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a stock symbol!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<Vector<String>> stockData = stockController.fetchStockData(symbol, timeframe);

        if (!stockData.isEmpty()) {
            tableModel.setRowCount(0);
            for (Vector<String> row : stockData) {
                tableModel.addRow(row);
            }
            enableActionButtons();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to fetch data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void predictFuturePrice(ActionEvent e) {
        double predictedPrice = stockController.predictFuturePrice();
        JOptionPane.showMessageDialog(this, "Predicted Price: $" + String.format("%.2f", predictedPrice),
                "Prediction Result", JOptionPane.INFORMATION_MESSAGE);
    }

    private void evaluateModel(ActionEvent e) {
        stockController.evaluateModel();
    }

    private void openChart(ActionEvent e) {
        String symbol = symbolField.getText().toUpperCase().trim();
        String timeframe = (String) timeframeDropdown.getSelectedItem();
        stockController.showChart(symbol, timeframe, this);
    }

    private void handleSymbolField(ActionEvent e) {
        symbolField.setText(symbolField.getText().toUpperCase().trim());
    }

    private void disableActionButtons() {
        saveButton.setEnabled(false);
        predictButton.setEnabled(false);
        evaluateButton.setEnabled(false);
        chartButton.setEnabled(false);
    }

    private void enableActionButtons() {
        saveButton.setEnabled(true);
        predictButton.setEnabled(true);
        evaluateButton.setEnabled(true);
        chartButton.setEnabled(true);
    }

    private void refreshScreen() {
        System.out.println("🔄 Refreshing Screen...");

        //  Clear table data
        tableModel.setRowCount(0);

        // Reset stock symbol input
        symbolField.setText("");

        // Reset timeframe dropdown back to "Daily"
        timeframeDropdown.setSelectedIndex(0); // Sets the dropdown to the first item (Daily)

        // Disable buttons
        saveButton.setEnabled(false);
        predictButton.setEnabled(false);
        evaluateButton.setEnabled(false);
        chartButton.setEnabled(false);

        System.out.println(" Screen has been refreshed! Timeframe set to Daily.");
        JOptionPane.showMessageDialog(this, "Screen has been refreshed!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
