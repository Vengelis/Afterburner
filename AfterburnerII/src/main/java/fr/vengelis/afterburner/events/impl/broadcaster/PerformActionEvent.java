package fr.vengelis.afterburner.events.impl.broadcaster;

import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.BroadcasterWebApiHandler;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.SlaveBroadcast;

public class PerformActionEvent extends AbstractEvent {

    private final SlaveBroadcast slave;
    private final BroadcasterWebApiHandler.Action action;

    public PerformActionEvent(SlaveBroadcast slave, BroadcasterWebApiHandler.Action action) {
        this.slave = slave;
        this.action = action;
    }

    public SlaveBroadcast getSlave() {
        return slave;
    }

    public BroadcasterWebApiHandler.Action getAction() {
        return action;
    }

}
