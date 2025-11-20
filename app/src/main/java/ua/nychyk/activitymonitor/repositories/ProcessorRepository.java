package ua.nychyk.activitymonitor.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class ProcessorRepository {

    private final DatabaseManager db;

    public ProcessorRepository(DatabaseManager db) {
        this.db = db;
    }

    public void saveCpuUsage(double cpu) {
        String date = LocalDateTime.now().toString();
        int hour = LocalDateTime.now().getHour();

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO processor_usage (date, hour, cpu) VALUES (?, ?, ?)"
             )) {

            stmt.setString(1, date);
            stmt.setInt(2, hour);
            stmt.setDouble(3, cpu);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
