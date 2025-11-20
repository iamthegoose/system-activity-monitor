package ua.nychyk.activitymonitor.report;

import ua.nychyk.activitymonitor.patterns.commands.Command;
import ua.nychyk.activitymonitor.patterns.visitors.ReportVisitor;

public class ReportInvoker {

    private Command command;

    public void setCommand(Command command) {
        this.command = command;
    }

    public Object executeCommand(ReportVisitor visitor) {
        if (command == null) {
            System.out.println("No command set.");
            return null;
        }
        return command.execute(visitor);
    }
}
