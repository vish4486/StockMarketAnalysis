package com.sdm.stock;

import okhttp3.*;
import org.json.JSONObject;
import java.io.IOException;

public class StockDataFetcher {
    private static final String API_KEY = "YOUR_ALPHAVANTAGE_API_KEY"; // Replace with your API key
    private static final String BASE_URL = "https://www.alphavantage.co/query";

    public static String getStockData(String symbol) throws IOException {
        OkHttpClient client = new OkHttpClient();
        String url = BASE_URL + "?function=TIME_SERIES_DAILY&symbol=" + symbol + "&apikey=" + API_KEY;

        Request request = new Request.Builder().url(url).build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java StockDataFetcher <STOCK_SYMBOL>");
            return;
        }

        String stockSymbol = args[0];
        try {
            String stockJson = getStockData(stockSymbol);
            JSONObject json = new JSONObject(stockJson);
            System.out.println(json.toString(4));
        } catch (IOException e) {
            System.err.println("Error fetching stock data: " + e.getMessage());
        }
    }
}

