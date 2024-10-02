package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.mprocess.argwrapper.IArgWrapper;

public class ExecutableEvent extends AbstractEvent {

    private final IArgWrapper wrapper;
    private StringBuilder cmdline;
    private final Stage stage;

    public ExecutableEvent(IArgWrapper wrapper, StringBuilder line, Stage stage) {
        this.wrapper = wrapper;
        this.cmdline = line;
        this.stage = stage;
    }

    public ExecutableEvent(IArgWrapper wrapper, StringBuilder line) {
        this.wrapper = wrapper;
        this.cmdline = line;
        this.stage = Stage.POST;
    }

    public ExecutableEvent() {
        this.wrapper = null;
        this.cmdline = new StringBuilder();
        this.stage = Stage.POST;
    }

    public StringBuilder getCmdline() {
        return cmdline;
    }

    public void setCmdline(StringBuilder cmdline) {
        this.cmdline = cmdline;
    }

    public IArgWrapper getWrapper() {
        return wrapper;
    }

    public Stage getStage() {
        return stage;
    }

    public enum Stage {
        PRE,
        POST,
        ;
    }
}
