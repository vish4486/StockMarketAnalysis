package com.sdm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class ConfigLoader {
    private static final Properties PROPERTIES = new Properties();

    // Prevent instantiation as it is final class
    private ConfigLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            //System.out.println("Trying to load config from: " + ConfigLoader.class.getClassLoader().getResource("config.properties"));

            if (input == null) {
                throw new IllegalStateException("ERROR: `config.properties` file not found! Place it in `src/main/resources/`.");
            }
            PROPERTIES.load(input);
        } catch (IOException e) {
            throw new IllegalStateException(" ERROR: Failed to load `config.properties`!", e);
        }
    }

    public static String getApiKey() {
        //String apiKey = properties.getProperty("API_KEY");
        final String apiKey = PROPERTIES.getProperty("api.key");


        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException(" ERROR: API_KEY is missing! Set it in `config.properties`.");
        }

        return apiKey;
    }

    public static String getBaseUrl() {
        return PROPERTIES.getProperty("BASE_URL", "https://api.twelvedata.com/time_series");
    }

    public static String getTradingViewUrl() {
        return PROPERTIES.getProperty("TRADING_VIEW_URL", "https://www.tradingview.com/chart/");
    }

    /**
     * Retrieves the Ticker API URL from config.properties.
     * If not found, it returns the default TwelveData ticker API URL.
     */
    public static String getTickerApiUrl() {
        return PROPERTIES.getProperty("TICKER_API_URL", "https://api.twelvedata.com/symbols");
    }

    /**
     * Retrieves any property by key with an optional default value.
    */
    public static String getProperty(String key, String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }

    /**
     * Retrieves a required property by key.
    * Throws if not found.
    */
    public static String getProperty(String key) {
        final String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing required config key: " + key);
        }
       return value;
    }

    
}
