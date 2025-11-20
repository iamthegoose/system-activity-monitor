package ua.nychyk.activitymonitor.patterns;

import javafx.scene.control.Label;
import ua.nychyk.activitymonitor.monitors.*;

public interface MonitorFactory {

    CpuMonitor createProcessorMonitor(String dbFile, Label guiLabel);

    MemoryMonitor createMemoryMonitor(String dbFile, Label guiLabel);

    MouseMonitor createMouseMonitor(Label guiLabel);

    KeyboardMonitor createKeyboardMonitor(Label guiLabel);

    ComputerUsageMonitor createComputerUsageMonitor(String dbFile, Label guiLabel);

    WindowMonitor createWindowMonitor(String dbFile, Label guiLabel);
}
