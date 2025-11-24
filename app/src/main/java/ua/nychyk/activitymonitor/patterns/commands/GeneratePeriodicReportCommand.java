package ua.nychyk.activitymonitor.patterns.commands;

import ua.nychyk.activitymonitor.patterns.visitors.ReportVisitor;
import ua.nychyk.activitymonitor.report.ReportService;

public class GeneratePeriodicReportCommand implements Command {

    private final ReportService service;
    private final String start;
    private final String end;
    private final int reportType;

    public GeneratePeriodicReportCommand(ReportService service, String start, String end, int reportType) {
        this.service = service;
        this.start = start;
        this.end = end;
        this.reportType = reportType;
    }

    @Override
    public Object execute(ReportVisitor visitor) {
        var data = service.getPeriodicReport(start, end, reportType);
        return visitor.visitPeriodicReport(data);
    }
}
