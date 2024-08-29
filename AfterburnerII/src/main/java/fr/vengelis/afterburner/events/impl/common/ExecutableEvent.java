package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.events.AbstractEvent;

public class ExecutableEvent extends AbstractEvent {
    private StringBuilder cmdline;
    private final Stage stage;

    public ExecutableEvent(StringBuilder cmdline) {
        this.cmdline = cmdline;
        this.stage = Stage.PRE;
    }

    public ExecutableEvent() {
        this.cmdline = new StringBuilder();
        this.stage = Stage.POST;
    }

    public StringBuilder getCmdline() {
        return cmdline;
    }

    public void setCmdline(StringBuilder cmdline) {
        this.cmdline = cmdline;
    }

    public enum Stage {
        PRE,
        POST,
        ;
    }
}
