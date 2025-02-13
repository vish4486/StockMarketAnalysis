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
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

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
            List<double[]> stockData = StockCSVHandler.getStockData(symbol);

            if (stockData.isEmpty()) {
                outputArea.setText("No data found for " + symbol);
                return;
            }

            outputArea.setText("Stock Data for: " + symbol + "\n");

            for (int i = 0; i < stockData.size(); i++) {
                outputArea.append("Entry " + (i + 1) + " - Close: " + stockData.get(i)[3] + "\n");
            }

            int daysAhead = 5;
            double predictedPrice = StockPredictor.predictNextPrice(stockData, daysAhead);
            outputArea.append("\nPredicted Price in " + daysAhead + " days: " + predictedPrice);
        });

        JPanel inputPanel = new JPanel();
        inputPanel.add(titleLabel);
        inputPanel.add(stockSymbolField);
        inputPanel.add(fetchButton);

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        frame.add(panel);
    }
}
