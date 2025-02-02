package com.stockanalysis;

import java.io.FileWriter;
import java.util.Map;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StockDataFetcher {

    public static void fetchStockData(String symbol) {
        String apiKey = "6HY5V13LX2LO0MKO"; // Replace with your Alpha Vantage API key
        String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol="
                + symbol + "&interval=5min&apikey=" + apiKey;

        try (FileWriter writer = new FileWriter("stock_data.csv")) {
            // Fetch data from the API
            String response = new java.util.Scanner(new java.net.URL(urlString).openStream(), "UTF-8").useDelimiter("\\A").next();

            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response).getAsJsonObject();
            JsonObject timeSeries = jsonResponse.getAsJsonObject("Time Series (5min)");

            // Write header
            writer.append("Timestamp,Open,High,Low,Close,Volume\n");

            // Iterate over timestamps and write each row
            for (Map.Entry<String, ?> entry : timeSeries.entrySet()) {
                String timestamp = entry.getKey();
                JsonObject data = (JsonObject) entry.getValue();

                String open = data.get("1. open").getAsString();
                String high = data.get("2. high").getAsString();
                String low = data.get("3. low").getAsString();
                String close = data.get("4. close").getAsString();
                String volume = data.get("5. volume").getAsString();

                writer.append(String.format("%s,%s,%s,%s,%s,%s\n", timestamp, open, high, low, close, volume));
            }

            System.out.println("Data saved to stock_data.csv");
        } catch (Exception e) {
            System.out.println("Error fetching stock data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        fetchStockData("AAPL");
    }
}

