package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.repositories.MemoryRepository;

import com.sun.management.OperatingSystemMXBean;
import java.lang.management.ManagementFactory;

public class MemoryMonitor implements Monitor {

    private final Label guiLabel;
    private final MemoryRepository repo;

    private final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public MemoryMonitor(Label guiLabel, MemoryRepository repo) {
        this.guiLabel = guiLabel;
        this.repo = repo;
    }

    @Override
    public void updateWidget() {
        long free = osBean.getFreePhysicalMemorySize();
        long total = osBean.getTotalPhysicalMemorySize();
        double percent = (1 - (double) free / total) * 100.0;

        Platform.runLater(() ->
                guiLabel.setText(String.format("Memory Usage: %.2f%%", percent))
        );
    }

    @Override
    public void saveData() {
        long free = osBean.getFreePhysicalMemorySize();
        long total = osBean.getTotalPhysicalMemorySize();
        double percent = (1 - (double) free / total) * 100.0;

        repo.saveMemoryUsage(percent);
    }

    @Override
    public boolean getActivityFlag() {
        return false; // не визначає активність
    }
}
