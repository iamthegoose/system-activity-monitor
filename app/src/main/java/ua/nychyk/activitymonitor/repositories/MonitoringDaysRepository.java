package ua.nychyk.activitymonitor.repositories;

import java.sql.*;

public class MonitoringDaysRepository {

    private final DatabaseManager db;

    public MonitoringDaysRepository(DatabaseManager db) {
        this.db = db;
    }

    public void markDay(String date) {
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT OR IGNORE INTO monitoring_days (date) VALUES (?)"
             )) {

            stmt.setString(1, date);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
