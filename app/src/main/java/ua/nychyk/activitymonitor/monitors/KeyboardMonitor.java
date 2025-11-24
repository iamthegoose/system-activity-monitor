package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class KeyboardMonitor implements ActivityAwareMonitor, NativeKeyListener {

    private final Label guiLabel;

    private final Set<String> pressedKeys = ConcurrentHashMap.newKeySet();

    public KeyboardMonitor(Label guiLabel) {
        this.guiLabel = guiLabel;
    }

    @Override
    public boolean getActivityFlag() {
        return !pressedKeys.isEmpty();
    }

    @Override
    public void checkActivity(boolean ignored) {}

    @Override
    public void updateWidget() {
        String keysText = pressedKeys.isEmpty()
                ? "None"
                : String.join(", ", pressedKeys);

        String text = String.format(
                "Keyboard pressed: %s   |   %s",
                keysText,
                pressedKeys.isEmpty() ? "Idle" : "Active"
        );

        Platform.runLater(() -> guiLabel.setText(text));
    }

    @Override
    public void saveData() {}

    // -------------------- KEYBOARD EVENTS --------------------

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        pressedKeys.add(NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        pressedKeys.remove(NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {}
}
