package com.sdm.stock;

import java.sql.*;

public class StockDatabase {
    private static final String DB_URL = "jdbc:sqlite:stock_data.db";

    /**
     * Creates the stocks table if it does not exist.
     */
    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS stocks (" +
                         "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                         "symbol TEXT NOT NULL, " +
                         "date TEXT NOT NULL, " +
                         "open REAL, " +
                         "high REAL, " +
                         "low REAL, " +
                         "close REAL, " +
                         "volume INTEGER)";
            stmt.executeUpdate(sql);
            System.out.println("✅ Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }
    }

    /**
     * Inserts stock data into the database.
     */
    public static void insertStockData(String symbol, String date, double open, double high, double low, double close, int volume) {
        String sql = "INSERT INTO stocks (symbol, date, open, high, low, close, volume) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, symbol);
            pstmt.setString(2, date);
            pstmt.setDouble(3, open);
            pstmt.setDouble(4, high);
            pstmt.setDouble(5, low);
            pstmt.setDouble(6, close);
            pstmt.setInt(7, volume);
            pstmt.executeUpdate();
            System.out.println("✅ Inserted stock data for " + symbol + " on " + date);
        } catch (SQLException e) {
            System.err.println("❌ Insert error: " + e.getMessage());
        }
    }
}

