package com.sdm;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.apache.commons.math3.stat.regression.SimpleRegression;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.Scanner;
import java.util.Vector;

public class App extends JFrame {

    private static final String API_KEY = "1Z4BJCEETPH8X9KE";
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    private JTextField symbolField;
    private JButton fetchButton, saveButton, predictButton;
    private JTable table;
    private DefaultTableModel tableModel;
    private File lastSavedFile;

    public App() {
        setTitle("Stock Data Viewer");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Stock Symbol:"));
        symbolField = new JTextField(10);
        inputPanel.add(symbolField);
        fetchButton = new JButton("Fetch Data");
        inputPanel.add(fetchButton);
        panel.add(inputPanel, BorderLayout.NORTH);

        // Table setup
        String[] columns = {"Date", "Open", "High", "Low", "Close", "Volume"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel();
        saveButton = new JButton("Save to CSV");
        saveButton.setEnabled(false);
        predictButton = new JButton("Predict Future Price");
        predictButton.setEnabled(false);

        buttonPanel.add(saveButton);
        buttonPanel.add(predictButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        add(panel);

        // Button actions
        fetchButton.addActionListener(e -> {
            String symbol = symbolField.getText().toUpperCase().trim();
            if (!symbol.isEmpty()) {
                fetchStockData(symbol);
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a stock symbol.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        saveButton.addActionListener(e -> saveToCSV());
        predictButton.addActionListener(e -> predictFuturePrice());

    }

    private void fetchStockData(String symbol) {
        new Thread(() -> {
            String response = fetchDataFromAPI(symbol);
            if (response != null) {
                updateTable(response);
                saveButton.setEnabled(true);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to fetch data for " + symbol, "Error", JOptionPane.ERROR_MESSAGE);
            }
        }).start();
    }

    private String fetchDataFromAPI(String symbol) {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + API_KEY;

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return null;
            }
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    private void updateTable(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            if (!jsonObject.has("Time Series (Daily)")) {
                JOptionPane.showMessageDialog(this, "Invalid symbol or API limit reached!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            JSONObject timeSeries = jsonObject.getJSONObject("Time Series (Daily)");
            Vector<Vector<String>> data = new Vector<>();

            for (String date : timeSeries.keySet()) {
                JSONObject dailyData = timeSeries.getJSONObject(date);
                Vector<String> row = new Vector<>();
                row.add(date);
                row.add(dailyData.getString("1. open"));
                row.add(dailyData.getString("2. high"));
                row.add(dailyData.getString("3. low"));
                row.add(dailyData.getString("4. close"));
                row.add(dailyData.getString("5. volume"));
                data.add(row);
            }

            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                for (Vector<String> row : data) {
                    tableModel.addRow(row);
                }
            });

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error parsing JSON data.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save CSV File");
        fileChooser.setSelectedFile(new File("stock_data.csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            lastSavedFile = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(lastSavedFile)) {
                writer.write("Date,Open,High,Low,Close,Volume\n");

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        writer.write(tableModel.getValueAt(row, col).toString());
                        if (col < tableModel.getColumnCount() - 1) {
                            writer.write(",");
                        }
                    }
                    writer.write("\n");
                }
                JOptionPane.showMessageDialog(this, "File saved: " + lastSavedFile.getAbsolutePath(), "Success", JOptionPane.INFORMATION_MESSAGE);
                predictButton.setEnabled(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Error saving CSV file.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void predictFuturePrice() {
        if (lastSavedFile == null) {
            JOptionPane.showMessageDialog(this, "No CSV file found. Save the file first.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Scanner scanner = new Scanner(lastSavedFile)) {
            scanner.nextLine(); // Skip header

            SimpleRegression regression = new SimpleRegression();

            int day = 1;
            while (scanner.hasNextLine()) {
                String[] row = scanner.nextLine().split(",");
                if (row.length >= 5) {
                    double closePrice = Double.parseDouble(row[4]);
                    regression.addData(day++, closePrice);
                }
            }

            double nextDay = day;
            double predictedPrice = regression.predict(nextDay);

            JOptionPane.showMessageDialog(this, "Predicted Next Day Price: $" + String.format("%.2f", predictedPrice), "Prediction", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error reading CSV file for prediction.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
