package ua.nychyk.activitymonitor.repositories;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ComputerUsageRepository {

    private final DatabaseManager db;

    public ComputerUsageRepository(DatabaseManager db) {
        this.db = db;
    }

    // -------------------------------------------------------------
    //  ВАРІАНТ, ЯКИЙ ВИКОРИСТОВУЄ reportService — НЕ ЧІПАЄМО
    // -------------------------------------------------------------
    public void saveComputerUsage(int dateId, String time) {
        String sql = "INSERT INTO ComputerUsage (date_id, time) VALUES (?, ?)";

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dateId);
            ps.setString(2, time);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------
    //  НОВИЙ МЕТОД, ЯКИЙ ВИКЛИКАЄ ComputerUsageMonitor
    // -------------------------------------------------------------
    public void saveComputerUsage(int activeSeconds) {
        if (activeSeconds <= 0) return;

        try (Connection conn = db.getConnection()) {

            // отримуємо date_id
            String today = LocalDate.now().toString();
            int dateId = getOrCreateDateId(conn, today);

            // час збереження
            String time = LocalTime.now()
                    .withNano(0)
                    .format(DateTimeFormatter.ofPattern("HH:mm:ss"));

            // вставка в таблицю
            String sql = "INSERT INTO ComputerUsage (date_id, time) VALUES (?, ?)";

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, dateId);
                ps.setString(2, time);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------
    //  ДОПОМІЖНЕ: отримати або створити MonitoringDates.date_id
    // -------------------------------------------------------------
    private int getOrCreateDateId(Connection conn, String date) throws SQLException {

        String select = "SELECT date_id FROM MonitoringDates WHERE date = ?";

        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("date_id");
            }
        }

        String insert = "INSERT INTO MonitoringDates (date) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, date);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }

        throw new SQLException("Failed to create or read MonitoringDates for date = " + date);
    }
    public int getDailyUptime(int dateId) {
    String sql = "SELECT time FROM ComputerUsage WHERE date_id = ?";

    try (var conn = db.getConnection();
         var stmt = conn.prepareStatement(sql)) {

        stmt.setInt(1, dateId);

        var rs = stmt.executeQuery();
        int seconds = 0;

        while (rs.next()) {
            String timeStr = rs.getString("time"); // format HH:MM:SS
            String[] parts = timeStr.split(":");

            int h = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            int s = Integer.parseInt(parts[2]);

            seconds += h * 3600 + m * 60 + s;
        }

        return seconds;

    } catch (Exception e) {
        e.printStackTrace();
        return 0;
    }
}

}
