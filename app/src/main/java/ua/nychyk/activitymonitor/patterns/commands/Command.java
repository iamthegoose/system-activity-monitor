package ua.nychyk.activitymonitor.patterns.commands;

import ua.nychyk.activitymonitor.patterns.visitors.ReportVisitor;

public interface Command {
    Object execute(ReportVisitor visitor);
}
