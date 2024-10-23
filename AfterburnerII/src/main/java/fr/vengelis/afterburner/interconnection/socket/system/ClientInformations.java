package fr.vengelis.afterburner.interconnection.socket.system;

import java.net.InetAddress;
import java.util.UUID;

public class ClientInformations {

    private final UUID uuid;
    private final InetAddress address;

    public ClientInformations(UUID uuid, InetAddress address) {
        this.uuid = uuid;
        this.address = address;
    }

    public UUID getUuid() {
        return uuid;
    }

    public InetAddress getAddress() {
        return address;
    }
}
