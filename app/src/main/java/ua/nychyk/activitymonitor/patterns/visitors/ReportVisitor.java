package ua.nychyk.activitymonitor.patterns.visitors;

import java.util.Map;

public interface ReportVisitor {
    Object process(Map<String, Object> data);
}
