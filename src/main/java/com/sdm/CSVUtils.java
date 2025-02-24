package com.sdm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class CSVUtils {
    public static void saveToCSV(String filename, List<Vector<String>> stockData) throws IOException {
        List<String> lines = stockData.stream()
            .map(row -> String.join(",", row))
            .collect(Collectors.toList());

        lines.add(0, "Date,Open,High,Low,Close,Volume"); // Add CSV header
        Files.write(Paths.get(filename), lines);
    }
}
