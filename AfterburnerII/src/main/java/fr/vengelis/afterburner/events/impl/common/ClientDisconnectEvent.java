package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.interconnection.socket.system.ClientInformations;

public class ClientDisconnectEvent extends AbstractEvent {

    private final ClientInformations clientInformations;

    public ClientDisconnectEvent(ClientInformations clientInformations) {
        this.clientInformations = clientInformations;
    }

    public ClientInformations getClientInformations() {
        return clientInformations;
    }

}
