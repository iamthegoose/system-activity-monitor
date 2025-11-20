package ua.nychyk.activitymonitor.report;

import ua.nychyk.activitymonitor.repositories.DatabaseManager;

import java.sql.*;
import java.util.*;

public class ReportService {

    private final DatabaseManager db;

    public ReportService(String dbFile) {
        this.db = new DatabaseManager(dbFile);
    }

    // =====================================================================
    //                           DAILY REPORT
    // =====================================================================

    public Map<String, Object> getDailyReport(String day, int type) {
        return switch (type) {
            case 1 -> cpuByHours(day);
            case 2 -> browserUsage(day);
            case 3 -> memoryByHours(day);
            case 4 -> uptimeByDay(day);
            case 5 -> programsByDay(day);
            case 6 -> avgCpuForRange(day, day);     // daily → same day range
            case 7 -> avgMemoryForRange(day, day);  // daily → same day range
            default -> Map.of("error", "Unknown report type");
        };
    }

    // =====================================================================
    //                           PERIODIC REPORT
    // =====================================================================

    public Map<String, Object> getPeriodicReport(String start, String end, int type) {
        return switch (type) {
            case 1 -> cpuByHoursRange(start, end);
            case 2 -> browserUsageRange(start, end);
            case 3 -> memoryByHoursRange(start, end);
            case 4 -> uptimeRange(start, end);
            case 5 -> programsRange(start, end);
            case 6 -> avgCpuForRange(start, end);
            case 7 -> avgMemoryForRange(start, end);
            default -> Map.of("error", "Unknown report type");
        };
    }

    // =====================================================================
    //                                TYPE 1
    //                      CPU Usage by Hours (Daily)
    // =====================================================================

    private Map<String, Object> cpuByHours(String day) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT hour, AVG(cpu) AS avg_cpu
            FROM processor_usage
            WHERE date LIKE ?
            GROUP BY hour
            ORDER BY hour;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, day + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "hour", rs.getInt("hour"),
                        "avg_cpu", rs.getDouble("avg_cpu")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("date", day);
        result.put("rows", rows);
        return result;
    }

    // CPU for range (same logic)
    private Map<String, Object> cpuByHoursRange(String start, String end) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT date, hour, AVG(cpu) AS avg_cpu
            FROM processor_usage
            WHERE date BETWEEN ? AND ?
            GROUP BY date, hour
            ORDER BY date, hour;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "date", rs.getString("date"),
                        "hour", rs.getInt("hour"),
                        "avg_cpu", rs.getDouble("avg_cpu")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("start", start);
        result.put("end", end);
        result.put("rows", rows);
        return result;
    }

    // =====================================================================
    //                                TYPE 2
    //                      Browser Usage % (Daily)
    // =====================================================================

    private Map<String, Object> browserUsage(String day) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT window_name, SUM(seconds) AS total_sec
            FROM window_usage
            WHERE date = ?
              AND (window_name LIKE '%Chrome%' OR window_name LIKE '%Safari%' OR window_name LIKE '%Firefox%' OR window_name LIKE '%Browser%')
            GROUP BY window_name
            ORDER BY total_sec DESC
            LIMIT 5;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, day);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "window_name", rs.getString("window_name"),
                        "seconds", rs.getInt("total_sec")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("date", day);
        result.put("rows", rows);
        return result;
    }

    private Map<String, Object> browserUsageRange(String start, String end) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT window_name, SUM(seconds) AS total_sec
            FROM window_usage
            WHERE date BETWEEN ? AND ?
              AND (window_name LIKE '%Chrome%' OR window_name LIKE '%Safari%' OR window_name LIKE '%Firefox%' OR window_name LIKE '%Browser%')
            GROUP BY window_name
            ORDER BY total_sec DESC
            LIMIT 10;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "window_name", rs.getString("window_name"),
                        "seconds", rs.getInt("total_sec")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("start", start);
        result.put("end", end);
        result.put("rows", rows);
        return result;
    }

    // =====================================================================
    //                                TYPE 3
    //                      Memory Usage by Hours
    // =====================================================================

    private Map<String, Object> memoryByHours(String day) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT hour, AVG(memory) AS avg_memory
            FROM memory_usage
            WHERE date LIKE ?
            GROUP BY hour
            ORDER BY hour;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, day + "%");

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "hour", rs.getInt("hour"),
                        "avg_memory", rs.getDouble("avg_memory")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("date", day);
        result.put("rows", rows);
        return result;
    }

    private Map<String, Object> memoryByHoursRange(String start, String end) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT date, hour, AVG(memory) AS avg_memory
            FROM memory_usage
            WHERE date BETWEEN ? AND ?
            GROUP BY date, hour
            ORDER BY date, hour;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "date", rs.getString("date"),
                        "hour", rs.getInt("hour"),
                        "avg_memory", rs.getDouble("avg_memory")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("start", start);
        result.put("end", end);
        result.put("rows", rows);
        return result;
    }

    // =====================================================================
    //                                TYPE 4
    //                          Uptime (Daily)
    // =====================================================================

    private Map<String, Object> uptimeByDay(String day) {
        Map<String, Object> result = new HashMap<>();

        String sql = """
            SELECT SUM(active_seconds) AS total
            FROM computer_usage
            WHERE date = ?;
        """;

        int total = 0;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, day);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) total = rs.getInt("total");

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("date", day);
        result.put("uptime_seconds", total);
        return result;
    }

    private Map<String, Object> uptimeRange(String start, String end) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT date, SUM(active_seconds) AS total
            FROM computer_usage
            WHERE date BETWEEN ? AND ?
            GROUP BY date
            ORDER BY date;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "date", rs.getString("date"),
                        "uptime_seconds", rs.getInt("total")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("start", start);
        result.put("end", end);
        result.put("rows", rows);
        return result;
    }

    // =====================================================================
    //                                TYPE 5
    //                     Programs Used by Day(s)
    // =====================================================================

    private Map<String, Object> programsByDay(String day) {
        Map<String, Object> result = new HashMap<>();
        List<String> programs = new ArrayList<>();

        String sql = """
            SELECT DISTINCT window_name
            FROM window_usage
            WHERE date = ?
            ORDER BY window_name;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, day);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) programs.add(rs.getString("window_name"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("date", day);
        result.put("programs", programs);
        return result;
    }

    private Map<String, Object> programsRange(String start, String end) {
        Map<String, Object> result = new HashMap<>();
        List<String> programs = new ArrayList<>();

        String sql = """
            SELECT DISTINCT window_name
            FROM window_usage
            WHERE date BETWEEN ? AND ?
            ORDER BY window_name;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) programs.add(rs.getString("window_name"));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("start", start);
        result.put("end", end);
        result.put("programs", programs);
        return result;
    }

    // =====================================================================
    //                             TYPE 6–7
    //                     Average CPU / Memory by Days
    // =====================================================================

    private Map<String, Object> avgCpuForRange(String start, String end) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT date, AVG(cpu) AS avg_cpu
            FROM processor_usage
            WHERE date BETWEEN ? AND ?
            GROUP BY date
            ORDER BY date;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "date", rs.getString("date"),
                        "avg_cpu", rs.getDouble("avg_cpu")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("start", start);
        result.put("end", end);
        result.put("rows", rows);
        return result;
    }

    private Map<String, Object> avgMemoryForRange(String start, String end) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> rows = new ArrayList<>();

        String sql = """
            SELECT date, AVG(memory) AS avg_memory
            FROM memory_usage
            WHERE date BETWEEN ? AND ?
            GROUP BY date
            ORDER BY date;
        """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, start);
            stmt.setString(2, end);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                rows.add(Map.of(
                        "date", rs.getString("date"),
                        "avg_memory", rs.getDouble("avg_memory")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        result.put("start", start);
        result.put("end", end);
        result.put("rows", rows);
        return result;
    }
}
