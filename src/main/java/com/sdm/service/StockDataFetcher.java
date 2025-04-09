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

@SuppressWarnings({"PMD.GuardLogStatement", "PMD.LongVariable", "PMD.UnusedAssignment"})
public class StockDataFetcher {

    // Base endpoints loaded from config
    private static final String BASE_URL = ConfigLoader.getBaseUrl();
    private static final String TICKER_API_URL = ConfigLoader.getTickerApiUrl();
    private final String apiKey;

    // Core datasets
    private final List<List<String>> stockData = new ArrayList<>();
    private final List<Double> trainingPrices = new ArrayList<>();
    private final List<Double> gridPrices = new ArrayList<>();

    // Scaled and split datasets for modeling
    private List<double[]> scaledTrainFeatures = new ArrayList<>();
    private List<double[]> scaledTestFeatures = new ArrayList<>();
    private List<Double> trainTargets = new ArrayList<>();
    private List<Double> testTargets = new ArrayList<>();
    private double[] scaledLatestFeature;

    private static final Map<String, String> STOCK_SYMBOL_MAP = new LinkedHashMap<>();
    private static boolean symbolsFetched = false;
    private static final Logger LOGGER = Logger.getLogger(StockDataFetcher.class.getName());

    public StockDataFetcher() {
        this.apiKey = ConfigLoader.getApiKey();
        if (!symbolsFetched) {
            fetchStockSymbols();
        }
    }

    
    /** Fetches list of available stock symbols from configured API */
    private void fetchStockSymbols() {
        if (!isTickerApiUrlValid()) {
            LOGGER.severe("ERROR: Cannot fetch stock symbols. API URL is not set.");
            return;
        }

        try {
            final JSONArray symbolsArray = getSymbolsArrayFromApi();
            populateStockSymbolMap(symbolsArray);
            symbolsFetched = true;
            LOGGER.info("Stock symbols fetched successfully. Total: " + STOCK_SYMBOL_MAP.size());
        } catch (IOException e) {
            LOGGER.severe("ERROR: Failed to fetch stock symbols: " + e.getMessage());
        }
    }

    private boolean isTickerApiUrlValid() {
        return TICKER_API_URL != null && !TICKER_API_URL.isEmpty();
    }

    /** Calls stock symbol API and returns JSONArray */
    private JSONArray getSymbolsArrayFromApi() throws IOException {
        final OkHttpClient client = new OkHttpClient();
        final String requestUrl = TICKER_API_URL + "?apikey=" + apiKey;
        final Request request = new Request.Builder().url(requestUrl).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                final JSONObject jsonResponse = new JSONObject(response.body().string());
                if (jsonResponse.has("data")) {
                    return jsonResponse.getJSONArray("data");
                } else {
                    throw new IOException("Missing 'data' field in response");
                }
            } else {
                throw new IOException("HTTP error: " + response.code());
            }
        }
    }

    
    /** Populates symbol-to-name mapping */
    private void populateStockSymbolMap(final JSONArray symbolsArray) {
        STOCK_SYMBOL_MAP.clear();

        for (int i = 0; i < symbolsArray.length(); i++) {
            final JSONObject stock = symbolsArray.getJSONObject(i);
            final String symbol = stock.getString("symbol");
            final String name = stock.getString("name");
            STOCK_SYMBOL_MAP.put(symbol, symbol + " - " + name);
        }
    }

    /** Used for symbol dropdown/autocomplete in UI */
    public static List<String> getStockSymbolList() {
        List<String> result;
        if (symbolsFetched) {
            result = new ArrayList<>(STOCK_SYMBOL_MAP.values());
        } else {
            result = Collections.emptyList();
        }
        return result;
    }

    
    /** Extracts symbol from selected item (symbol + company name) */
    public static String getSymbolFromSelection(final String selection) {
        String matchedSymbol = null;
        for (final Map.Entry<String, String> entry : STOCK_SYMBOL_MAP.entrySet()) {
            if (selection.equals(entry.getValue())) {
                matchedSymbol = entry.getKey();
                break;
            }
        }
        return matchedSymbol;
    }

    
    /** Fetches price/time series data for given stock symbol + timeframe */
    public List<List<String>> fetchStockData(final String symbol, final String timeframe) {
        LOGGER.info("Fetching stock data for: " + symbol + " | Timeframe: " + timeframe);

        final String interval = switch (timeframe) {
            case "Daily" -> "1day";
            case "Weekly" -> "1week";
            case "Monthly" -> "1month";
            default -> "1day";
        };

        final String url = BASE_URL + "?symbol=" + symbol + "&interval=" + interval +
                "&apikey=" + apiKey + "&outputsize=120";

        LOGGER.info("API Request URL: " + url);

        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url(url).build();

        List<List<String>> result;

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                result = parseJson(response.body().string());
            } else {
                result = Collections.emptyList();
            }
        } catch (IOException e) {
            LOGGER.severe("Exception while fetching stock data: " + e.getMessage());
            result = Collections.emptyList();
        }
    
        return result;
    }

    
    /** Parses JSON stock data response and normalizes features */
    private List<List<String>> parseJson(final String jsonData) {
        stockData.clear();
        trainingPrices.clear();
        gridPrices.clear();
    
        final List<List<String>> result;
    
        final JSONObject jsonObject = new JSONObject(jsonData);
        if (jsonObject.has("values")) {
            final JSONArray timeSeries = jsonObject.getJSONArray("values");
            final List<JSONObject> sortedData = timeSeries.toList().stream()
                    .map(obj -> new JSONObject((Map<?, ?>) obj))
                    .sorted(Comparator.comparing(o -> o.getString("datetime"), Comparator.reverseOrder()))
                    .collect(Collectors.toList());
    
            final int minRecords = 2;
            if (sortedData.size() < minRecords) {
                LOGGER.severe("Not enough records received (need at least 2, got " + sortedData.size() + ")");
                result = Collections.emptyList();
            } else {
                final List<Double> allClosePrices = new ArrayList<>();
                final List<double[]> allFeatures = new ArrayList<>();
    
                // Parse and extract required features (log transform on volume)
                for (final JSONObject dataPoint : sortedData.stream().limit(120).toList()) {
                    final double open = dataPoint.getDouble("open");
                    final double high = dataPoint.getDouble("high");
                    final double low = dataPoint.getDouble("low");
                    final double close = dataPoint.getDouble("close");
                    final double volume = dataPoint.getDouble("volume");
    
                    final List<String> row = new ArrayList<>(Arrays.asList(
                            dataPoint.getString("datetime"),
                            formatStockPrice(open),
                            formatStockPrice(high),
                            formatStockPrice(low),
                            formatStockPrice(close),
                            String.valueOf(volume)
                    ));
                    stockData.add(row);
    
                    allClosePrices.add(close);
                    final double safeVolume = Math.max(volume, 1);
                    allFeatures.add(new double[]{open, high, low, Math.log(safeVolume)});
                }
    
                // 80-20 train-test split
                final int splitIndex = (int) (allClosePrices.size() * 0.8);
                trainTargets = allClosePrices.subList(0, splitIndex);
                testTargets = allClosePrices.subList(splitIndex, allClosePrices.size());
    
                final List<double[]> rawTrain = allFeatures.subList(0, splitIndex);
                final List<double[]> rawTest = allFeatures.subList(splitIndex, allFeatures.size());
    
                final double[][][] scaled = normalizeTogether(rawTrain, rawTest);
                scaledTrainFeatures = new ArrayList<>(Arrays.asList(scaled[0]));
                scaledTestFeatures = new ArrayList<>(Arrays.asList(scaled[1]));
    
                if (scaledTestFeatures.isEmpty()) {
                    scaledLatestFeature = new double[]{0, 0, 0, 0};
                    LOGGER.severe("No test data found after scaling!");
                } else {
                    scaledLatestFeature = scaledTestFeatures.get(scaledTestFeatures.size() - 1);
                    LOGGER.info("Latest Features Vector: " + Arrays.toString(scaledLatestFeature));
                }
    
                trainingPrices.addAll(trainTargets);
                gridPrices.addAll(testTargets);
    
                LOGGER.info("Training Data (Scaled) Count: " + scaledTrainFeatures.size());
                LOGGER.info("Test Data (Scaled) Count: " + scaledTestFeatures.size());
    
                result = stockData.subList(0, Math.min(60, stockData.size()));
            }
        }
            else {
                LOGGER.severe("Invalid JSON response: No 'values' field found.");
                result = Collections.emptyList();
            }
        
    
        return result;
    }
    

    /** Applies Z-score normalization on all features */
    private double[][][] normalizeTogether(final List<double[]> train, final List<double[]> test) {
        final int numFeatures = train.get(0).length;
        final double[] mean = new double[numFeatures];
        final double[] std = new double[numFeatures];

        for (int i = 0; i < numFeatures; i++) {
            final int idx = i;
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

    private String formatStockPrice(final double price) {
        return String.format("%.2f", roundToTick(price));
    }

    private double roundToTick(final double price) {
        final double tickSize = 0.05;
        return Math.round(price / tickSize) * tickSize;
    }

    
    /** Exports the latest fetched stock data to CSV via a GUI file chooser */
    public void saveToCSV() {
        if (stockData.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No stock data to save!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        final JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Stock Data As...");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText("Save");
        fileChooser.setSelectedFile(new File("stock_data.csv"));

        final int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            final String filename = selectedFile.getAbsolutePath();
            if (!filename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
                selectedFile = new File(filename + ".csv");
            }

            try {
                CSVUtils.saveToCSV(selectedFile.getAbsolutePath(), stockData);
                JOptionPane.showMessageDialog(null, "Stock data saved successfully at:\n" + selectedFile.getAbsolutePath(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, "Error saving stock data!", "Error", JOptionPane.ERROR_MESSAGE);
                LOGGER.severe("Error saving CSV: " + e.getMessage());
            }
        }
    }

    public List<Double> getTrainingPrices() { return trainingPrices; }
    public List<Double> getGridPrices() { return gridPrices; }
    public List<double[]> getScaledTrainFeatures() { return scaledTrainFeatures; }
    public List<double[]> getScaledTestFeatures() { return scaledTestFeatures; }
    public List<Double> getTrainTargets() { return trainTargets; }
    public List<Double> getTestTargets() { return testTargets; }
    
    /** Used for real-time prediction on unseen/latest stock data */
    public double[] getLatestScaledFeatureVector() {
        return scaledLatestFeature != null ? Arrays.copyOf(scaledLatestFeature, scaledLatestFeature.length) : new double[0];
    }
}
