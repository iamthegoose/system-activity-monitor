package ua.nychyk.activitymonitor.patterns.visitors;

import java.util.*;

public class TextReportVisitor implements ReportVisitor {

    @SuppressWarnings("unchecked")
    private static <T> T cast(Object o) {
        return (T) o;
    }

    @Override
    public Object visitDailyReport(Map<String, Object> data) {

        if (data.containsKey("error"))
            return "ERROR: " + data.get("error");

        StringBuilder sb = new StringBuilder();
        sb.append("===== DAILY REPORT =====\n");
        sb.append("Type: ").append(data.get("type")).append("\n\n");

        // 1) data → List<Map<String,Object>> (CPU / RAM / Windows)
        if (data.containsKey("data")) {
            Object raw = data.get("data");

            if (raw instanceof List<?> list) {
                for (Object o : list) {
                    Map<String, Object> row = cast(o);

                    if (row.containsKey("time") && row.containsKey("value")) {
                        sb.append(row.get("time"))
                          .append(" | ")
                          .append(row.get("value"))
                          .append("\n");
                    }
                    else if (row.containsKey("window") && row.containsKey("time")) {
                        sb.append(row.get("time"))
                          .append(" | ")
                          .append(row.get("window"))
                          .append("\n");
                    }
                    else if (row.containsKey("window")) {
                        sb.append(row.get("window")).append("\n");
                    }
                }
            }
        }

        // 2) programs → List<String>
        if (data.containsKey("programs")) {
            List<String> programs = cast(data.get("programs"));

            for (String p : programs)
                sb.append(p).append("\n");
        }

        // 3) uptime
        if (data.containsKey("seconds")) {
            sb.append("\nUptime: ").append(data.get("seconds")).append(" seconds");
        }

        return sb.toString();
    }

    @Override
    public Object visitPeriodicReport(Map<String, Object> data) {

        if (data.containsKey("error"))
            return "ERROR: " + data.get("error");

        StringBuilder sb = new StringBuilder();
        sb.append("===== PERIODIC REPORT =====\n");
        sb.append("Type: ").append(data.get("type")).append("\n");

        if (data.containsKey("period")) {
            Map<String, Object> period = cast(data.get("period"));

            sb.append("Period: ")
              .append(period.get("start"))
              .append(" → ")
              .append(period.get("end"))
              .append("\n\n");
        } else {
            sb.append("\n");
        }

        String type = (String) data.get("type");

        switch (type) {
            case "CPU Usage by Hours" ->
                    formatCpuOrMemory(sb, cast(data.get("data")));

            case "Memory Usage by Hours" ->
                    formatCpuOrMemory(sb, cast(data.get("data")));

            case "Programs Usage Time" ->
                    formatWindowTime(sb, cast(data.get("data")));

            case "Computer Uptime by Day(s)" ->
                    formatUptime(sb, data);

            case "Programs Used by Day(s)" ->
                    formatPrograms(sb, cast(data.get("data")));

            default -> sb.append("Raw: ").append(data);
        }

        return sb.toString();
    }

    // HELPERS


    private void formatCpuOrMemory(StringBuilder sb, Map<String, Object> byDay) {
        for (var entry : byDay.entrySet()) {
            sb.append(entry.getKey()).append(":\n");

            List<Map<String, Object>> list = cast(entry.getValue());

            for (Map<String, Object> row : list) {
                sb.append("  ")
                  .append(row.get("time"))
                  .append(" | ")
                  .append(row.get("value"))
                  .append("\n");
            }

            sb.append("\n");
        }
    }

    private void formatWindowTime(StringBuilder sb, List<Map<String, Object>> list) {
        for (Map<String, Object> row : list) {
            sb.append(row.get("window"))
              .append(" — ")
              .append(row.get("hhmmss"))
              .append(" (")
              .append(row.get("seconds"))
              .append(" sec)")
              .append("\n");
        }
    }

    private void formatUptime(StringBuilder sb, Map<String, Object> data) {

        if (data.containsKey("days")) {
            Map<String, Object> days = cast(data.get("days"));

            for (var e : days.entrySet()) {
                sb.append(e.getKey())
                  .append(" — ")
                  .append(e.getValue())
                  .append(" sec\n");
            }
            sb.append("\n");
        }

        if (data.containsKey("totalSeconds"))
            sb.append("Total: ")
              .append(data.get("totalSeconds"))
              .append(" sec (")
              .append(data.get("totalFormatted"))
              .append(")\n");
    }

    private void formatPrograms(StringBuilder sb, Map<String, Object> byDay) {
        for (var entry : byDay.entrySet()) {
            sb.append(entry.getKey()).append(":\n");

            List<String> list = cast(entry.getValue());

            for (String p : list)
                sb.append("  - ").append(p).append("\n");

            sb.append("\n");
        }
    }
}
