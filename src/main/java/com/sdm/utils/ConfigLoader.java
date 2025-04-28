package com.sdm.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Loads configuration from a properties file at startup.
 * Provides safe access to configuration values.
 */
public final class ConfigLoader {

    private static final Logger LOGGER = Logger.getLogger(ConfigLoader.class.getName());
    private static final Properties PROPERTIES = new Properties();

    private ConfigLoader() {
        throw new UnsupportedOperationException("Utility class");
    }

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new IllegalStateException("config.properties not found. Please place it under src/main/resources.");
            }
            PROPERTIES.load(input);
            LOGGER.info("Configuration loaded successfully.");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load config.properties", e);
        }
    }

    public static String getApiKey() {
        final String apiKey = PROPERTIES.getProperty("api.key");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("API key (api.key) is missing in config.properties.");
        }
        return apiKey;
    }

    public static String getBaseUrl() {
        return PROPERTIES.getProperty("BASE_URL", "https://api.twelvedata.com/time_series");
    }

    public static String getTradingViewUrl() {
        return PROPERTIES.getProperty("TRADING_VIEW_URL", "https://www.tradingview.com/chart/");
    }

    public static String getTickerApiUrl() {
        return PROPERTIES.getProperty("TICKER_API_URL", "https://api.twelvedata.com/symbols");
    }

    public static String getProperty(final String key) {
        final String value = PROPERTIES.getProperty(key);
        if (value == null) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value;
    }

    public static String getProperty(final String key, final String defaultValue) {
        return PROPERTIES.getProperty(key, defaultValue);
    }
}
