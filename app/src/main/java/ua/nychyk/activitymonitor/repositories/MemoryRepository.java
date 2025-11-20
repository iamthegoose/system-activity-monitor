package ua.nychyk.activitymonitor.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class MemoryRepository {

    private final DatabaseManager db;

    public MemoryRepository(DatabaseManager db) {
        this.db = db;
    }

    public void saveMemoryUsage(double memory) {
        String date = LocalDateTime.now().toString();
        int hour = LocalDateTime.now().getHour();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO memory_usage (date, hour, memory) VALUES (?, ?, ?)"
             )) {

            stmt.setString(1, date);
            stmt.setInt(2, hour);
            stmt.setDouble(3, memory);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
