package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.repositories.ComputerUsageRepository;

public class ComputerUsageMonitor implements ActivityAwareMonitor {

    private final Label guiLabel;
    private final ComputerUsageRepository repo;

    private int activeSeconds = 0;

    public ComputerUsageMonitor(Label guiLabel, ComputerUsageRepository repo) {
        this.guiLabel = guiLabel;
        this.repo = repo;
    }

    @Override
    public void updateWidget() {
        Platform.runLater(() ->
                guiLabel.setText("Computer Active: " + activeSeconds + " sec")
        );
    }

    @Override
    public void saveData() {
        repo.saveComputerUsage(activeSeconds);
        activeSeconds = 0;
    }

    @Override
    public boolean getActivityFlag() {
        return activeSeconds > 0;
    }

    public void checkActivity(boolean isActiveNow) {
        if (isActiveNow) {
            activeSeconds++;
        }
    }
}
