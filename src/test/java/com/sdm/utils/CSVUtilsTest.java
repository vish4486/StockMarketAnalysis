package com.sdm.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CSVUtilsTest {

    // default access: used by JUnit for injecting temporary directory
    @TempDir
    Path tempDir;

    private File testCsvFile;
    private List<List<String>> sampleData;

    public CSVUtilsTest() {}

    @BeforeEach
    void setup() {
        testCsvFile = tempDir.resolve("test.csv").toFile();
        sampleData = List.of(
            List.of("Date", "Open", "High", "Low", "Close", "Volume"),
            List.of("2024-02-23", "100", "105", "98", "102", "2000")
        );
    }

    @Test
    void testSaveToCsvSuccessfulWrite() {
        assertAll("CSV file writing assertions",
            () -> assertDoesNotThrow(() -> CSVUtils.saveToCSV(testCsvFile.getAbsolutePath(), sampleData),
                    "CSVUtils should write file without throwing exceptions"),
            () -> assertTrue(testCsvFile.exists(), "CSV file should be created"),
            () -> assertTrue(testCsvFile.length() > 0, "CSV file should not be empty")
        );
    }

    @Test
    void testSaveToCsvWithEmptyData() {
        final List<List<String>> emptyData = List.of();

        assertAll("Empty CSV file writing",
            () -> assertDoesNotThrow(() -> CSVUtils.saveToCSV(testCsvFile.getAbsolutePath(), emptyData),
                    "Saving an empty CSV should still succeed"),
            () -> assertTrue(testCsvFile.exists(), "CSV file should still be created even with empty data")
        );
    }

    @Test
    void testSaveToCsvInvalidPathThrows() {
        final List<List<String>> data = List.of(
            List.of("2024-02-23", "100", "105", "98", "102", "2000")
        );

        assertThrows(IOException.class,
            () -> CSVUtils.saveToCSV("/invalid\\path/<>file.csv", data),
            "IOException expected for invalid file path"
        );
    }
}
