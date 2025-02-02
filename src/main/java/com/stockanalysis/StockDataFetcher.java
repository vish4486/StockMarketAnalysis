package com.stockanalysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StockDataFetcher {

    public static void fetchStockData(String symbol) {
        String apiKey = "6HY5V13LX2LO0MKO"; // Replace with your Alpha Vantage API key
        String urlString = "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol="
                           + symbol + "&interval=5min&apikey=" + apiKey;

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse JSON response
            JsonObject jsonResponse = JsonParser.parseString(response.toString()).getAsJsonObject();
            System.out.println("Raw JSON Response: " + jsonResponse);
        } catch (Exception e) {
            System.out.println("Error fetching stock data: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        fetchStockData("AAPL");
    }
}

