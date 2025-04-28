package com.sdm.app;

import com.sdm.controller.StockController;
import com.sdm.controller.ViewListener;
import com.sdm.model.ModelFactory;
import com.sdm.model.PredictionModel;
import com.sdm.service.StockDataFetcher;
import com.sdm.view.builder.ActionButtonPanelBuilder;
import com.sdm.view.builder.AutoCompleteHandler;
import com.sdm.view.builder.InputPanelBuilder;
import com.sdm.view.builder.TableBuilder;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serial;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Main GUI class. Acts as the View.
 * Delegates logic to StockController and only handles UI construction and events.
 */
@SuppressWarnings({"PMD.GuardLogStatement", "PMD.TooManyMethods"})
public class App extends JFrame implements ViewListener {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static final String TITLE = "Stock Data Viewer";
    private static final String[] TIMEFRAMES = {"Daily", "Weekly", "Monthly"};
    private static final String INPUT_ERROR = "Input Error";
    private static final String INFO = "Info";
    private static final String ERROR = "Error";

    private JTextField symbolField;
    private JComboBox<String> timeframeDropdown;
    private JButton fetchButton, saveButton, predictButton, evaluateButton, chartButton, refreshButton;
    private JTable table;
    private DefaultTableModel tableModel;
    private final List<String> stockSymbols;
    private final Set<String> predictedSessions = new HashSet<>();
    private final transient StockController stockController;

    public App() {
        super();
        List<PredictionModel> models = ModelFactory.getFixedModels();
        this.stockController = new StockController(models, this);
        this.stockSymbols = StockDataFetcher.getStockSymbolList();
    }

    public void initializeAppWindow() {
        setTitle(TITLE);
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new BorderLayout());

        buildComponents();
        panel.add(new InputPanelBuilder(symbolField, timeframeDropdown, fetchButton, this::fetchStockData).build(), BorderLayout.NORTH);
        panel.add(new JScrollPane(new TableBuilder().build(tableModel)), BorderLayout.CENTER);
        panel.add(new ActionButtonPanelBuilder(saveButton, predictButton, evaluateButton, chartButton, refreshButton,
                stockController::saveToCSV, this::predictFuturePrice, this::evaluateModel, this::openChart, this::refreshScreen).build(), BorderLayout.SOUTH);

        new AutoCompleteHandler(symbolField, stockSymbols).enable();
        setContentPane(panel);

        LOGGER.info("UI Initialized.");
    }

    private void buildComponents() {
        symbolField = new JTextField(10);
        timeframeDropdown = new JComboBox<>(TIMEFRAMES);
        fetchButton = new JButton("Fetch Data");

        saveButton = new JButton("Save to CSV");
        predictButton = new JButton("Predict Price");
        evaluateButton = new JButton("Evaluate Model");
        chartButton = new JButton("Chart");
        refreshButton = new JButton("Refresh Screen");

        tableModel = new DefaultTableModel(new String[]{"Date", "Open", "High", "Low", "Close", "Volume"}, 0);
        table = new JTable(tableModel);

        disableActionButtons();
    }

    private void fetchStockData(ActionEvent event) {
        final String inputText = normalizeInput(symbolField.getText());
        
        if (inputText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid stock symbol!", INPUT_ERROR, JOptionPane.WARNING_MESSAGE);
            return;
        }
    
        final String symbol;
        if (inputText.contains("-")) {
            symbol = StockDataFetcher.getSymbolFromSelection(inputText); // extract before '-'
        } else {
            symbol = inputText; // already a clean symbol
        }
    
        if (symbol == null || symbol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Could not extract symbol properly!", ERROR, JOptionPane.ERROR_MESSAGE);
            return;
        }
    
        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        final List<List<String>> stockData = stockController.fetchStockData(symbol, timeframe, tableModel);
    
        if (!stockData.isEmpty()) {
            tableModel.setRowCount(0);
            stockData.forEach(row -> tableModel.addRow(row.toArray()));
            enableActionButtons();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to fetch data!", ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }
    

    private void predictFuturePrice(ActionEvent event) {
        final String symbol = normalizeInput(symbolField.getText());
        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        stockController.predictFuturePrice(symbol, timeframe);
    }

    private void evaluateModel(ActionEvent event) {
        String timeframe = (String) timeframeDropdown.getSelectedItem();
        stockController.evaluateModel(timeframe);
    }

    private void openChart(ActionEvent event) {
        String symbol = normalizeInput(symbolField.getText());
        String timeframe = (String) timeframeDropdown.getSelectedItem();
        stockController.showChart(symbol, timeframe, this);
    }

    private void refreshScreen(ActionEvent event) {
        tableModel.setRowCount(0);
        symbolField.setText("");
        timeframeDropdown.setSelectedIndex(0);
        disableActionButtons();
        predictedSessions.clear();
        JOptionPane.showMessageDialog(this, "Screen has been refreshed!", INFO, JOptionPane.INFORMATION_MESSAGE);
    }

    private void enableActionButtons() {
        saveButton.setEnabled(true);
        predictButton.setEnabled(true);
        evaluateButton.setEnabled(true);
        chartButton.setEnabled(true);
    }

    private void disableActionButtons() {
        saveButton.setEnabled(false);
        predictButton.setEnabled(false);
        evaluateButton.setEnabled(false);
        chartButton.setEnabled(false);
    }

    private String normalizeInput(String input) {
        return input == null ? "" : input.toUpperCase(Locale.ROOT).trim();
    }

    @Override
    public void onPredictionCompleted(double predictedPrice) {
        evaluateButton.setEnabled(true);
        JOptionPane.showMessageDialog(this, "Predicted Price: $" + String.format("%.2f", predictedPrice), INFO, JOptionPane.INFORMATION_MESSAGE);
    }


    @Override
    public void onEvaluationCompleted() {
        JOptionPane.showMessageDialog(this, "Model evaluation completed.", INFO, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            App app = new App();
            app.initializeAppWindow();
            app.setVisible(true);
        });
    }
}
