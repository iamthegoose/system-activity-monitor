package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.repositories.ProcessorRepository;

import java.lang.management.ManagementFactory;
import com.sun.management.OperatingSystemMXBean;

public class CpuMonitor implements Monitor {

    private final Label guiLabel;
    private final ProcessorRepository repo;

    private final OperatingSystemMXBean osBean =
            (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    public CpuMonitor(Label guiLabel, ProcessorRepository repo) {
        this.guiLabel = guiLabel;
        this.repo = repo;
    }

    @Override
    public void updateWidget() {
        double cpu = osBean.getSystemCpuLoad() * 100.0;

        Platform.runLater(() ->
                guiLabel.setText(String.format("CPU Usage: %.2f%%", cpu))
        );
    }

    @Override
    public void saveData() {
        double cpu = osBean.getSystemCpuLoad() * 100.0;
        repo.saveCpuUsage(cpu);
    }

    @Override
    public boolean getActivityFlag() {
        return false; // CPU не фіксує активність
    }
}
