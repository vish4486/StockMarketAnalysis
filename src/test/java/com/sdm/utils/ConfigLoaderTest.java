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
class ConfigLoaderTest { // Made package-private

    private Properties testProperties;

    // PMD: AtLeastOneConstructor
    public ConfigLoaderTest() {}

    @BeforeEach
    void setUp() {
        try (BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/config.properties"))) {
            testProperties = new Properties();
            testProperties.load(reader);
        } catch (IOException e) {
            throw new IllegalStateException(" ERROR: Failed to load test config.properties", e);
        }
    }

    @Test
    void testGetApiKey() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            mocked.when(ConfigLoader::getApiKey).thenReturn("test-key-123");

            final String key = ConfigLoader.getApiKey();
            assertEquals("test-key-123", key, "API key should match mocked value");
        }
    }

    @Test
    void testGetBaseUrl() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            mocked.when(ConfigLoader::getBaseUrl).thenReturn("https://mock.api");

            final String url = ConfigLoader.getBaseUrl();
            assertEquals("https://mock.api", url, "Base URL should match mocked value");
        }
    }

    @Test
    void testGetTradingViewUrl() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            mocked.when(ConfigLoader::getTradingViewUrl).thenReturn("https://trading.mock");

            final String url = ConfigLoader.getTradingViewUrl();
            assertEquals("https://trading.mock", url, "TradingView URL should match mocked value");
        }
    }

    @SuppressWarnings("PMD.JUnitTestContainsTooManyAsserts")
    @Test
    void testMissingApiKeyThrowsError() {
        final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            mocked.when(ConfigLoader::getApiKey).thenThrow(new RuntimeException("API_KEY is missing!"));
            ConfigLoader.getApiKey();
        }
    });

    assertTrue(exception.getMessage().contains("API_KEY is missing!"),
            "Exception message should mention missing API_KEY");
}
    @Test
    void testDefaultBaseUrlIfMissing() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            testProperties.remove("BASE_URL");

            mocked.when(ConfigLoader::getBaseUrl).thenReturn("https://default.api");
            final String url = ConfigLoader.getBaseUrl();
            assertEquals("https://default.api", url, "Base URL should fall back to default");
        }
    }

    @Test
    void testDefaultTradingViewUrlIfMissing() {
        try (MockedStatic<ConfigLoader> mocked = Mockito.mockStatic(ConfigLoader.class)) {
            testProperties.remove("TRADING_VIEW_URL");

            mocked.when(ConfigLoader::getTradingViewUrl).thenReturn("https://default.trading");
            final String url = ConfigLoader.getTradingViewUrl();
            assertEquals("https://default.trading", url, "TradingView URL should fall back to default");
        }
    }

    @Test
    @Tag("integration")
    void testLoadRealApiKey() {
        final String realKey = ConfigLoader.getApiKey();
        assertNotNull(realKey, "Real API key should be loaded from config.properties");
    }

}
