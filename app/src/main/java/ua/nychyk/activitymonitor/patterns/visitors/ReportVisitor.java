package ua.nychyk.activitymonitor.patterns.visitors;

import java.util.Map;

public interface ReportVisitor {
    Object visitDailyReport(Map<String, Object> data);
    Object visitPeriodicReport(Map<String, Object> data);
}
