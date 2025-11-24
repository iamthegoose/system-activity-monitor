package ua.nychyk.activitymonitor.patterns.visitors;

import java.util.Map;

public class TextReportVisitor implements ReportVisitor {

    @Override
    public Object visitDailyReport(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== DAILY REPORT ===\n");
        data.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        return sb.toString();
    }

    @Override
    public Object visitPeriodicReport(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== PERIODIC REPORT ===\n");
        data.forEach((k, v) -> sb.append(k).append(": ").append(v).append("\n"));
        return sb.toString();
    }
}
