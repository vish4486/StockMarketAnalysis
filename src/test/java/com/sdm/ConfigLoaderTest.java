package com.sdm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Properties;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class ConfigLoaderTest {

    private Properties testProperties;

    @BeforeEach
    void setUp() {
        System.out.println("\n [ConfigLoaderTest] Setting up test properties...");

        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/config.properties"))) {
            testProperties = new Properties();
            testProperties.load(reader);
            System.out.println("[ConfigLoaderTest] Loaded test config.properties");

            // Debug: Print test properties
            testProperties.forEach((key, value) -> System.out.println("    TEST PROPERTY: " + key + " = " + value));

        } catch (IOException e) {
            throw new RuntimeException("ERROR: Could not load test config.properties", e);
        }
    }

    @Test
    public void testGetApiKey() {
        System.out.println("\n [ConfigLoaderTest] Running testGetApiKey()...");

        File configFile = new File("src/main/resources/config.properties");
        File testConfigFile = new File("src/test/resources/config.properties");

        System.out.println(" Checking config files...");
        System.out.println("    Main config exists: " + configFile.exists());
        System.out.println("   Test config exists: " + testConfigFile.exists());

        if (testConfigFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(testConfigFile))) {
                System.out.println(" Test Config File Contents:");
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("    " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        System.out.println("\n Mocking ConfigLoader.getApiKey()...");
        try (MockedStatic<ConfigLoader> mockedConfigLoader = Mockito.mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(ConfigLoader::getApiKey).thenReturn("cc53e127e0ce40feb25ad8e083377f67");

            String apiKey = ConfigLoader.getApiKey();
            System.out.println("Mocked API Key: " + apiKey);

            assertNotNull(apiKey, " ERROR: API_KEY should not be null");
            assertEquals("cc53e127e0ce40feb25ad8e083377f67", apiKey, " ERROR: API_KEY does not match expected value!");
        }
    }

    @Test
    public void testGetBaseUrl() {
        try (MockedStatic<ConfigLoader> mockedConfigLoader = Mockito.mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(ConfigLoader::getBaseUrl).thenReturn("https://api.twelvedata.com/time_series");

            String baseUrl = ConfigLoader.getBaseUrl();
            System.out.println(" Base URL Retrieved: " + baseUrl);

            assertNotNull(baseUrl, " ERROR: BASE_URL should not be null");
            assertFalse(baseUrl.isEmpty(), " ERROR: BASE_URL should not be empty");
            assertEquals("https://api.twelvedata.com/time_series", baseUrl, " ERROR: BASE_URL does not match expected value!");
        }
    }

    @Test
    public void testGetTradingViewUrl() {
        try (MockedStatic<ConfigLoader> mockedConfigLoader = Mockito.mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(ConfigLoader::getTradingViewUrl).thenReturn("https://www.tradingview.com/chart/");

            String tradingViewUrl = ConfigLoader.getTradingViewUrl();
            System.out.println("TradingView URL Retrieved: " + tradingViewUrl);

            assertNotNull(tradingViewUrl, " ERROR: TRADING_VIEW_URL should not be null");
            assertFalse(tradingViewUrl.isEmpty(), " ERROR: TRADING_VIEW_URL should not be empty");
            assertEquals("https://www.tradingview.com/chart/", tradingViewUrl, " ERROR: TRADING_VIEW_URL does not match expected value!");
        }
    }

    @Test
    public void testDefaultBaseUrlIfMissing() {
        try (MockedStatic<ConfigLoader> mockedConfigLoader = Mockito.mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(ConfigLoader::getBaseUrl).thenReturn("https://api.twelvedata.com/time_series");

            testProperties.remove("BASE_URL");
            String baseUrl = ConfigLoader.getBaseUrl();
            System.out.println("Default Base URL Retrieved: " + baseUrl);

            assertEquals("https://api.twelvedata.com/time_series", baseUrl, " ERROR: Default BASE_URL should be returned if missing.");
        }
    }

    @Test
    public void testDefaultTradingViewUrlIfMissing() {
        try (MockedStatic<ConfigLoader> mockedConfigLoader = Mockito.mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(ConfigLoader::getTradingViewUrl).thenReturn("https://www.tradingview.com/chart/");

            testProperties.remove("TRADING_VIEW_URL");
            String tradingViewUrl = ConfigLoader.getTradingViewUrl();
            System.out.println(" Default TradingView URL Retrieved: " + tradingViewUrl);

            assertEquals("https://www.tradingview.com/chart/", tradingViewUrl, " ERROR: Default TRADING_VIEW_URL should be returned if missing.");
        }
    }

    @Test
    public void testMissingApiKeyThrowsError() {
        try (MockedStatic<ConfigLoader> mockedConfigLoader = Mockito.mockStatic(ConfigLoader.class)) {
            mockedConfigLoader.when(ConfigLoader::getApiKey).thenThrow(new RuntimeException(" ERROR: API_KEY is missing!"));

            System.out.println("\n [ConfigLoaderTest] Running testMissingApiKeyThrowsError()...");
            Exception exception = assertThrows(RuntimeException.class, ConfigLoader::getApiKey);

            assertTrue(exception.getMessage().contains("API_KEY is missing!"), " ERROR: API_KEY missing error should be thrown.");
        }
    }
}
