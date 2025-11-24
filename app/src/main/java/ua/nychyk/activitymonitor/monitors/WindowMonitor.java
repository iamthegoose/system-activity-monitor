package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.repositories.WindowRepository;
import ua.nychyk.activitymonitor.repositories.MonitoringDaysRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class WindowMonitor implements ActivityAwareMonitor {

    private final Label guiLabel;
    private final WindowRepository repo;
    private final MonitoringDaysRepository daysRepo;

    private String currentWindow = "unknown";
    private int activeSeconds = 0;

    public WindowMonitor(Label guiLabel, WindowRepository repo, MonitoringDaysRepository daysRepo) {
        this.guiLabel = guiLabel;
        this.repo = repo;
        this.daysRepo = daysRepo;
    }

    @Override
    public void updateWidget() {
        String title = getActiveWindowTitle();
        if (title == null || title.isBlank()) {
            title = "Activity Monitor"; // FIX: показуємо замість unknown
        }
        currentWindow = title;

        Platform.runLater(() ->
                guiLabel.setText("Active Window: " + currentWindow)
        );
    }

    @Override
    public void saveData() {
        try {
            String today = LocalDate.now().toString();
            int dateId = daysRepo.getOrAddDateId(today);

            int windowId = repo.getOrAddWindowId(currentWindow);

            String timeStr = secondsToTime(activeSeconds);

            repo.saveWindowUsage(dateId, windowId, timeStr);
        } catch (Exception e) {
            System.err.println("Failed to save window usage: " + e.getMessage());
        }

        activeSeconds = 0;
    }

    @Override
    public boolean getActivityFlag() {
        return false;
    }

    @Override
    public void checkActivity(boolean isActiveNow) {
        if (isActiveNow) {
            activeSeconds++;
        }
    }

    private String secondsToTime(int seconds) {
        LocalTime t = LocalTime.ofSecondOfDay(seconds);
        return t.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    private String getActiveWindowTitle() {
        try {
            Process proc = Runtime.getRuntime().exec(
                    new String[]{
                            "osascript",
                            "-e",
                            "tell application \"System Events\"",
                            "-e",
                            "set frontApp to first application process whose frontmost is true",
                            "-e",
                            "set winTitle to name of window 1 of frontApp",
                            "-e",
                            "return winTitle",
                            "-e",
                            "end tell"
                    }
            );

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(proc.getInputStream())
            );

            String line = reader.readLine();
            proc.waitFor();

            return line;

        } catch (Exception e) {
            return "Activity Monitor";
        }
    }
}
