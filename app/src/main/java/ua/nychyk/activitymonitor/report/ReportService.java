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

    //                        DAILY REPORT
    
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
                    "type", "Programs Usage Time",
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
                    "type", "Programs Used by Day",
                    "programs", windowRepo.getDistinctWindows(dateId)
            );

            default -> Map.of("error", "Unknown report type");
        };
    }

    //                      PERIODIC REPORT 
    public Map<String, Object> getPeriodicReport(
            String start,
            String end,
            int type
    ) {
        List<Integer> dateIds = daysRepo.getDateRange(start, end);

        if (dateIds.isEmpty())
            return Map.of("error", "No data for this date range");

        return switch (type) {

            case 1 -> generateCpuPeriod(start, end, dateIds);

            case 2 -> generateWindowTimePeriod(start, end, dateIds);

            case 3 -> generateMemoryPeriod(start, end, dateIds);

            case 4 -> generateUptimePeriod(start, end, dateIds);

            case 5 -> generateProgramsUsedPeriod(start, end, dateIds);

            default -> Map.of("error", "Unknown periodic report type");
        };
    }

    //                   PERIODIC CPU
    private Map<String, Object> generateCpuPeriod(
            String start, String end, List<Integer> dateIds
    ) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "CPU Usage by Hours");
        map.put("period", Map.of("start", start, "end", end));

        Map<String, Object> data = new LinkedHashMap<>();

        for (int dateId : dateIds) {
            String day = daysRepo.getDateById(dateId);
            data.put(day, processorRepo.getUsageByDay(dateId));
        }

        map.put("data", data);
        return map;
    }

    //                 PERIODIC WINDOW USAGE TIME
    private Map<String, Object> generateWindowTimePeriod(
            String start, String end, List<Integer> dateIds
    ) {
        Map<String, Integer> totals = new HashMap<>();

        for (int dateId : dateIds) {
            var rows = windowRepo.getUsageByDay(dateId);

            for (var row : rows) {
                String wnd = (String) row.get("window");
                String time = (String) row.get("time");

                int sec = toSeconds(time);

                totals.put(wnd, totals.getOrDefault(wnd, 0) + sec);
            }
        }

        // Convert to formatted strings
        List<Map<String, Object>> formatted = new ArrayList<>();

        totals.forEach((wnd, sec) -> {
            formatted.add(Map.of(
                    "window", wnd,
                    "seconds", sec,
                    "hhmmss", toHHMMSS(sec)
            ));
        });

        formatted.sort((a, b) -> Integer.compare(
                (int) b.get("seconds"),
                (int) a.get("seconds")
        ));

        return Map.of(
                "type", "Programs Usage Time",
                "period", Map.of("start", start, "end", end),
                "data", formatted
        );
    }

    // ============================================================
    //                PERIODIC MEMORY
    // ============================================================
    private Map<String, Object> generateMemoryPeriod(
            String start, String end, List<Integer> dateIds
    ) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "Memory Usage by Hours");
        map.put("period", Map.of("start", start, "end", end));

        Map<String, Object> data = new LinkedHashMap<>();

        for (int dateId : dateIds) {
            String day = daysRepo.getDateById(dateId);
            data.put(day, memoryRepo.getUsageByDay(dateId));
        }

        map.put("data", data);
        return map;
    }

    // ============================================================
    //                PERIODIC UPTIME
    // ============================================================
    private Map<String, Object> generateUptimePeriod(
            String start, String end, List<Integer> dateIds
    ) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "Computer Uptime by Day(s)");
        map.put("period", Map.of("start", start, "end", end));

        Map<String, Integer> perDay = new LinkedHashMap<>();
        int total = 0;

        for (int dateId : dateIds) {
            String day = daysRepo.getDateById(dateId);
            int sec = usageRepo.getDailyUptime(dateId);

            perDay.put(day, sec);
            total += sec;
        }

        map.put("days", perDay);
        map.put("totalSeconds", total);
        map.put("totalFormatted", toHHMMSS(total));

        return map;
    }

    // ============================================================
    //              PERIODIC PROGRAMS USED
    // ============================================================
    private Map<String, Object> generateProgramsUsedPeriod(
            String start, String end, List<Integer> dateIds
    ) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", "Programs Used by Day(s)");
        map.put("period", Map.of("start", start, "end", end));

        Map<String, Object> byDay = new LinkedHashMap<>();

        for (int dateId : dateIds) {
            String day = daysRepo.getDateById(dateId);
            byDay.put(day, windowRepo.getDistinctWindows(dateId));
        }

        map.put("data", byDay);
        return map;
    }


    // ============================================================
    //              HELPERS
    // ============================================================
    private int toSeconds(String t) {
        String[] p = t.split(":");
        return Integer.parseInt(p[0]) * 3600
                + Integer.parseInt(p[1]) * 60
                + Integer.parseInt(p[2]);
    }

    private String toHHMMSS(int sec) {
        int h = sec / 3600;
        int m = (sec % 3600) / 60;
        int s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
