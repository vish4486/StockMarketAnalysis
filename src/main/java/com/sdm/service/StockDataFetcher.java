package com.sdm.service;
import com.sdm.utils.ConfigLoader;
import com.sdm.utils.CSVUtils;
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
    private static final String BASE_URL = ConfigLoader.getBaseUrl();
    private static final String TICKER_API_URL = ConfigLoader.getTickerApiUrl();
    private final String API_KEY;

    private final List<Vector<String>> stockData = new ArrayList<>();

    private final List<Double> trainingPrices = new ArrayList<>();
    private final List<Double> gridPrices = new ArrayList<>();

    private List<double[]> scaledTrainFeatures = new ArrayList<>();
    private List<double[]> scaledTestFeatures = new ArrayList<>();
    private List<Double> trainTargets = new ArrayList<>();
    private List<Double> testTargets = new ArrayList<>();
    private double[] scaledLatestFeature;

    private static final Map<String, String> stockSymbolMap = new LinkedHashMap<>();
    private static boolean symbolsFetched = false;

    public StockDataFetcher() {
        this.API_KEY = ConfigLoader.getApiKey();
        if (!symbolsFetched) {
            fetchStockSymbols();
        }
    }

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
                stockSymbolMap.put(symbol, symbol + " - " + name);
            }

            symbolsFetched = true;
            System.out.println("Stock symbols fetched successfully. Total: " + stockSymbolMap.size());

        } catch (Exception e) {
            System.err.println("ERROR: Failed to fetch stock symbols: " + e.getMessage());
        }
    }

    public static List<String> getStockSymbolList() {
        if (!symbolsFetched) {
            System.err.println(" WARNING: Stock symbols were not fetched successfully!");
            return Collections.emptyList();
        }
        return new ArrayList<>(stockSymbolMap.values());
    }

    public static String getSymbolFromSelection(String selection) {
        for (Map.Entry<String, String> entry : stockSymbolMap.entrySet()) {
            if (selection.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
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

        System.out.println(" API Request URL: " + url);

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println(" API Request Failed: " + response.message());
                return Collections.emptyList();
            }

            // Uncomment to debug raw API response
            // System.out.println(" API Response Body:\n" + response.body().string());

            return parseJson(response.body().string());

        } catch (Exception e) {
            System.err.println(" Exception while fetching stock data:");
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Vector<String>> parseJson(String jsonData) {
        stockData.clear();
        trainingPrices.clear();
        gridPrices.clear();
        

       scaledTrainFeatures = new ArrayList<>();
       scaledTestFeatures = new ArrayList<>();
       trainTargets = new ArrayList<>();
       testTargets = new ArrayList<>();
   

        JSONObject jsonObject = new JSONObject(jsonData);
        if (!jsonObject.has("values")) {
            System.err.println("Invalid JSON response: No 'values' field found.");
            return Collections.emptyList();
        }

        JSONArray timeSeries = jsonObject.getJSONArray("values");

        List<JSONObject> sortedData = timeSeries.toList().stream()
                .map(obj -> new JSONObject((Map<?, ?>) obj))
                .sorted(Comparator.comparing(o -> o.getString("datetime"), Comparator.reverseOrder()))
                .collect(Collectors.toList());

        if (sortedData.size() < 2) {
            System.err.println(" Not enough records received (need at least 2, got " + sortedData.size() + ")");
            return Collections.emptyList();
        }

        System.out.println(" Total records received from API: " + sortedData.size());

        List<Double> allClosePrices = new ArrayList<>();
        List<double[]> allFeatures = new ArrayList<>();

        for (JSONObject dataPoint : sortedData.stream().limit(120).toList()) {
            double open = dataPoint.getDouble("open");
            double high = dataPoint.getDouble("high");
            double low = dataPoint.getDouble("low");
            double close = dataPoint.getDouble("close");
            double volume = dataPoint.getDouble("volume");

            Vector<String> row = new Vector<>(Arrays.asList(
                    dataPoint.getString("datetime"),
                    formatStockPrice(open),
                    formatStockPrice(high),
                    formatStockPrice(low),
                    formatStockPrice(close),
                    String.valueOf(volume)
            ));
            stockData.add(row);

            allClosePrices.add(close);
            //allFeatures.add(new double[]{open, high, low, volume});
            double safeVolume = Math.max(volume, 1); // avoid log(0)
            allFeatures.add(new double[]{open, high, low, Math.log(safeVolume)});

        }

        int splitIndex = (int) (allClosePrices.size() * 0.8);
        trainTargets = allClosePrices.subList(0, splitIndex);
        testTargets = allClosePrices.subList(splitIndex, allClosePrices.size());

        List<double[]> rawTrain = allFeatures.subList(0, splitIndex);
        List<double[]> rawTest = allFeatures.subList(splitIndex, allFeatures.size());

        double[][][] scaled = normalizeTogether(rawTrain, rawTest);
       // scaledTrainFeatures = Arrays.asList(scaled[0]);
       // scaledTestFeatures = Arrays.asList(scaled[1]);
       scaledTrainFeatures = new ArrayList<>(Arrays.asList(scaled[0]));
       scaledTestFeatures = new ArrayList<>(Arrays.asList(scaled[1]));


        if (!scaledTestFeatures.isEmpty()) {
            scaledLatestFeature = scaledTestFeatures.get(scaledTestFeatures.size() - 1);
            System.out.println(" Latest Features Vector: " + Arrays.toString(scaledLatestFeature));
        } else {
            scaledLatestFeature = new double[]{0, 0, 0, 0};
            System.err.println(" No test data found after scaling!");
        }

        trainingPrices.addAll(trainTargets); // legacy fallback
        gridPrices.addAll(testTargets);

        System.out.println(" Training Data (Scaled) Count: " + scaledTrainFeatures.size());
        System.out.println(" Test Data (Scaled) Count: " + scaledTestFeatures.size());

        return stockData.subList(0, Math.min(60, stockData.size()));
    }

    private double[][][] normalizeTogether(List<double[]> train, List<double[]> test) {
        int numFeatures = train.get(0).length;
        double[] mean = new double[numFeatures];
        double[] std = new double[numFeatures];

        for (int i = 0; i < numFeatures; i++) {
            final int idx = i;
            mean[i] = train.stream().mapToDouble(x -> x[idx]).average().orElse(0);
            std[i] = Math.sqrt(train.stream().mapToDouble(x -> Math.pow(x[idx] - mean[idx], 2)).average().orElse(1));
        }

        double[][] trainScaled = train.stream().map(x -> scaleRow(x, mean, std)).toArray(double[][]::new);
        double[][] testScaled = test.stream().map(x -> scaleRow(x, mean, std)).toArray(double[][]::new);

        return new double[][][]{trainScaled, testScaled};
    }

    private double[] scaleRow(double[] row, double[] mean, double[] std) {
        double[] result = new double[row.length];
        for (int i = 0; i < row.length; i++) {
            result[i] = (row[i] - mean[i]) / std[i];
        }
        return result;
    }

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

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Stock Data As...");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setApproveButtonText("Save");
        fileChooser.setSelectedFile(new File("stock_data.csv"));

        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();

            if (!selectedFile.getAbsolutePath().toLowerCase().endsWith(".csv")) {
                selectedFile = new File(selectedFile.getAbsolutePath() + ".csv");
            }

            try {
                CSVUtils.saveToCSV(selectedFile.getAbsolutePath(), stockData);
                JOptionPane.showMessageDialog(null, " Stock data saved successfully at:\n" + selectedFile.getAbsolutePath(),
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(null, " Error saving stock data!", "Error", JOptionPane.ERROR_MESSAGE);
                System.err.println(" Error saving CSV: " + e.getMessage());
            }
        }
    }

    // === Getters ===
    public List<Double> getTrainingPrices() {
        return trainingPrices;
    }

    public List<Double> getGridPrices() {
        return gridPrices;
    }

    public List<double[]> getScaledTrainFeatures() {
        return scaledTrainFeatures;
    }

    public List<double[]> getScaledTestFeatures() {
        return scaledTestFeatures;
    }

    public List<Double> getTrainTargets() {
        return trainTargets;
    }

    public List<Double> getTestTargets() {
        return testTargets;
    }

    public double[] getLatestScaledFeatureVector() {
        return scaledLatestFeature;
    }
}