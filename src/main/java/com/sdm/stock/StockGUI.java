package com.sdm.stock;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class StockGUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(StockGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Stock Market Analysis");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null); // Center window

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Enter Stock Symbol:", SwingConstants.CENTER);
        JTextField stockSymbolField = new JTextField(10);
        JButton fetchButton = new JButton("Fetch Data");

        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        fetchButton.addActionListener(e -> {
            String symbol = stockSymbolField.getText().toUpperCase();
            List<StockRecord> stockData = StockDatabase.getStockData(symbol);

            if (stockData.isEmpty()) {
                outputArea.setText("No data found for " + symbol);
                return;
            }

            outputArea.setText("Stock Data for: " + symbol + "\n");

            for (StockRecord record : stockData) {
                outputArea.append(record.getDate() + " - Close: " + record.getClose() + "\n");
            }

            int daysAhead = 5;
            double predictedPrice = StockPredictor.predictNextPrice(stockData, daysAhead);
            outputArea.append("\n🔮 Predicted Price in " + daysAhead + " days: " + predictedPrice);
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(titleLabel);
        inputPanel.add(stockSymbolField);
        inputPanel.add(fetchButton);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.add(panel);
        frame.setVisible(true);
    }
}
