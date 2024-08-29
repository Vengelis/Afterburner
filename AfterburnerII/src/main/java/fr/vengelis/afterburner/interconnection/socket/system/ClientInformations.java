package fr.vengelis.afterburner.interconnection.socket.system;

import java.util.UUID;

public class ClientInformations {

    private final UUID uuid;

    public ClientInformations(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
