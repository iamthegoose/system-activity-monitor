package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MouseMonitor implements ActivityFlagMonitor, NativeMouseInputListener {

    private final Label guiLabel;

    private volatile int mouseX = 0;
    private volatile int mouseY = 0;

    private final Set<String> pressedButtons = ConcurrentHashMap.newKeySet();

    public MouseMonitor(Label guiLabel) {
        this.guiLabel = guiLabel;
    }

    @Override
    public boolean getActivityFlag() {
        return !pressedButtons.isEmpty();
    }

    @Override
    public void updateWidget() {
        String buttons = pressedButtons.isEmpty()
                ? "None"
                : String.join(", ", pressedButtons);

        String text = String.format(
                "Mouse pos: (%d, %d)   |   Buttons: %s   |   %s",
                mouseX, mouseY,
                buttons,
                pressedButtons.isEmpty() ? "Idle" : "Active"
        );

        Platform.runLater(() -> guiLabel.setText(text));
    }

    @Override
    public void saveData() {}

    // -------------------- EVENTS --------------------

    @Override
    public void nativeMouseMoved(NativeMouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent e) {
        pressedButtons.add(convertButton(e.getButton()));
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent e) {
        pressedButtons.remove(convertButton(e.getButton()));
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent e) {}

    private String convertButton(int b) {
        return switch (b) {
            case NativeMouseEvent.BUTTON1 -> "Left";
            case NativeMouseEvent.BUTTON2 -> "Middle";
            case NativeMouseEvent.BUTTON3 -> "Right";
            default -> "Unknown";
        };
    }
}
