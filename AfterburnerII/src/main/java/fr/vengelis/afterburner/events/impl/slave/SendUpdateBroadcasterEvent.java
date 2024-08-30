package fr.vengelis.afterburner.events.impl.slave;

import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.interconnection.socket.broadcaster.SlaveBroadcast;

public class SendUpdateBroadcasterEvent extends AbstractEvent {

    private final SlaveBroadcast slaveBroadcast;

    public SendUpdateBroadcasterEvent(SlaveBroadcast slaveBroadcast) {
        this.slaveBroadcast = slaveBroadcast;
    }

    public SlaveBroadcast getSlaveBroadcast() {
        return slaveBroadcast;
    }
}
