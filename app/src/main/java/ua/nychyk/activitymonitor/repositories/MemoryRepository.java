package ua.nychyk.activitymonitor.repositories;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MemoryRepository {

    private final DatabaseManager db;

    public MemoryRepository(DatabaseManager db) {
        this.db = db;
    }

    public void insertMemoryUsage(int dateId, String timestamp, int usedMb) {

        String sql = """
            INSERT INTO MemoryUsage (date_id, timestamp, memory_usage_mb)
            VALUES (?, ?, ?)
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dateId);
            ps.setString(2, timestamp);
            ps.setInt(3, usedMb);

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertMemoryUsage(int usedMb) {
        try (Connection conn = db.getConnection()) {

            int dateId = getOrCreateDateId(conn);
            String timestamp = java.time.LocalTime.now()
                    .withNano(0)
                    .toString();

            insertMemoryUsage(dateId, timestamp, usedMb);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    //  ReportService → getUsageByDay(dateId)

    public List<Map<String, Object>> getUsageByDay(int dateId) {
        List<Map<String, Object>> out = new ArrayList<>();

        String sql = "SELECT timestamp, memory_usage_mb FROM MemoryUsage WHERE date_id = ? ORDER BY timestamp";

        try (Connection conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dateId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("time", rs.getString("timestamp"));
                    item.put("value", rs.getDouble("memory_usage_mb"));
                    out.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }


    public double getAverageUsage(List<Integer> dateIds) {

        double sum = 0;
        int count = 0;

        String sql = "SELECT memory_usage_mb FROM MemoryUsage WHERE date_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int dateId : dateIds) {
                ps.setInt(1, dateId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sum += rs.getInt("memory_usage_mb");
                        count++;
                    }
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (count == 0) return 0;
        return sum / count;
    }

    private int getOrCreateDateId(Connection conn) throws SQLException {
        String today = java.time.LocalDate.now().toString();

        String select = "SELECT date_id FROM MonitoringDates WHERE date = ?";
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, today);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        String insert = "INSERT INTO MonitoringDates (date) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, today);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Cannot create MonitoringDates record for: " + today);
    }
}
