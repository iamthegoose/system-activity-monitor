package ua.nychyk.activitymonitor.repositories;

import java.sql.*;
import java.util.*;

public class WindowRepository {

    private final DatabaseManager db;

    public WindowRepository(DatabaseManager db) {
        this.db = db;
    }

    public void saveWindowUsage(int dateId, int windowId, String addedTime) {

        try (Connection conn = db.getConnection()) {

            String checkSql = """
                SELECT time FROM WindowUsage
                WHERE date_id = ? AND window_id = ?
            """;

            PreparedStatement check = conn.prepareStatement(checkSql);
            check.setInt(1, dateId);
            check.setInt(2, windowId);

            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                String oldTime = rs.getString("time");

                String newTime = sumTimes(oldTime, addedTime);

                String updateSql = """
                    UPDATE WindowUsage
                    SET time = ?
                    WHERE date_id = ? AND window_id = ?
                """;

                PreparedStatement update = conn.prepareStatement(updateSql);
                update.setString(1, newTime);
                update.setInt(2, dateId);
                update.setInt(3, windowId);
                update.executeUpdate();

            } else {
                String insertSql = """
                    INSERT INTO WindowUsage (date_id, window_id, time)
                    VALUES (?, ?, ?)
                """;

                PreparedStatement insert = conn.prepareStatement(insertSql);
                insert.setInt(1, dateId);
                insert.setInt(2, windowId);
                insert.setString(3, addedTime);
                insert.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getOrAddWindowId(String windowName) {
        try (Connection conn = db.getConnection()) {

            String checkSql = "SELECT window_id FROM Windows WHERE name = ?";
            PreparedStatement check = conn.prepareStatement(checkSql);
            check.setString(1, windowName);

            ResultSet rs = check.executeQuery();
            if (rs.next()) return rs.getInt("window_id");

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

    public List<Map<String, Object>> getUsageByDay(int dateId) {
        List<Map<String, Object>> out = new ArrayList<>();

        String sql = """
            SELECT W.name, U.time
            FROM WindowUsage U
            JOIN Windows W ON U.window_id = W.window_id
            WHERE U.date_id = ?
            ORDER BY U.time DESC
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dateId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("time", rs.getString("time"));
                    item.put("window", rs.getString("name"));
                    out.add(item);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return out;
    }

    private String sumTimes(String t1, String t2) {

        int s1 = toSeconds(t1);
        int s2 = toSeconds(t2);
        int sum = s1 + s2;

        return toHHMMSS(sum);
    }

    private int toSeconds(String t) {
        String[] p = t.split(":");
        int h = Integer.parseInt(p[0]);
        int m = Integer.parseInt(p[1]);
        int s = Integer.parseInt(p[2]);
        return h * 3600 + m * 60 + s;
    }

    private String toHHMMSS(int sec) {
        int h = sec / 3600;
        int m = (sec % 3600) / 60;
        int s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
    public List<String> getDistinctWindows(int dateId) {
        List<String> out = new ArrayList<>();

        String sql = """
            SELECT DISTINCT W.name
            FROM WindowUsage U
            JOIN Windows W ON U.window_id = W.window_id
            WHERE U.date_id = ?
            ORDER BY W.name
        """;

        try (Connection conn = db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, dateId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                out.add(rs.getString("name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }

}
