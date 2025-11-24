package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.repositories.MemoryRepository;
import ua.nychyk.activitymonitor.repositories.MonitoringDaysRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MemoryMonitor implements Monitor {

    private final Label guiLabel;
    private final MemoryRepository memoryRepo;
    private final MonitoringDaysRepository daysRepo;

    public MemoryMonitor(Label guiLabel, MemoryRepository memoryRepo, MonitoringDaysRepository daysRepo) {
        this.guiLabel = guiLabel;
        this.memoryRepo = memoryRepo;
        this.daysRepo = daysRepo;
    }

    @Override
    public void updateWidget() {
        MemoryInfo mem = getMacMemory();

        double percent = (double) mem.usedMB / mem.totalMB * 100.0;

        Platform.runLater(() -> guiLabel.setText(
                String.format("Memory: %d MB / %d MB (%.2f%%)",
                        mem.usedMB, mem.totalMB, percent)
        ));
    }

    @Override
    public void saveData() {
        MemoryInfo mem = getMacMemory();
        int usedMB = mem.usedMB;

        String today = java.time.LocalDate.now().toString();
        int dateId = daysRepo.getOrAddDateId(today);

        String timestamp = getCurrentHourTimestamp();

        memoryRepo.insertMemoryUsage(dateId, timestamp, usedMB);
    }

    private String getCurrentHourTimestamp() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        now = now.withMinute(0).withSecond(0).withNano(0);
        return now.toLocalTime().toString();
    }

    @Override
    public boolean getActivityFlag() {
        return false;
    }

    private MemoryInfo getMacMemory() {
        try {
            long totalBytes = runCommandAndParseLong("sysctl", "-n", "hw.memsize");
            long totalMB = totalBytes / (1024 * 1024);

            String vmOutput = runCommandAndGetOutput("vm_stat");

            long active = 0, wired = 0, compressed = 0;
            long pageSize = 16384;

            for (String line : vmOutput.split("\n")) {
                line = line.trim();

                if (line.startsWith("Pages active"))
                    active = extractVmNumber(line);

                if (line.startsWith("Pages wired down"))
                    wired = extractVmNumber(line);

                if (line.startsWith("Pages occupied by compressor"))
                    compressed = extractVmNumber(line);
            }

            long usedBytes = (active + wired + compressed) * pageSize;
            long usedMB = usedBytes / (1024 * 1024);

            return new MemoryInfo((int) usedMB, (int) totalMB);

        } catch (Exception e) {
            e.printStackTrace();
            return new MemoryInfo(0, 1);
        }
    }

    // ---- Generic ProcessBuilder caller ----

    private long runCommandAndParseLong(String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        String line = reader.readLine();
        proc.waitFor();

        return Long.parseLong(line.trim());
    }

    private String runCommandAndGetOutput(String... cmd) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);

        Process proc = pb.start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }

        proc.waitFor();
        return sb.toString();
    }

    private long extractVmNumber(String line) {
        String num = line.replaceAll("[^0-9]", "");
        return Long.parseLong(num);
    }

    private static class MemoryInfo {
        final int usedMB;
        final int totalMB;

        MemoryInfo(int used, int total) {
            this.usedMB = used;
            this.totalMB = total;
        }
    }
}
