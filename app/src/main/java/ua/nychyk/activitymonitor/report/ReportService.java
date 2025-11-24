package ua.nychyk.activitymonitor.report;

import ua.nychyk.activitymonitor.repositories.*;

import java.util.*;

public class ReportService {

    private final ProcessorRepository processorRepo;
    private final MemoryRepository memoryRepo;
    private final ComputerUsageRepository usageRepo;
    private final WindowRepository windowRepo;
    private final MonitoringDaysRepository daysRepo;

    public ReportService(
            ProcessorRepository processorRepo,
            MemoryRepository memoryRepo,
            ComputerUsageRepository usageRepo,
            WindowRepository windowRepo,
            MonitoringDaysRepository daysRepo
    ) {
        this.processorRepo = processorRepo;
        this.memoryRepo = memoryRepo;
        this.usageRepo = usageRepo;
        this.windowRepo = windowRepo;
        this.daysRepo = daysRepo;
    }

    // ============================================================
    //                        DAILY REPORT
    // ============================================================
    public Map<String, Object> getDailyReport(String day, int type) {
        Integer dateId = daysRepo.getDateId(day);
        if (dateId == null)
            return Map.of("error", "No data for day " + day);

        return switch (type) {

            case 1 -> Map.of(
                    "type", "CPU Usage by Hours",
                    "data", processorRepo.getUsageByDay(dateId)
            );

            case 2 -> Map.of(
                    "type", "Window Usage Percentage",
                    "data", windowRepo.getUsageByDay(dateId)
            );

            case 3 -> Map.of(
                    "type", "Memory Usage by Hours",
                    "data", memoryRepo.getUsageByDay(dateId)
            );

            case 4 -> Map.of(
                    "type", "Computer Uptime (seconds)",
                    "seconds", usageRepo.getDailyUptime(dateId)
            );

            case 5 -> Map.of(
                    "type", "Programs Used By Day",
                    "data", windowRepo.getUsageByDay(dateId)
            );

            default -> Map.of("error", "Unknown report type");
        };
    }

    // ============================================================
    //                      PERIODIC REPORT
    // ============================================================
    public Map<String, Object> getPeriodicReport(
            String start,
            String end,
            int type
    ) {
        List<Integer> dateIds = daysRepo.getDateRange(start, end);

        if (dateIds.isEmpty())
            return Map.of("error", "No data for this date range");

        return switch (type) {

            case 6 -> Map.of(
                    "type", "Average CPU Usage by Days",
                    "value", processorRepo.getAverageUsage(dateIds)
            );

            case 7 -> Map.of(
                    "type", "Average Memory Usage by Days",
                    "value", memoryRepo.getAverageUsage(dateIds)
            );

            default -> Map.of("error", "Unknown periodic report type");
        };
    }
}
