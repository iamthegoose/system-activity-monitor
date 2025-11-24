package ua.nychyk.activitymonitor.repositories;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WindowRepository {

    private final DatabaseManager db;

    public WindowRepository(DatabaseManager db) {
        this.db = db;
    }

    // =======================================================
    //                 ЗБЕРЕЖЕННЯ ДАНИХ
    // =======================================================

    /** зберегти використання вікна */
    public void saveWindowUsage(int dateId, int windowId, String time) {
        String sql = """
                INSERT INTO WindowUsage (date_id, window_id, time)
                VALUES (?, ?, ?)
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dateId);
            stmt.setInt(2, windowId);
            stmt.setString(3, time);
            stmt.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** додати у таблицю Windows — якщо нема */
    public int getOrAddWindowId(String windowName) {
        try (Connection conn = db.getConnection()) {

            // 1. Перевіряємо чи вже є
            String checkSql = "SELECT window_id FROM Windows WHERE name = ?";
            PreparedStatement check = conn.prepareStatement(checkSql);
            check.setString(1, windowName);

            ResultSet rs = check.executeQuery();
            if (rs.next()) return rs.getInt("window_id");

            // 2. Якщо немає — додаємо
            String insertSql = "INSERT INTO Windows (name) VALUES (?)";
            PreparedStatement insert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insert.setString(1, windowName);
            insert.executeUpdate();

            ResultSet keys = insert.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    // =======================================================
    //                  МЕТОДИ ДЛЯ ЗВІТІВ
    // =======================================================

    /** Повертає список назв вікон, використаних за день */
    public List<String> getUsageByDay(int dateId) {
        List<String> result = new ArrayList<>();

        String sql = """
                SELECT W.name
                FROM WindowUsage U
                JOIN Windows W ON U.window_id = W.window_id
                WHERE U.date_id = ?
                ORDER BY U.time
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, dateId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                result.add(rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
