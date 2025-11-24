package ua.nychyk.activitymonitor.repositories;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProcessorRepository {

    private final DatabaseManager db;

    public ProcessorRepository(DatabaseManager db) {
        this.db = db;
    }

    // =====================================================
    //   ЗБЕРЕЖЕННЯ ДАНИХ (викликається CpuMonitor)
    // =====================================================
    public void saveCpuUsage(int dateId, String timestamp, double cpuUsage) {
        String sql = "INSERT INTO ProcessorUsage (date_id, timestamp, cpu_usage) VALUES (?, ?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dateId);
            ps.setString(2, timestamp);
            ps.setDouble(3, cpuUsage);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Оверсімпліфайд метод, який викликає твій CpuMonitor
    public void saveCpuUsage(double cpuUsage) {
        try (Connection conn = db.getConnection()) {

            String today = LocalDate.now().toString();
            int dateId = getOrCreateDateId(conn, today);

            String timestamp = LocalTime.now()
                    .withNano(0)
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            saveCpuUsage(dateId, timestamp, cpuUsage);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =====================================================
    //   ВИКЛИКАЄ ReportService → getUsageByDay(dateId)
    // =====================================================
    public List<Double> getUsageByDay(int dateId) {
        List<Double> out = new ArrayList<>();

        String sql = "SELECT cpu_usage FROM ProcessorUsage WHERE date_id = ? ORDER BY timestamp";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dateId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(rs.getDouble("cpu_usage"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return out;
    }

    // =====================================================
    //   AVERAGE (ReportService → type 6)
    // =====================================================
    public double getAverageUsage(List<Integer> dateIds) {
        double sum = 0;
        int count = 0;

        String sql = "SELECT cpu_usage FROM ProcessorUsage WHERE date_id = ?";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int dateId : dateIds) {
                ps.setInt(1, dateId);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        sum += rs.getDouble("cpu_usage");
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

    // =====================================================
    //   ДОПОМІЖНЕ
    // =====================================================
    private int getOrCreateDateId(Connection conn, String date) throws SQLException {

        String select = "SELECT date_id FROM MonitoringDates WHERE date = ?";

        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, date);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        }

        String insert = "INSERT INTO MonitoringDates (date) VALUES (?)";

        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, date);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        }

        throw new SQLException("getOrCreateDateId() failed for date=" + date);
    }
}
