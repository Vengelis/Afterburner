package fr.vengelis.afterburner.interconnection.socket.broadcaster;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import fr.vengelis.afterburner.AfterburnerBroadcasterApp;
import fr.vengelis.afterburner.AfterburnerState;
import fr.vengelis.afterburner.language.LanguageManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;

public class SlaveBroadcast {

    private final UUID uuid;
    private final String name;
    private final String address;
    private final Integer port;

    private Long lastContact = 0L;
    private boolean available = false;
    private AfterburnerState state = AfterburnerState.NOT_STARTED;

    private final int BASE_REMOVE = 5;

    public SlaveBroadcast(UUID uuid, String name, String address, Integer port) {
        this.uuid = uuid;
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public Integer getPort() {
        return port;
    }

    public Long getLastContact() {
        return lastContact;
    }

    public void setLastContact(Long lastContact) {
        this.lastContact = lastContact;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public AfterburnerState getState() {
        return state;
    }

    public void setState(AfterburnerState state) {
        this.state = state;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static SlaveBroadcast fromJson(String json) throws JsonSyntaxException {
        Gson gson = new Gson();
        return gson.fromJson(json, SlaveBroadcast.class);
    }

    public void actualise() {
        if(BASE_REMOVE == Instant.now().getEpochSecond() - lastContact) {
            available = false;
            ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("broadcaster-slave-not-available"),this.getUuid().toString(), this.getName()));
        }
        if(!available && ((BASE_REMOVE + 5) < Instant.now().getEpochSecond() - lastContact)) {
            ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("broadcaster-slave-expired-removed"),this.getUuid().toString(), this.getName()));
            AfterburnerBroadcasterApp.get().getSlaves().remove(this);
        }
    }
}
