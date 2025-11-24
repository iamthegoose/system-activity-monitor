package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.repositories.WindowRepository;
import ua.nychyk.activitymonitor.repositories.MonitoringDaysRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.LocalDate;

public class WindowMonitor implements ActivityAwareMonitor {

    private final Label guiLabel;
    private final WindowRepository repo;
    private final MonitoringDaysRepository daysRepo;

    private String lastWindow = null;
    private int secondsInCurrentWindow = 0;

    public WindowMonitor(Label guiLabel, WindowRepository repo, MonitoringDaysRepository daysRepo) {
        this.guiLabel = guiLabel;
        this.repo = repo;
        this.daysRepo = daysRepo;
    }

    @Override
    public void updateWidget() {
        String title = getActiveAppName();   // FIX → ім'я програми, не вкладки

        if (title == null || title.isBlank())
            return;

        if (lastWindow != null && !lastWindow.equals(title)) {
            saveWindow(lastWindow, secondsInCurrentWindow);
            secondsInCurrentWindow = 0;
        }

        lastWindow = title;

        Platform.runLater(() ->
                guiLabel.setText("Active Window: " + lastWindow)
        );
    }

    @Override
    public void saveData() {
        if (lastWindow != null && secondsInCurrentWindow > 0) {
            saveWindow(lastWindow, secondsInCurrentWindow);
        }
        secondsInCurrentWindow = 0;
    }

    private void saveWindow(String windowName, int seconds) {
        try {
            String today = LocalDate.now().toString();
            int dateId = daysRepo.getOrAddDateId(today);

            String timeStr = toHHMMSS(seconds);

            int windowId = repo.getOrAddWindowId(windowName);
            repo.saveWindowUsage(dateId, windowId, timeStr);

        } catch (Exception e) {
            System.err.println("Failed to save window usage: " + e.getMessage());
        }
    }

    @Override
    public boolean getActivityFlag() {
        return true;
    }

    @Override
    public void checkActivity(boolean active) {
        secondsInCurrentWindow++;
    }

    // ---------- HELPERS ----------

    private String toHHMMSS(int sec) {
        int h = sec / 3600;
        int m = (sec % 3600) / 60;
        int s = sec % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private String getActiveAppName() {
        try {
            Process proc = Runtime.getRuntime().exec(
                    new String[]{
                            "osascript",
                            "-e",
                            "tell application \"System Events\"",
                            "-e",
                            "set frontApp to name of (first application process whose frontmost is true)",
                            "-e",
                            "return frontApp",
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
            return null;
        }
    }
    
}
