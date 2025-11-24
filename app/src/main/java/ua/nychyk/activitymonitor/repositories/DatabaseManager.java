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


    //                CREATE TABLES IF NOT EXISTS

    private void createTables() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // ---------------- MonitoringDates ----------------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS MonitoringDates (
                    date_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date TEXT NOT NULL UNIQUE
                );
            """);

            // ---------------- ProcessorUsage ----------------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ProcessorUsage (
                    procusage_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date_id INTEGER NOT NULL,
                    timestamp TEXT NOT NULL,
                    cpu_usage REAL NOT NULL,
                    FOREIGN KEY (date_id) REFERENCES MonitoringDates(date_id)
                );
            """);

            // ---------------- MemoryUsage ----------------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS MemoryUsage (
                    memusage_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date_id INTEGER NOT NULL,
                    timestamp TEXT NOT NULL,
                    memory_usage_mb REAL NOT NULL,
                    FOREIGN KEY (date_id) REFERENCES MonitoringDates(date_id)
                );
            """);

            // ---------------- ComputerUsage ----------------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ComputerUsage (
                    compusage_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date_id INTEGER NOT NULL,
                    time TEXT NOT NULL,
                    FOREIGN KEY (date_id) REFERENCES MonitoringDates(date_id)
                );
            """);

            // ---------------- Windows ----------------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS Windows (
                    window_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE
                );
            """);

            // ---------------- WindowUsage ----------------
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS WindowUsage (
                    winusage_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    date_id INTEGER NOT NULL,
                    window_id INTEGER NOT NULL,
                    time TEXT NOT NULL,
                    FOREIGN KEY (date_id) REFERENCES MonitoringDates(date_id),
                    FOREIGN KEY (window_id) REFERENCES Windows(window_id)
                );
            """);

            System.out.println("Tables created/verified successfully");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
