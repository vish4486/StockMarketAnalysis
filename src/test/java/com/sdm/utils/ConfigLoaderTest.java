package com.sdm.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit") //  Categorize all tests in this class as unit tests
public class ConfigLoaderTest {

    private Properties testProperties;

    @BeforeEach
    void setUp() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/config.properties"))) {
            testProperties = new Properties();
            testProperties.load(reader);
        } catch (IOException e) {
            throw new RuntimeException(" ERROR: Failed to load test config.properties", e);
        }
    }

    @Test
    void testGetApiKey() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            mocked.when(ConfigLoader::getApiKey).thenReturn("test-key-123");

            String key = ConfigLoader.getApiKey();
            assertEquals("test-key-123", key);
        }
    }

    @Test
    void testGetBaseUrl() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            mocked.when(ConfigLoader::getBaseUrl).thenReturn("https://mock.api");

            String url = ConfigLoader.getBaseUrl();
            assertEquals("https://mock.api", url);
        }
    }

    @Test
    void testGetTradingViewUrl() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            mocked.when(ConfigLoader::getTradingViewUrl).thenReturn("https://trading.mock");

            String url = ConfigLoader.getTradingViewUrl();
            assertEquals("https://trading.mock", url);
        }
    }

    @Test
    void testMissingApiKeyThrowsError() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            mocked.when(ConfigLoader::getApiKey).thenThrow(new RuntimeException("API_KEY is missing!"));

            Exception ex = assertThrows(RuntimeException.class, ConfigLoader::getApiKey);
            assertTrue(ex.getMessage().contains("API_KEY is missing!"));
        }
    }

    @Test
    void testDefaultBaseUrlIfMissing() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            testProperties.remove("BASE_URL");

            mocked.when(ConfigLoader::getBaseUrl).thenReturn("https://default.api");
            String url = ConfigLoader.getBaseUrl();

            assertEquals("https://default.api", url);
        }
    }

    @Test
    void testDefaultTradingViewUrlIfMissing() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            testProperties.remove("TRADING_VIEW_URL");

            mocked.when(ConfigLoader::getTradingViewUrl).thenReturn("https://default.trading");
            String url = ConfigLoader.getTradingViewUrl();

            assertEquals("https://default.trading", url);
        }
    }
}
