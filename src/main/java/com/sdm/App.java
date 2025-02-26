package com.sdm;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Vector;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.stream.Collectors;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;



public class App extends JFrame {

    private static final String[] TIMEFRAMES = {"Daily", "Weekly", "Monthly"};

    private JTextField symbolField;
    private JButton fetchButton, saveButton, predictButton, evaluateButton, chartButton;
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> timeframeDropdown;
    private final StockController stockController;
    private final List<String> stockSymbols; // for autocomplete textfield functionality

    public App() {
        setTitle("Stock Data Viewer");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        PredictionModel predictionModel = new LinearRegressionModel();
        stockController = new StockController(predictionModel);
       // Fetch stock symbols for autocomplete  textfield functionality
        //StockDataFetcher dataFetcher = new StockDataFetcher();
        //stockSymbols = dataFetcher.getStockSymbols();
        stockSymbols = StockDataFetcher.getStockSymbolList();
       //
        initializeUI();
    }

    private void initializeUI() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Stock Symbol:"));
        symbolField = new JTextField(10);
        enableAutoComplete(symbolField);  // for autocomplete  textfield functionality
        //symbolField.addActionListener(e -> handleSymbolField());
        symbolField.addActionListener(this::handleSymbolField);



        inputPanel.add(symbolField);

        timeframeDropdown = new JComboBox<>(TIMEFRAMES);
        inputPanel.add(new JLabel("Timeframe:"));
        inputPanel.add(timeframeDropdown);

        fetchButton = new JButton("Fetch Data");
        fetchButton.addActionListener(this::fetchStockData);
        inputPanel.add(fetchButton);

        //  NEW: Auto-fetch when timeframe changes
        timeframeDropdown.addActionListener(e -> autoFetchOnTimeframeChange());

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

        // FIXED: Using method references (no more warnings!)
        saveButton.addActionListener(stockController::saveToCSV);
        predictButton.addActionListener(this::predictFuturePrice);
        evaluateButton.addActionListener(this::evaluateModel);
        chartButton.addActionListener(this::openChart);

        buttonPanel.add(saveButton);
        buttonPanel.add(predictButton);
        buttonPanel.add(evaluateButton);
        buttonPanel.add(chartButton);

        // 🔹 Refresh Button (Separate Panel)
        JPanel refreshPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh Screen");
        refreshButton.addActionListener(e -> refreshScreen());
        refreshPanel.add(refreshButton);

        buttonPanel.add(refreshPanel, BorderLayout.SOUTH);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        System.out.println(" Action panel added to main UI.");

        //panel.add(buttonPanel, BorderLayout.SOUTH);
        add(panel);
    }


    /**
 * Auto-fetches stock data when the timeframe is changed.
 */
     private void autoFetchOnTimeframeChange() {
    String symbol = symbolField.getText().toUpperCase().trim();

    if (symbol.isEmpty()) {
        return; // Do nothing if stock symbol is empty
    }

    System.out.println("Timeframe changed, fetching new data for: " + symbol);
    fetchStockData(null); // Calls existing fetchStockData method
    }

     /**
     * Enables autocomplete functionality for the stock symbol text field.
     */
  private void enableAutoComplete(JTextField textField) {
    JPopupMenu popupMenu = new JPopupMenu();
    JList<String> suggestionList = new JList<>();
    JScrollPane scrollPane = new JScrollPane(suggestionList);


    popupMenu.setFocusable(false);
    popupMenu.setPopupSize(300, 150);
    popupMenu.add(scrollPane);

    textField.getDocument().addDocumentListener(new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateSuggestions();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateSuggestions();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateSuggestions();
        }

        private void updateSuggestions() {
            SwingUtilities.invokeLater(() -> {
                String input = textField.getText().toUpperCase().trim();

                if (input.isEmpty()) {
                    popupMenu.setVisible(false);
                    return;
                }

                //  Fetch **stock names** from StockDataFetcher
                List<String> filteredSymbols = StockDataFetcher.getStockSymbolList().stream()
                        .filter(name -> name.toUpperCase().contains(input)) // Match input anywhere in the name
                        .limit(10)
                        .toList();

                if (filteredSymbols.isEmpty()) {
                    popupMenu.setVisible(false);
                    return;
                }

                // Update the suggestion list
                suggestionList.setListData(filteredSymbols.toArray(new String[0]));
                suggestionList.setVisibleRowCount(Math.min(filteredSymbols.size(), 10));

                //  Show popup below text field
                popupMenu.show(textField, 0, textField.getHeight());
            });
        }
    });

    textField.addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
        if (popupMenu.isVisible()) {
            if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                suggestionList.setSelectedIndex(0); // Select first item
                suggestionList.requestFocus(); // Move focus to suggestions
            }
        }
    }
});

    // 🔹 Allow selection using **keyboard arrows & Enter key**
    suggestionList.addKeyListener(new KeyAdapter() {
    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            String selectedValue = suggestionList.getSelectedValue();
            String symbol = StockDataFetcher.getSymbolFromSelection(selectedValue);

            if (symbol != null) {
                int caretPosition = textField.getCaretPosition();
                textField.setText(symbol);
                textField.setCaretPosition(caretPosition); // Keep cursor at the end
                popupMenu.setVisible(false);
                textField.requestFocus(); //  Return focus to text field for further typing
            }
        }
    }
});


    // 🔹 Handle mouse click selection on the suggestion list
    suggestionList.addMouseListener(new MouseAdapter() {
    @Override
    public void mouseClicked(MouseEvent e) {
        String selectedValue = suggestionList.getSelectedValue();
        String symbol = StockDataFetcher.getSymbolFromSelection(selectedValue);

        if (symbol != null) {
            // 🔹 Preserve cursor position
            int caretPosition = textField.getCaretPosition();
            textField.setText(symbol);
            textField.setCaretPosition(caretPosition); // Restore cursor position
            popupMenu.setVisible(false);
        }
    }});

}


   
   private void fetchStockData(ActionEvent event) {
    String symbol = symbolField.getText().toUpperCase().trim();

    // 🔹 Validate the stock symbol
    if (symbol == null || symbol.isEmpty()) {
        JOptionPane.showMessageDialog(this, "Please enter a valid stock symbol!", "Input Error", JOptionPane.WARNING_MESSAGE);
        return;
    }

    String timeframe = (String) timeframeDropdown.getSelectedItem();
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

        //  Reset stock symbol input
        symbolField.setText("");

        //  Reset timeframe dropdown back to "Daily"
        timeframeDropdown.setSelectedIndex(0); // Sets the dropdown to the first item (Daily)

        //  Disable buttons
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
