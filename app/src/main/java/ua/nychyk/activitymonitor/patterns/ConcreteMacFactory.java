package ua.nychyk.activitymonitor.patterns;

import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.monitors.ComputerUsageMonitor;
import ua.nychyk.activitymonitor.monitors.CpuMonitor;
import ua.nychyk.activitymonitor.monitors.KeyboardMonitor;
import ua.nychyk.activitymonitor.monitors.MemoryMonitor;
import ua.nychyk.activitymonitor.monitors.MouseMonitor;
import ua.nychyk.activitymonitor.monitors.WindowMonitor;
import ua.nychyk.activitymonitor.repositories.*;

public class ConcreteMacFactory implements MonitorFactory {

    private final MonitorRepositoryFactory repoFactory;

    public ConcreteMacFactory(String dbFile) {
        this.repoFactory = new MonitorRepositoryFactory(dbFile);
    }

    @Override
    public CpuMonitor createProcessorMonitor(String dbFile, Label guiLabel) {
        return new CpuMonitor(guiLabel, repoFactory.getProcessorRepository());
    }

    @Override
    public MemoryMonitor createMemoryMonitor(String dbFile, Label guiLabel) {
        return new MemoryMonitor(guiLabel, repoFactory.getMemoryRepository());
    }

    @Override
    public MouseMonitor createMouseMonitor(Label guiLabel) {
        return new MouseMonitor(guiLabel);
    }

    @Override
    public KeyboardMonitor createKeyboardMonitor(Label guiLabel) {
        return new KeyboardMonitor(guiLabel);
    }

    @Override
    public ComputerUsageMonitor createComputerUsageMonitor(String dbFile, Label guiLabel) {
        return new ComputerUsageMonitor(guiLabel, repoFactory.getComputerUsageRepository());
    }

    @Override
    public WindowMonitor createWindowMonitor(String dbFile, Label guiLabel) {
        return new WindowMonitor(guiLabel, repoFactory.getWindowRepository());
    }
}
