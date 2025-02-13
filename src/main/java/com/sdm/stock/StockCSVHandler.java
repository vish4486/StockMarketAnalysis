package com.sdm.stock;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StockCSVHandler {
    private static final String FILE_NAME = "stock_data.csv";

    public static void saveStockData(String symbol, String date, double open, double high, double low, double close, int volume) {
        try (FileWriter writer = new FileWriter(FILE_NAME, true)) {
            writer.write(symbol + "," + date + "," + open + "," + high + "," + low + "," + close + "," + volume + "\n");
        } catch (IOException ignored) {}
    }

    public static List<double[]> getStockData(String symbol) {
        List<double[]> stockData = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            reader.readLine(); // Skip header
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts[0].equalsIgnoreCase(symbol)) {
                    stockData.add(new double[]{
                        Double.parseDouble(parts[2]),  // open
                        Double.parseDouble(parts[3]),  // high
                        Double.parseDouble(parts[4]),  // low
                        Double.parseDouble(parts[5]),  // close
                        Double.parseDouble(parts[6])   // volume
                    });
                }
            }
        } catch (IOException ignored) {}
        return stockData;
    }
}
