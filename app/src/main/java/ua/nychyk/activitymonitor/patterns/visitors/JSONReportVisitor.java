package ua.nychyk.activitymonitor.patterns.visitors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Map;

public class JSONReportVisitor implements ReportVisitor {

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .create();

    @Override
    public Object visitDailyReport(Map<String, Object> data) {
        return gson.toJson(data);
    }

    @Override
    public Object visitPeriodicReport(Map<String, Object> data) {
        return gson.toJson(data);
    }
}
