package com.sdm.app;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.Serial;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.HashSet;

import java.util.logging.Logger;

import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import com.sdm.model.PredictionModel;
import com.sdm.model.ModelFactory;
import com.sdm.controller.StockController;
import com.sdm.service.StockDataFetcher;

@SuppressWarnings({
    "PMD.GuardLogStatement",
    "PMD.TooManyMethods",
    "PMD.CognitiveComplexity",
    "PMD.LawOfDemeter",
    "PMD.UnusedFormalParameter",
    "PMD.ConfusingTernary"
})
public class App extends JFrame {

    @Serial
    private static final long serialVersionUID = 1L;
    // Java Logging utility (lightweight alternative to log4j/slf4j for small apps)
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());
    private static final String[] TIMEFRAMES = {"Daily", "Weekly", "Monthly"}; //pre-defined timeframes for stock analysis
    
    // UI Components 
    private JTextField symbolField;
    @SuppressWarnings("PMD.SingularField")
    private JButton fetchButton;
    private JButton saveButton, predictButton, evaluateButton, chartButton;
    @SuppressWarnings("PMD.SingularField")
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> timeframeDropdown;

    //Business Logic and corresponding holders
    private final transient StockController stockController;
    private final List<String> stockSymbols;
    private final Set<String> predictedSessions = new HashSet<>();
    
    // Constructor to initialize controller & model layer
    public App() {
        super();
        final List<PredictionModel> models = ModelFactory.getFixedModels();
        stockController = new StockController(models);
        stockSymbols = StockDataFetcher.getStockSymbolList();
    }

    
    //  initializer to set title, size, and build UI
    public void init() {
        setTitle("Stock Data Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeUI(); // contains setContentPane()
        }
    
    
    /**
     * Main UI composition using Java Swing.
     * it has :
     * - Input panel (symbol, timeframe)
     * - Action buttons
     * - Table for stock OHLC data
     * - Refresh button
     */
    private void initializeUI() {
        final JPanel panel = new JPanel(new BorderLayout());

        final JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Stock Symbol:"));
        symbolField = new JTextField(10);
        enableAutoComplete(symbolField);
        symbolField.addActionListener(this::handleSymbolField);

        inputPanel.add(symbolField);

        timeframeDropdown = new JComboBox<>(TIMEFRAMES);
        inputPanel.add(new JLabel("Timeframe:"));
        inputPanel.add(timeframeDropdown);

        fetchButton = new JButton("Fetch Data");
        fetchButton.addActionListener(this::fetchStockData);
        inputPanel.add(fetchButton);

        timeframeDropdown.addActionListener(event -> autoFetchOnTimeframeChange());
        panel.add(inputPanel, BorderLayout.NORTH);

        final String[] columns = {"Date", "Open", "High", "Low", "Close", "Volume"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        final JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save to CSV");
        predictButton = new JButton("Predict Price");
        evaluateButton = new JButton("Evaluate Model");
        chartButton = new JButton("Chart");

        disableActionButtons();

        saveButton.addActionListener(stockController::saveToCSV);
        predictButton.addActionListener(this::predictFuturePrice);
        evaluateButton.addActionListener(this::evaluateModel);
        chartButton.addActionListener(this::openChart);

        buttonPanel.add(saveButton);
        buttonPanel.add(predictButton);
        buttonPanel.add(evaluateButton);
        buttonPanel.add(chartButton);
        
        //Refresh panel to clean screen
        final JPanel refreshPanel = new JPanel();
        final JButton refreshButton = new JButton("Refresh Screen");
        refreshButton.addActionListener(event -> refreshScreen());
        refreshPanel.add(refreshButton);

        buttonPanel.add(refreshPanel);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(panel);
        LOGGER.info("Action panel added to main UI.");
    }

    
    // this trigger auto-fetch if timeframe changes and symbol is filled
    private void autoFetchOnTimeframeChange() {
        final String symbol = symbolField.getText().toUpperCase(Locale.ROOT).trim();
        if (symbol.isEmpty()) {
            return;
        }
        LOGGER.info("Timeframe changed, fetching new data for: " + symbol);
        fetchStockData(null);
    }

    
    /**
     * Here it attach autocomplete logic to stock input field.
     * Shows suggestions based on user typing.
     */
    private void enableAutoComplete(final JTextField textField) {
        final JPopupMenu popupMenu = new JPopupMenu();
        final JList<String> suggestionList = new JList<>();
        final JScrollPane scrollPane = new JScrollPane(suggestionList);

        popupMenu.setFocusable(false);
        popupMenu.setPopupSize(300, 150);
        popupMenu.add(scrollPane);

        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) { updateSuggestions(); }
            @Override
            public void removeUpdate(DocumentEvent event) { updateSuggestions(); }
            @Override
            public void changedUpdate(DocumentEvent event) { updateSuggestions(); }

            private void updateSuggestions() {
                SwingUtilities.invokeLater(() -> {
                    final String input = textField.getText().toUpperCase(Locale.ROOT).trim();
                    if (input.isEmpty()) {
                        popupMenu.setVisible(false);
                        return;
                    }
                    final List<String> filteredSymbols = StockDataFetcher.getStockSymbolList().stream()
                            .filter(name -> name.toUpperCase(Locale.ROOT).contains(input))
                            .limit(10).toList();
                    if (filteredSymbols.isEmpty()) {
                        popupMenu.setVisible(false);
                        return;
                    }
                    suggestionList.setListData(filteredSymbols.toArray(new String[0]));
                    suggestionList.setVisibleRowCount(Math.min(filteredSymbols.size(), 10));
                    popupMenu.show(textField, 0, textField.getHeight());
                });
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent event) {
                if (popupMenu.isVisible() && event.getKeyCode() == KeyEvent.VK_DOWN) {
                    suggestionList.setSelectedIndex(0);
                    suggestionList.requestFocus();
                }
            }
        });

        suggestionList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(final KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    final String selectedValue = suggestionList.getSelectedValue();
                    final String symbol = StockDataFetcher.getSymbolFromSelection(selectedValue);
                    if (symbol != null) {
                        final int caretPosition = textField.getCaretPosition();
                        textField.setText(symbol);
                        textField.setCaretPosition(caretPosition);
                        popupMenu.setVisible(false);
                        textField.requestFocus();
                    }
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                final String selectedValue = suggestionList.getSelectedValue();
                final String symbol = StockDataFetcher.getSymbolFromSelection(selectedValue);
                if (symbol != null) {
                    final int caretPosition = textField.getCaretPosition();
                    textField.setText(symbol);
                    textField.setCaretPosition(caretPosition);
                    popupMenu.setVisible(false);
                }
            }
        });
    }

    
    /**
     * to Fetch and populate stock data into the table.
     */
    private void fetchStockData(ActionEvent event) {
        final String symbol = symbolField.getText().toUpperCase(Locale.ROOT).trim();
        if (symbol == null || symbol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid stock symbol!", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        final List<List<String>> stockData = stockController.fetchStockData(symbol, timeframe);

        if (!stockData.isEmpty()) {
            tableModel.setRowCount(0);
            for (final List<String> row : stockData) {
                tableModel.addRow(row.toArray());
            }
            enableActionButtons();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to fetch data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    /**
     * to predict future price and then enables evaluation.
     */
    private void predictFuturePrice(ActionEvent event) {
        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        final double predictedPrice = stockController.predictFuturePrice(timeframe);
        final String symbol = symbolField.getText().toUpperCase(Locale.ROOT).trim();
        final String sessionKey = symbol + "_" + timeframe;
        predictedSessions.add(sessionKey);
        JOptionPane.showMessageDialog(this, "Predicted Price: $" + String.format("%.2f", predictedPrice), "Prediction Result", JOptionPane.INFORMATION_MESSAGE);
        evaluateButton.setEnabled(true);
    }

    
    /**
     * to Evaluate model accuracy after prediction is made.
     * this Requires a prior prediction for the same session key.
     */
    private void evaluateModel(ActionEvent event) {
        final String symbol = symbolField.getText().toUpperCase(Locale.ROOT).trim();
        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        final String sessionKey = symbol + "_" + timeframe;

        if (!predictedSessions.contains(sessionKey)) {
            JOptionPane.showMessageDialog(this, "Please predict the price before evaluating the model for this symbol and timeframe.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        stockController.evaluateModel(timeframe);
    }

    
    /**
     * this Opens an embedded TradingView chart in a separate frame.usage of JavaFX here
     */
    private void openChart(ActionEvent event) {
        final String symbol = symbolField.getText().toUpperCase(Locale.ROOT).trim();
        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        stockController.showChart(symbol, timeframe, this);
    }

    
    /**
     * Normalize user input (uppercase, trimmed).
     */
    private void handleSymbolField(ActionEvent event) {
        symbolField.setText(symbolField.getText().toUpperCase(Locale.ROOT).trim());
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

    
    /**
     * this Resets the UI state and clears inputs.
     */
    private void refreshScreen() {
        LOGGER.info("Refreshing Screen...");
        tableModel.setRowCount(0);
        symbolField.setText("");
        timeframeDropdown.setSelectedIndex(0);
        disableActionButtons();
        predictedSessions.clear();
        LOGGER.info("Screen has been refreshed! Timeframe set to Daily.");
        JOptionPane.showMessageDialog(this, "Screen has been refreshed!", "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    
    /**
     * Application entry point: uses SwingUtilities for thread safety.
     */
    public static void main(String[] args) {
        //SwingUtilities.invokeLater(() -> new App().setVisible(true));
        //lambda expression
        SwingUtilities.invokeLater(() -> {
                final App app = new App();
                app.init();              // <-  all UI setup here
                app.setVisible(true);
            });
        }
        
    }

