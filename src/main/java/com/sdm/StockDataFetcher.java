package com.sdm;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class StockDataFetcher {
    private static final String BASE_URL = ConfigLoader.getBaseUrl(); //  No hardcoded URL
    private static final String TICKER_API_URL = ConfigLoader.getTickerApiUrl(); // Load dynamically from config.properties
    private final String API_KEY;

    private final List<Double> trainingPrices = new ArrayList<>();
    private final List<Double> gridPrices = new ArrayList<>();
    private final List<Vector<String>> stockData = new ArrayList<>();

    // Cache for stock tickers (to avoid repeated API calls)
   private static final Map<String, String> stockSymbolMap = new LinkedHashMap<>(); // Store symbol-name pairs
    private static boolean symbolsFetched = false;

    public StockDataFetcher() {
        this.API_KEY = ConfigLoader.getApiKey(); //  Load API Key from ConfigLoader
        // Fetch stock symbols only once when the class is initialized
        if (!symbolsFetched) {
            fetchStockSymbols();
        }
    }

    /**
     * Fetches stock tickers and stores them in a map (symbol → "symbol - name").
     */
    private void fetchStockSymbols() {
        if (TICKER_API_URL == null || TICKER_API_URL.isEmpty()) {
            System.err.println("ERROR: Cannot fetch stock symbols. API URL is not set.");
            return;
        }

        try {
            OkHttpClient client = new OkHttpClient();
            String requestUrl = TICKER_API_URL + "?apikey=" + API_KEY;
            Request request = new Request.Builder().url(requestUrl).build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful() || response.body() == null) {
                System.err.println("ERROR: Failed to fetch stock symbols. HTTP Code: " + response.code());
                return;
            }

            JSONObject jsonResponse = new JSONObject(response.body().string());
            if (!jsonResponse.has("data")) {
                System.err.println("ERROR: Unexpected API response format. No 'data' field found.");
                return;
            }

            JSONArray symbolsArray = jsonResponse.getJSONArray("data");
            stockSymbolMap.clear();

            for (int i = 0; i < symbolsArray.length(); i++) {
                JSONObject stock = symbolsArray.getJSONObject(i);
                String symbol = stock.getString("symbol");
                String name = stock.getString("name");

                // Store "symbol - company name" in the map
                stockSymbolMap.put(symbol, symbol + " - " + name);
            }

            symbolsFetched = true;
            System.out.println("Stock symbols fetched successfully. Total: " + stockSymbolMap.size());

        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch stock symbols: " + e.getMessage());
        }
    }

    /**
     * Returns a list of formatted stock symbols ("SYMBOL - Company Name") for autocomplete.
     */
    public static List<String> getStockSymbolList() {
        if (!symbolsFetched) {
            System.err.println("⚠ WARNING: Stock symbols were not fetched successfully!");
            return Collections.emptyList();
        }
        return new ArrayList<>(stockSymbolMap.values());
    }

    /**
 * Extracts and returns the stock symbol from a selection string (e.g., "AAPL - Apple Inc.").
 */
public static String getSymbolFromSelection(String selection) {
    for (Map.Entry<String, String> entry : stockSymbolMap.entrySet()) {
        if (selection.equals(entry.getValue())) { // Ensure exact match with name
            return entry.getKey(); // Return only the stock ticker (symbol)
        }
    }
    return null; // No match found
}


public static List<String> getMatchingSymbols(String input) {
    return stockSymbolMap.entrySet().stream()
            .filter(entry -> entry.getKey().startsWith(input)) // Matches stock symbol first
            .limit(10)
            .map(entry -> entry.getKey() + " - " + entry.getValue()) // "AAPL - Apple Inc."
            .collect(Collectors.toList());
}




    public List<Vector<String>> fetchStockData(String symbol, String timeframe) {
        System.out.println(" Fetching stock data for: " + symbol + " | Timeframe: " + timeframe);

        String interval = switch (timeframe) {
            case "Daily" -> "1day";
            case "Weekly" -> "1week";
            case "Monthly" -> "1month";
            default -> "1day";
        };

        String url = BASE_URL + "?symbol=" + symbol + "&interval=" + interval +
                "&apikey=" + API_KEY + "&outputsize=120";

        System.out.println("🔗 API Request URL: " + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println(" API Request Failed: " + response.message());
                return Collections.emptyList();
            }
            return parseJson(response.body().string());
        } catch (Exception e) {
            System.err.println(" Error fetching stock data: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<Vector<String>> parseJson(String jsonData) {
        stockData.clear();
        trainingPrices.clear();
        gridPrices.clear();

        JSONObject jsonObject = new JSONObject(jsonData);
        if (!jsonObject.has("values")) {
            System.err.println("Invalid JSON response: No 'values' field found.");
            return Collections.emptyList();
        }

        JSONArray timeSeries = jsonObject.getJSONArray("values");

        //  Convert JSONArray to List<JSONObject> and Sort Descending (Latest First)
        List<JSONObject> sortedData = timeSeries.toList().stream()
                .map(obj -> new JSONObject((Map<?, ?>) obj))
                .sorted(Comparator.comparing(o -> o.getString("datetime"), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        System.out.println(" Total records received from API: " + sortedData.size());

        //  Extract and Store Data
        sortedData.stream()
                .limit(120) // Process only 120 records
                .forEachOrdered(dailyData -> {
                    double closePrice = roundToTick(dailyData.getDouble("close"));
                    Vector<String> row = new Vector<>(Arrays.asList(
                            dailyData.getString("datetime"),
                            formatStockPrice(dailyData.getDouble("open")),
                            formatStockPrice(dailyData.getDouble("high")),
                            formatStockPrice(dailyData.getDouble("low")),
                            formatStockPrice(closePrice),
                            dailyData.getString("volume")
                    ));
                    stockData.add(row);
                });

        // Extract Close Prices using Streams (Functional Approach)
        List<Double> closePrices = sortedData.stream()
                .map(d -> roundToTick(d.getDouble("close")))
                .collect(Collectors.toList());

        trainingPrices.addAll(closePrices.subList(60, 120)); // First 60 for training
        gridPrices.addAll(closePrices.subList(0, 60)); // Last 60 for UI

        System.out.println(" Training Data (Oldest 60) Count: " + trainingPrices.size());
        System.out.println("Grid Data (Latest 60) Count: " + gridPrices.size());

        //return stockData.subList(stockData.size() - 60, stockData.size());
        return stockData.subList(0, 60); //  Correct - Use first 60 records

    }

    //  Rounds prices to the nearest 0.05
    private String formatStockPrice(double price) {
        return String.format("%.2f", roundToTick(price));
    }

    private double roundToTick(double price) {
        return Math.round(price / 0.05) * 0.05;
    }

   


    public void saveToCSV() {
    if (stockData.isEmpty()) {
        JOptionPane.showMessageDialog(null, " No stock data to save!", "Error", JOptionPane.ERROR_MESSAGE);
        return;
    }

    //  Open File Chooser Dialog
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setDialogTitle("Save Stock Data As...");
    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    fileChooser.setApproveButtonText("Save");

    //  Suggest a default filename
    fileChooser.setSelectedFile(new File("stock_data.csv"));

    int userSelection = fileChooser.showSaveDialog(null);

    if (userSelection == JFileChooser.APPROVE_OPTION) {
        File selectedFile = fileChooser.getSelectedFile();
        
        //  Ensure the file has a `.csv` extension
        if (!selectedFile.getAbsolutePath().toLowerCase().endsWith(".csv")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
        }

        try {
            //  Save using CSVUtils (Delegate responsibility)
            CSVUtils.saveToCSV(selectedFile.getAbsolutePath(), stockData);
            JOptionPane.showMessageDialog(null, " Stock data saved successfully at:\n" + selectedFile.getAbsolutePath(), 
                                          "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, " Error saving stock data!", "Error", JOptionPane.ERROR_MESSAGE);
            System.err.println(" Error saving CSV: " + e.getMessage());
        }
    } else {
        System.out.println("⚠ User canceled the save operation.");
    }
}


    //  Getters for training and grid prices
    public List<Double> getTrainingPrices() {
        return trainingPrices;
    }

    public List<Double> getGridPrices() {
        return gridPrices;
    }
}
