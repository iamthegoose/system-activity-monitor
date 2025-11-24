package ua.nychyk.activitymonitor.patterns.commands;

import ua.nychyk.activitymonitor.patterns.visitors.ReportVisitor;
import ua.nychyk.activitymonitor.report.ReportService;

public class GenerateDailyReportCommand implements Command {

    private final ReportService service;
    private final String day;
    private final int reportType;

    public GenerateDailyReportCommand(ReportService service, String day, int reportType) {
        this.service = service;
        this.day = day;
        this.reportType = reportType;
    }

    @Override
    public Object execute(ReportVisitor visitor) {
        var data = service.getDailyReport(day, reportType);
        return visitor.visitDailyReport(data);
    }
}
