package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.repositories.WindowRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class WindowMonitor implements ActivityAwareMonitor {

    private final Label guiLabel;
    private final WindowRepository repo;

    private String currentWindow = "unknown";
    private int activeSeconds = 0;

    public WindowMonitor(Label guiLabel, WindowRepository repo) {
        this.guiLabel = guiLabel;
        this.repo = repo;
    }

    @Override
    public void updateWidget() {
        // Отримуємо активне вікно через AppleScript
        String title = getActiveWindowTitle();
        if (title != null && !title.isBlank()) {
            currentWindow = title;
        } else {
            currentWindow = "unknown";
        }

        Platform.runLater(() ->
                guiLabel.setText("Active Window: " + currentWindow)
        );
    }

    @Override
    public void saveData() {
        repo.saveWindowUsage(currentWindow, activeSeconds);
        activeSeconds = 0;
    }

    @Override
    public boolean getActivityFlag() {
        // Вікно само по собі не визначає активність
        return false;
    }

    @Override
    public void checkActivity(boolean isActiveNow) {
        if (isActiveNow) {
            activeSeconds++;
        }
    }

    // ---------------------------------------------------------
    //   Реальна реалізація через AppleScript (macOS)
    // ---------------------------------------------------------

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
            return "unknown";
        }
    }
}
