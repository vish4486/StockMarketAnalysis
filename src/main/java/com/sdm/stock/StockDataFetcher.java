package com.sdm.stock;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Scanner;

public class StockDataFetcher {
    private static final String API_KEY = "1Z4BJCEETPH8X9KE";
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    public static String getStockData(String symbol) {
        try {
            OkHttpClient client = new OkHttpClient();
            String url = BASE_URL + "?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + API_KEY;
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            return null;
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Stock Symbol: ");
        String stockSymbol = scanner.next().toUpperCase();
        scanner.close();

        String stockJson = getStockData(stockSymbol);
        if (stockJson == null) {
            return;
        }

        try {
            JSONObject json = new JSONObject(stockJson);
            JSONObject timeSeries = json.getJSONObject("Time Series (Daily)");
            String latestDate = timeSeries.keys().next();
            JSONObject latestData = timeSeries.getJSONObject(latestDate);

            double open = latestData.getDouble("1. open");
            double high = latestData.getDouble("2. high");
            double low = latestData.getDouble("3. low");
            double close = latestData.getDouble("4. close");
            int volume = latestData.getInt("5. volume");

            StockCSVHandler.saveStockData(stockSymbol, latestDate, open, high, low, close, volume);

            System.out.println("\nStock Data:");
            System.out.println("Symbol: " + stockSymbol);
            System.out.println("Date: " + latestDate);
            System.out.println("Open: " + open);
            System.out.println("High: " + high);
            System.out.println("Low: " + low);
            System.out.println("Close: " + close);
            System.out.println("Volume: " + volume);

        } catch (Exception e) {}
    }
}
