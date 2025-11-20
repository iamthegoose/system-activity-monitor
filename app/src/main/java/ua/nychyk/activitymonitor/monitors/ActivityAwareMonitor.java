package ua.nychyk.activitymonitor.monitors;

public interface ActivityAwareMonitor extends Monitor {
    boolean getActivityFlag();
    void checkActivity(boolean active);
}

