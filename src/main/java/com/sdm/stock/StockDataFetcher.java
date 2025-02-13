package com.sdm.stock;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.List;

public class StockDataFetcher {
    private static final String API_KEY = "1Z4BJCEETPH8X9KE"; // Your API key
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    public static String getStockData(String symbol) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + API_KEY;

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        
        if (!response.isSuccessful()) {
            throw new IOException("API request failed with status: " + response.code());
        }

        return response.body().string();
    }

    public static void main(String[] args) {
        System.out.println("Starting StockDataFetcher...");

        if (args.length == 0) {
            System.out.println("Usage: java StockDataFetcher <STOCK_SYMBOL>");
            return;
        }

        String stockSymbol = args[0];

        try {
            String stockJson = getStockData(stockSymbol);
            JSONObject json = new JSONObject(stockJson);

            JSONObject timeSeries = json.getJSONObject("Time Series (Daily)");
            String latestDate = timeSeries.keys().next();
            JSONObject latestData = timeSeries.getJSONObject(latestDate);

            double open = latestData.getDouble("1. open");
            double high = latestData.getDouble("2. high");
            double low = latestData.getDouble("3. low");
            double close = latestData.getDouble("4. close");
            int volume = latestData.getInt("5. volume");

            StockDatabase.initializeDatabase();
            StockDatabase.insertStockData(stockSymbol, latestDate, open, high, low, close, volume);

            System.out.println("\n✅ Stock Data Stored in SQLite:");
            System.out.println("Symbol: " + stockSymbol);
            System.out.println("Date: " + latestDate);
            System.out.println("Open: " + open);
            System.out.println("High: " + high);
            System.out.println("Low: " + low);
            System.out.println("Close: " + close);
            System.out.println("Volume: " + volume);

            // Predict Stock Price
            List<StockRecord> stockData = StockDatabase.getStockData(stockSymbol);
            int daysAhead = 5;
            double predictedPrice = StockPredictor.predictNextPrice(stockData, daysAhead);
            System.out.println("\n🔮 Predicted Stock Price for " + stockSymbol + " in " + daysAhead + " days: " + predictedPrice);

        } catch (IOException e) {
            System.err.println("❌ Error fetching stock data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Parsing error: " + e.getMessage());
        }
    }
}

