package ua.nychyk.activitymonitor.monitors;

public interface Monitor {
    void updateWidget();
    void saveData();
    boolean getActivityFlag();
}
