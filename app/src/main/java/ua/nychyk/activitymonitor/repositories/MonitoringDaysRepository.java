package ua.nychyk.activitymonitor.repositories;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MonitoringDaysRepository {

    private final DatabaseManager db;

    public MonitoringDaysRepository(DatabaseManager db) {
        this.db = db;
    }

    /** Повернути date_id або створити нову дату */
    public int getOrAddDateId(String date) {
        try (Connection conn = db.getConnection()) {

            // 1. Перевіряємо чи існує дата
            PreparedStatement check = conn.prepareStatement(
                    "SELECT date_id FROM MonitoringDates WHERE date = ?"
            );
            check.setString(1, date);
            ResultSet rs = check.executeQuery();

            if (rs.next()) return rs.getInt("date_id");

            // 2. Додаємо нову дату
            PreparedStatement insert = conn.prepareStatement(
                    "INSERT INTO MonitoringDates (date) VALUES (?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            insert.setString(1, date);
            insert.executeUpdate();

            ResultSet keys = insert.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    /** Повернути date_id (без створення) */
    public Integer getDateId(String date) {
        try (Connection conn = db.getConnection()) {

            PreparedStatement stmt = conn.prepareStatement(
                    "SELECT date_id FROM MonitoringDates WHERE date = ?"
            );
            stmt.setString(1, date);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt("date_id");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /** Діапазон дат -> список date_id */
    public List<Integer> getDateRange(String start, String end) {
        List<Integer> result = new ArrayList<>();

        String sql = """
                SELECT date_id
                FROM MonitoringDates
                WHERE date >= ? AND date <= ?
                ORDER BY date
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getInt("date_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
