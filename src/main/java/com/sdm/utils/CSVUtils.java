package com.sdm.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import java.util.stream.Collectors;

public final class CSVUtils {

    // Prevent instantiation
    private CSVUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void saveToCSV(final String filename, final List<List<String>> stockData) throws IOException {
        final List<String> lines = stockData.stream()
            .map(row -> String.join(",", row))
            .collect(Collectors.toList());

        lines.add(0, "Date,Open,High,Low,Close,Volume"); // Add CSV header
        Files.write(Paths.get(filename), lines);
    }
}
