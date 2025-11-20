package ua.nychyk.activitymonitor.patterns.visitors;

import java.util.Map;

public class TextReportVisitor implements ReportVisitor {

    @Override
    public Object process(Map<String, Object> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== TEXT REPORT ===\n");
        
        data.forEach((key, value) -> 
            sb.append(key).append(": ").append(value).append("\n")
        );

        return sb.toString();
    }
}
