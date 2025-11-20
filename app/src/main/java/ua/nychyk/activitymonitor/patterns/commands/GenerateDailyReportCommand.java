package ua.nychyk.activitymonitor.patterns.commands;

import ua.nychyk.activitymonitor.patterns.visitors.ReportVisitor;
import ua.nychyk.activitymonitor.report.ReportService;

public class GenerateDailyReportCommand implements Command {

    private final ReportService reportService;
    private final String day;
    private final int reportType;

    public GenerateDailyReportCommand(
            ReportService reportService,
            String day,
            int reportType
    ) {
        this.reportService = reportService;
        this.day = day;
        this.reportType = reportType;
    }

    @Override
    public Object execute(ReportVisitor visitor) {
        var data = reportService.getDailyReport(day, reportType);
        return visitor.process(data);
    }
}
