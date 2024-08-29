package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.interconnection.socket.system.ClientInformations;

public class ClientConnectEvent extends AbstractEvent {

    private final ClientInformations uuid;

    public ClientConnectEvent(ClientInformations uuid) {
        this.uuid = uuid;
    }

    public ClientInformations getUuid() {
        return uuid;
    }
}
