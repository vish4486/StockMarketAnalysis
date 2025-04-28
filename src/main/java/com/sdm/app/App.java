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
    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    private static final String TITLE = "Stock Data Viewer";
    private static final String INPUT_ERROR = "Input Error";
    private static final String INFO = "Info";
    private static final String ERROR = "Error";
    private static final String[] TIMEFRAMES = {"Daily", "Weekly", "Monthly"};

    private JTextField symbolField;
    private JButton fetchButton, saveButton, predictButton, evaluateButton, chartButton;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> timeframeDropdown;

    private final transient StockController stockController;
    private final List<String> stockSymbols;
    private final Set<String> predictedSessions = new HashSet<>();

    public App() {
        super();
        final List<PredictionModel> models = ModelFactory.getFixedModels();
        stockController = new StockController(models);
        stockSymbols = StockDataFetcher.getStockSymbolList();
    }

    public void initializeAppWindow() {
        setTitle(TITLE);
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initializeUI();
    }

    private void initializeUI() {
        final JPanel panel = new JPanel(new BorderLayout());

        panel.add(buildInputPanel(), BorderLayout.NORTH);
        panel.add(new JScrollPane(buildTable()), BorderLayout.CENTER);
        panel.add(buildActionButtonPanel(), BorderLayout.SOUTH);

        setContentPane(panel);
        LOGGER.info("UI Initialized.");
    }

    private JPanel buildInputPanel() {
        final JPanel inputPanel = new JPanel();
        symbolField = new JTextField(10);
        enableAutoComplete(symbolField);
        symbolField.addActionListener(this::handleSymbolField);

        timeframeDropdown = new JComboBox<>(TIMEFRAMES);
        timeframeDropdown.addActionListener(event -> autoFetchOnTimeframeChange());

        fetchButton = new JButton("Fetch Data");
        fetchButton.addActionListener(this::fetchStockData);

        inputPanel.add(new JLabel("Stock Symbol:"));
        inputPanel.add(symbolField);
        inputPanel.add(new JLabel("Timeframe:"));
        inputPanel.add(timeframeDropdown);
        inputPanel.add(fetchButton);

        return inputPanel;
    }

    private JTable buildTable() {
        final String[] columns = {"Date", "Open", "High", "Low", "Close", "Volume"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        return table;
    }

    private JPanel buildActionButtonPanel() {
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

        final JButton refreshButton = new JButton("Refresh Screen");
        refreshButton.addActionListener(event -> refreshScreen());
        buttonPanel.add(refreshButton);

        return buttonPanel;
    }

    private void enableAutoComplete(final JTextField textField) {
        final JPopupMenu popupMenu = new JPopupMenu();
        final JList<String> suggestionList = new JList<>();

        popupMenu.setFocusable(false);
        popupMenu.setPopupSize(300, 150);
        popupMenu.add(new JScrollPane(suggestionList));

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSuggestions(); }
            public void removeUpdate(DocumentEvent e) { updateSuggestions(); }
            public void changedUpdate(DocumentEvent e) { updateSuggestions(); }

            private void updateSuggestions() {
                SwingUtilities.invokeLater(() -> {
                    final String input = normalizeInput(textField.getText());
                    if (input.isEmpty()) {
                        popupMenu.setVisible(false);
                        return;
                    }
                    final List<String> filtered = stockSymbols.stream()
                            .filter(name -> name.toUpperCase(Locale.ROOT).contains(input))
                            .limit(10).toList();
                    if (filtered.isEmpty()) {
                        popupMenu.setVisible(false);
                        return;
                    }
                    suggestionList.setListData(filtered.toArray(new String[0]));
                    suggestionList.setVisibleRowCount(Math.min(filtered.size(), 10));
                    popupMenu.show(textField, 0, textField.getHeight());
                });
            }
        });

        textField.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent event) {
                if (popupMenu.isVisible() && event.getKeyCode() == KeyEvent.VK_DOWN) {
                    suggestionList.setSelectedIndex(0);
                    suggestionList.requestFocus();
                }
            }
        });

        suggestionList.addKeyListener(new KeyAdapter() {
            public void keyPressed(final KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.VK_ENTER) {
                    final String selected = suggestionList.getSelectedValue();
                    applyAutoComplete(selected);
                }
            }
        });

        suggestionList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent event) {
                final String selected = suggestionList.getSelectedValue();
                applyAutoComplete(selected);
            }
        });
    }

    private void applyAutoComplete(String selected) {
        if (selected != null) {
            final String symbol = StockDataFetcher.getSymbolFromSelection(selected);
            symbolField.setText(symbol);
            symbolField.requestFocus();
        }
    }

    private void autoFetchOnTimeframeChange() {
        if (!normalizeInput(symbolField.getText()).isEmpty()) {
            fetchStockData(null);
        }
    }

    private void fetchStockData(ActionEvent event) {
        final String symbol = normalizeInput(symbolField.getText());
        if (symbol.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a valid stock symbol!", INPUT_ERROR, JOptionPane.WARNING_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Failed to fetch data!", ERROR, JOptionPane.ERROR_MESSAGE);
        }
    }

    private void predictFuturePrice(ActionEvent event) {
        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        final double predictedPrice = stockController.predictFuturePrice(timeframe);
        final String symbol = normalizeInput(symbolField.getText());
        final String sessionKey = symbol + "_" + timeframe;
        predictedSessions.add(sessionKey);
        JOptionPane.showMessageDialog(this, "Predicted Price: $" + String.format("%.2f", predictedPrice), INFO, JOptionPane.INFORMATION_MESSAGE);
        evaluateButton.setEnabled(true);
    }

    private void evaluateModel(ActionEvent event) {
        final String symbol = normalizeInput(symbolField.getText());
        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        final String sessionKey = symbol + "_" + timeframe;

        if (!predictedSessions.contains(sessionKey)) {
            JOptionPane.showMessageDialog(this, "Please predict the price before evaluating.", INFO, JOptionPane.WARNING_MESSAGE);
            return;
        }
        stockController.evaluateModel(timeframe);
    }

    private void openChart(ActionEvent event) {
        final String symbol = normalizeInput(symbolField.getText());
        final String timeframe = (String) timeframeDropdown.getSelectedItem();
        stockController.showChart(symbol, timeframe, this);
    }

    private void refreshScreen() {
        tableModel.setRowCount(0);
        symbolField.setText("");
        timeframeDropdown.setSelectedIndex(0);
        disableActionButtons();
        predictedSessions.clear();
        JOptionPane.showMessageDialog(this, "Screen has been refreshed!", INFO, JOptionPane.INFORMATION_MESSAGE);
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

    private void handleSymbolField(ActionEvent event) {
        symbolField.setText(normalizeInput(symbolField.getText()));
    }

    private String normalizeInput(String input) {
        return input == null ? "" : input.toUpperCase(Locale.ROOT).trim();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            final App app = new App();
            app.initializeAppWindow();
            app.setVisible(true);
        });
    }
}
