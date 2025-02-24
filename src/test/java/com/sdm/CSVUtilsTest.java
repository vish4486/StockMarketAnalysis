package com.sdm;
import java.util.List;
import java.util.Arrays;
import java.util.Vector;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;
import java.io.IOException;


class CSVUtilsTest {
    @Test
    void testSaveToCSV() {
        List<Vector<String>> data = Arrays.asList(
                new Vector<>(Arrays.asList("2024-02-23", "100", "105", "98", "102", "2000"))
        );
        assertDoesNotThrow(() -> {
            try {
                CSVUtils.saveToCSV("test.csv", data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, "CSV should be written successfully");
    }
}