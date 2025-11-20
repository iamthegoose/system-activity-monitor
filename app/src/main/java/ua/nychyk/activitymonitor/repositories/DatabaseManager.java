package ua.nychyk.activitymonitor.repositories;

import java.sql.*;

public class DatabaseManager {

    private final String dbFile;

    public DatabaseManager(String dbFile) {
        this.dbFile = dbFile;
        createTables();
    }

    private Connection connect() throws SQLException {
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile);
    }

    public Connection getConnection() throws SQLException {
        return connect();
    }

    // ============================================================
    //                CREATE ALL TABLES (LIKE PYTHON VERSION)
    // ============================================================
    private void createTables() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // CPU usage
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS processor_usage (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT,
                    hour INTEGER,
                    cpu REAL
                );
            """);

            // Memory usage
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS memory_usage (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT,
                    hour INTEGER,
                    memory REAL
                );
            """);

            // Computer activity
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS computer_usage (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT,
                    hour INTEGER,
                    active_seconds INTEGER
                );
            """);

            // Window usage
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS window_usage (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT,
                    hour INTEGER,
                    window_name TEXT,
                    seconds INTEGER
                );
            """);

            // Monitoring days
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS monitoring_days (
                    date TEXT PRIMARY KEY
                );
            """);

            System.out.println("Tables created/verified successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
