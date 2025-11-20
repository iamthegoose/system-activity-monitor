package ua.nychyk.activitymonitor.patterns;

import ua.nychyk.activitymonitor.monitors.Monitor;

import java.util.Iterator;
import java.util.List;

public class MonitorIterator implements Iterator<Monitor> {

    private final List<Monitor> monitors;
    private int index = 0;

    public MonitorIterator(List<Monitor> monitors) {
        this.monitors = monitors;
    }

    @Override
    public boolean hasNext() {
        return index < monitors.size();
    }

    @Override
    public Monitor next() {
        return monitors.get(index++);
    }
}
