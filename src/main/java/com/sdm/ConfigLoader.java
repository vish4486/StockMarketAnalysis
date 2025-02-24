package com.sdm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {
    private static final Properties properties = new Properties();

    static {
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                throw new RuntimeException("ERROR: `config.properties` file not found! Place it in `src/main/resources/`.");
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException(" ERROR: Failed to load `config.properties`!", e);
        }
    }

    public static String getApiKey() {
        //String apiKey = properties.getProperty("API_KEY");
        String apiKey = properties.getProperty("api.key");


        if (apiKey == null || apiKey.isEmpty()) {
            throw new RuntimeException(" ERROR: API_KEY is missing! Set it in `config.properties`.");
        }

        return apiKey;
    }

    public static String getBaseUrl() {
        return properties.getProperty("BASE_URL", "https://api.twelvedata.com/time_series");
    }

    public static String getTradingViewUrl() {
        return properties.getProperty("TRADING_VIEW_URL", "https://www.tradingview.com/chart/");
    }
    
}
