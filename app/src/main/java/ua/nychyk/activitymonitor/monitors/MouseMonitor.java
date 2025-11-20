package ua.nychyk.activitymonitor.monitors;

import javafx.application.Platform;
import javafx.scene.control.Label;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

public class MouseMonitor implements ActivityFlagMonitor, NativeMouseInputListener {

    private final Label guiLabel;
    private volatile boolean active = false;

    public MouseMonitor(Label guiLabel) {
        this.guiLabel = guiLabel;

        try {
            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeMouseListener(this);
            GlobalScreen.addNativeMouseMotionListener(this);
        } catch (NativeHookException e) {
            System.err.println("JNativeHook failed: " + e.getMessage());
        }
    }

    @Override
    public boolean getActivityFlag() {
        return active;
    }

    @Override
    public void updateWidget() {
        Platform.runLater(() ->
                guiLabel.setText("Mouse Activity: " + (active ? "Active" : "Idle"))
        );
    }

    @Override
    public void saveData() {
        // миша не записує дані в БД
    }

    // --------------------- LISTENER METHODS ---------------------

    @Override
    public void nativeMouseMoved(NativeMouseEvent nativeMouseEvent) {
        active = true;
    }

    @Override
    public void nativeMouseDragged(NativeMouseEvent nativeMouseEvent) {
        active = true;
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nativeMouseEvent) {
        active = true;
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nativeMouseEvent) {
        active = true;
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nativeMouseEvent) {
        active = true;
    }
}
