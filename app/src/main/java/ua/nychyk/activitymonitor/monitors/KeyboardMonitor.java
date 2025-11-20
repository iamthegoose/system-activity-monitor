package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;

public class KeyboardMonitor implements ActivityAwareMonitor, NativeKeyListener {

    private final Label guiLabel;
    private volatile boolean active = false;

    public KeyboardMonitor(Label guiLabel) {
        this.guiLabel = guiLabel;

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(this);
        } catch (NativeHookException e) {
            System.err.println("JNativeHook failed: " + e.getMessage());
        }
    }

    @Override
    public boolean getActivityFlag() {
        return active;
    }

    @Override
    public void checkActivity(boolean isActive) {
        this.active = isActive;
    }

    @Override
    public void updateWidget() {
        Platform.runLater(() ->
                guiLabel.setText("Keyboard Activity: " + (active ? "Active" : "Idle"))
        );
        active = false; // RESET
    }


    @Override
    public void saveData() { }

    // ---------------- KEYBOARD LISTENER ----------------

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeKeyEvent) {
        active = true;
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nativeKeyEvent) { }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nativeKeyEvent) { }
}
