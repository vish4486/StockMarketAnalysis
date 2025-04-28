package com.sdm.service;

import com.sdm.utils.ConfigLoader;
import com.sdm.utils.CSVUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Fetches stock data, normalizes features, manages stock symbol list.
 */
@SuppressWarnings({"PMD.GuardLogStatement", "PMD.LongVariable"})
public class StockDataFetcher {

    private static final String BASE_URL = ConfigLoader.getBaseUrl();
    private static final String TICKER_API_URL = ConfigLoader.getTickerApiUrl();
    private static final boolean VALIDATE_TICKERS = Boolean.parseBoolean(ConfigLoader.getProperty("validate.tickers"));
    private static final Logger LOGGER = Logger.getLogger(StockDataFetcher.class.getName());
    private static final OkHttpClient CLIENT = new OkHttpClient();

    private static final Map<String, String> STOCK_SYMBOL_MAP = new LinkedHashMap<>();
    private static boolean symbolsFetched = false;

    private final String apiKey;

    private final List<List<String>> stockData = new ArrayList<>();
    private final List<Double> trainingPrices = new ArrayList<>();
    private final List<Double> gridPrices = new ArrayList<>();
    private List<double[]> scaledTrainFeatures = new ArrayList<>();
    private List<double[]> scaledTestFeatures = new ArrayList<>();
    private List<Double> trainTargets = new ArrayList<>();
    private List<Double> testTargets = new ArrayList<>();
    private double[] scaledLatestFeature;

    public StockDataFetcher() {
        this.apiKey = ConfigLoader.getApiKey();
        if (!symbolsFetched) {
            fetchStockSymbols();
        }
    }

    private void fetchStockSymbols() {
        if (TICKER_API_URL == null || TICKER_API_URL.isEmpty()) {
            logError("Cannot fetch stock symbols. API URL is not set.");
            return;
        }
        try {
            JSONArray symbolsArray = getSymbolsArrayFromApi();
            if (VALIDATE_TICKERS) {
                symbolsArray = validateTickers(symbolsArray);
            }
            populateStockSymbolMap(symbolsArray);
            symbolsFetched = true;
            LOGGER.info("Stock symbols fetched successfully. Total: " + STOCK_SYMBOL_MAP.size());
        } catch (IOException e) {
            logError("Failed to fetch stock symbols: " + e.getMessage());
        }
    }

    private JSONArray getSymbolsArrayFromApi() throws IOException {
        final String url = TICKER_API_URL + (TICKER_API_URL.contains("?") ? "&" : "?") + "apikey=" + apiKey;
        final Request request = new Request.Builder().url(url).build();
        return safeApiCall(request, response -> new JSONObject(response.body().string()).getJSONArray("data"));
    }

    private JSONArray validateTickers(final JSONArray symbolsArray) {
        final JSONArray validTickers = new JSONArray();
        int validatedCount = 0;
        for (int i = 0; i < symbolsArray.length() && validatedCount < 1000; i++) {
            final JSONObject stock = symbolsArray.getJSONObject(i);
            final String symbol = stock.getString("symbol");
            final String testUrl = BASE_URL + "?symbol=" + symbol + "&interval=1day&apikey=" + apiKey + "&outputsize=5";

            final Request request = new Request.Builder().url(testUrl).build();
            try (Response response = CLIENT.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null && new JSONObject(response.body().string()).has("values")) {
                    validTickers.put(stock);
                    validatedCount++;
                }
            } catch (IOException ignored) {
            }
        }
        return validTickers;
    }

    private void populateStockSymbolMap(final JSONArray symbolsArray) {
        STOCK_SYMBOL_MAP.clear();
        for (int i = 0; i < symbolsArray.length(); i++) {
            final JSONObject stock = symbolsArray.getJSONObject(i);
            STOCK_SYMBOL_MAP.put(stock.getString("symbol"), stock.getString("symbol") + " - " + stock.getString("name"));
        }
    }

    public static List<String> getStockSymbolList() {
        return symbolsFetched ? new ArrayList<>(STOCK_SYMBOL_MAP.values()) : Collections.emptyList();
    }

    public static String getSymbolFromSelection(final String selection) {
        return STOCK_SYMBOL_MAP.entrySet().stream()
                .filter(entry -> selection.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    public List<List<String>> fetchStockData(final String symbol, final String timeframe) {
        final String interval = switch (timeframe) {
            case "Weekly" -> "1week";
            case "Monthly" -> "1month";
            default -> "1day";
        };
        final String url = BASE_URL + "?symbol=" + symbol + "&interval=" + interval + "&apikey=" + apiKey + "&outputsize=120";

        final Request request = new Request.Builder().url(url).build();
        return safeApiCall(request, response -> parseJson(response.body().string()));
    }

   /**
 * Parses the API JSON response and prepares training/test datasets.
 * Applies Z-score normalization to extracted features.
 *
 * @param jsonData Raw JSON string from stock API
 * @return Parsed and processed stock data rows
 */
private List<List<String>> parseJson(final String jsonData) {
    stockData.clear();
    trainingPrices.clear();
    gridPrices.clear();

    final JSONObject root = new JSONObject(jsonData);

    if (!root.has("values")) {
        LOGGER.severe("Invalid API response: missing 'values' field.\n" + root.toString(2));
        return Collections.emptyList();
    }

    final JSONArray values = root.getJSONArray("values");
    if (values.isEmpty()) {
        LOGGER.warning("API returned empty 'values' array.");
        return Collections.emptyList();
    }

    // Sort latest first
    final List<JSONObject> records = values.toList().stream()
            .map(obj -> new JSONObject((Map<?, ?>) obj))
            .sorted(Comparator.comparing(o -> o.getString("datetime"), Comparator.reverseOrder()))
            .toList();

    return parseStockRecords(records);
}


    /**
    * Parses stock OHLCV records and splits into training/testing sets.
    * Also extracts feature vectors for modeling.
    *
    * @param records List of stock data points (each as JSONObject)
    * @return Sublist of stock rows for UI table
    */
    private List<List<String>> parseStockRecords(final List<JSONObject> records) {
        final List<Double> allClosePrices = new ArrayList<>();
        final List<double[]> allFeatures = new ArrayList<>();

        for (final JSONObject record : records.stream().limit(120).toList()) {
            final double open = record.getDouble("open");
            final double high = record.getDouble("high");
            final double low = record.getDouble("low");
            final double close = record.getDouble("close");
            final double volume = Math.max(record.getDouble("volume"), 1);

            stockData.add(Arrays.asList(
                record.getString("datetime"),
                format(open),
                format(high),
                format(low),
                format(close),
                String.valueOf(volume)
        ));

        allClosePrices.add(close);
        allFeatures.add(new double[]{open, high, low, Math.log(volume)});
    }

    if (allClosePrices.size() < 5) {
        LOGGER.severe("Insufficient stock records received: " + allClosePrices.size());
        return Collections.emptyList();
    }

    // Split data
    final int split = (int) (allClosePrices.size() * 0.8);
    trainTargets = allClosePrices.subList(0, split);
    testTargets = allClosePrices.subList(split, allClosePrices.size());

    final double[][][] scaled = normalizeTogether(
            allFeatures.subList(0, split),
            allFeatures.subList(split, allFeatures.size())
    );

    scaledTrainFeatures = Arrays.asList(scaled[0]);
    scaledTestFeatures = Arrays.asList(scaled[1]);

    scaledLatestFeature = scaledTestFeatures.isEmpty() ? new double[]{0, 0, 0, 0}
            : scaledTestFeatures.get(scaledTestFeatures.size() - 1);

    trainingPrices.addAll(trainTargets);
    gridPrices.addAll(testTargets);

    LOGGER.info("Records parsed: " + records.size());
    LOGGER.info("Training samples: " + trainTargets.size());
    LOGGER.info("Testing samples: " + testTargets.size());
    return stockData.subList(0, Math.min(60, stockData.size()));
}


    private double[][][] normalizeTogether(final List<double[]> train, final List<double[]> test) {
        final int numFeatures = train.get(0).length;
        final double[] mean = new double[numFeatures];
        final double[] std = new double[numFeatures];
    
        for (int i = 0; i < numFeatures; i++) {
            final int idx = i; // capture loop variable into effectively final
            mean[i] = train.stream().mapToDouble(x -> x[idx]).average().orElse(0);
            std[i] = Math.sqrt(train.stream().mapToDouble(x -> Math.pow(x[idx] - mean[idx], 2)).average().orElse(1));
        }
    
        final double[][] trainScaled = train.stream().map(x -> scaleRow(x, mean, std)).toArray(double[][]::new);
        final double[][] testScaled = test.stream().map(x -> scaleRow(x, mean, std)).toArray(double[][]::new);
    
        return new double[][][]{trainScaled, testScaled};
    }
    

   

    private double[] scaleRow(final double[] row, final double[] mean, final double[] std) {
        final double[] result = new double[row.length];
        for (int i = 0; i < row.length; i++) {
            result[i] = (row[i] - mean[i]) / std[i];
        }
        return result;
    }
    

    private String format(final double value) {
        return String.format("%.2f", Math.round(value / 0.05) * 0.05);
    }

    private void logError(final String message) {
        LOGGER.severe(message);
    }

    private <T> T safeApiCall(final Request request, final ApiResponseHandler<T> handler) {
        try (Response response = CLIENT.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return handler.handle(response);
            }
        } catch (IOException e) {
            logError("API call failed: " + e.getMessage());
        }
        return null;
    }

    public void saveToCSV() {
        if (stockData.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No stock data to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Stock Data As...");
        chooser.setSelectedFile(new File("stock_data.csv"));

        if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file + ".csv");
            }
            try {
                CSVUtils.saveToCSV(file.getAbsolutePath(), stockData);
                JOptionPane.showMessageDialog(null, "Stock data saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                logError("Error saving CSV: " + e.getMessage());
                JOptionPane.showMessageDialog(null, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public List<Double> getTrainingPrices() { return trainingPrices; }
    public List<Double> getGridPrices() { return gridPrices; }
    public List<double[]> getScaledTrainFeatures() { return scaledTrainFeatures; }
    public List<double[]> getScaledTestFeatures() { return scaledTestFeatures; }
    public List<Double> getTrainTargets() { return trainTargets; }
    public List<Double> getTestTargets() { return testTargets; }
    public double[] getLatestScaledFeatureVector() { return scaledLatestFeature != null ? Arrays.copyOf(scaledLatestFeature, scaledLatestFeature.length) : new double[0]; }

    @FunctionalInterface
    private interface ApiResponseHandler<T> {
        T handle(Response response) throws IOException;
    }
}
