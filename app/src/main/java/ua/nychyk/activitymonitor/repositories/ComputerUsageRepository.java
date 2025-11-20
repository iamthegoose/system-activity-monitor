package ua.nychyk.activitymonitor.repositories;

import java.sql.*;

public class ComputerUsageRepository {

    private final DatabaseManager db;

    public ComputerUsageRepository(DatabaseManager db) {
        this.db = db;
    }

    public void saveComputerUsage(int seconds) {
        String date = java.time.LocalDate.now().toString();
        int hour = java.time.LocalDateTime.now().getHour();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO computer_usage (date, hour, active_seconds) VALUES (?, ?, ?)"
             )) {

            stmt.setString(1, date);
            stmt.setInt(2, hour);
            stmt.setInt(3, seconds);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
