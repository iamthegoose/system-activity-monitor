package ua.nychyk.activitymonitor.patterns.visitors;

import com.google.gson.Gson;
import java.util.Map;

public class JSONReportVisitor implements ReportVisitor {

    private final Gson gson = new Gson();

    @Override
    public Object process(Map<String, Object> data) {
        return gson.toJson(data);
    }
}
