package com.sdm.utils;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import static org.junit.jupiter.api.Assertions.*;

@Tag("unit")
class CSVUtilsTest {

    @TempDir
    Path tempDir; //  Temporary folder auto-deleted after test

    private File testCsvFile;
    private List<Vector<String>> sampleData;

    @BeforeEach
    void setup() {
        testCsvFile = tempDir.resolve("test.csv").toFile();
        sampleData = Arrays.asList(
                new Vector<>(Arrays.asList("Date", "Open", "High", "Low", "Close", "Volume")),
                new Vector<>(Arrays.asList("2024-02-23", "100", "105", "98", "102", "2000"))
        );
    }

    @Test
    void testSaveToCSV_successfulWrite() {
        assertDoesNotThrow(() -> CSVUtils.saveToCSV(testCsvFile.getAbsolutePath(), sampleData),
                "CSVUtils should write file without throwing exceptions");

        assertTrue(testCsvFile.exists(), "CSV file should be created");
        assertTrue(testCsvFile.length() > 0, "CSV file should not be empty");
    }

    @Test
    void testSaveToCSV_withEmptyData() {
        List<Vector<String>> emptyData = List.of();

        assertDoesNotThrow(() -> CSVUtils.saveToCSV(testCsvFile.getAbsolutePath(), emptyData),
                "Saving an empty CSV should still succeed");

        assertTrue(testCsvFile.exists(), "CSV file should still be created even with empty data");
    }

    @Test
    @Tag("unit")
    void testSaveToCSV_invalidPathThrows() {
    List<Vector<String>> data = Arrays.asList(
        new Vector<>(Arrays.asList("2024-02-23", "100", "105", "98", "102", "2000"))
    );

    assertThrows(IOException.class, () -> {
        CSVUtils.saveToCSV("/invalid\\path/<>file.csv", data);
    }, "IOException expected for invalid file path");
}

}
