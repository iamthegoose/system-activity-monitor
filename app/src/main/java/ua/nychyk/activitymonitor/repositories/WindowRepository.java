package ua.nychyk.activitymonitor.repositories;

import java.sql.*;

public class WindowRepository {

    private final DatabaseManager db;

    public WindowRepository(DatabaseManager db) {
        this.db = db;
    }

    public void saveWindowUsage(String windowName, int seconds) {
        String date = java.time.LocalDate.now().toString();
        int hour = java.time.LocalDateTime.now().getHour();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO window_usage (date, hour, window_name, seconds) VALUES (?, ?, ?, ?)"
             )) {

            stmt.setString(1, date);
            stmt.setInt(2, hour);
            stmt.setString(3, windowName);
            stmt.setInt(4, seconds);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
