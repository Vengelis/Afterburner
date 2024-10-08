package fr.vengelis.afterburner.events.impl.slave;

import com.google.gson.JsonObject;
import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.events.CancellableEvent;

/**
 * This class represents the KillTaskEvent in the application.
 * It extends the AbstractCancelableEvent class, which means it inherits all of its methods and properties.
 * <p>
 * It has two properties:
 * <ul>
 *     <li>shutdownAfterburner: a boolean that indicates whether the Afterburner should be shut down when this event is triggered.</li>
 *     <li>message: a JsonObject that contains the message associated with this event.</li>
 * </ul>
 * <p>
 * It provides two constructors:
 * <ul>
 *     <li>KillTaskEvent(boolean shutdownAfterburner, JsonObject message): This constructor initializes both the 'shutdownAfterburner' and 'message' properties.</li>
 *     <li>KillTaskEvent(JsonObject message): This constructor only initializes the 'message' property. The 'shutdownAfterburner' property is set to false by default.</li>
 * </ul>
 * <p>
 * It also provides two public methods:
 * <ul>
 *     <li>isShutdownAfterburner(): This method returns the current state of the 'shutdownAfterburner' property.</li>
 *     <li>getMessage(): This method returns the current state of the 'message' property.</li>
 * </ul>
 */
public class KillTaskEvent extends AbstractEvent implements CancellableEvent {
    private boolean shutdownAfterburner = false;
    private final JsonObject message;
    private boolean cancelled = false;

    /**
     * This constructor initializes both the 'shutdownAfterburner' and 'message' properties.
     * @param shutdownAfterburner Boolean
     * @param message JsonObject
     */
    public KillTaskEvent(boolean shutdownAfterburner, JsonObject message) {
        this.shutdownAfterburner = shutdownAfterburner;
        this.message = message;
    }

    /**
     * This constructor only initializes the 'message' property. The 'shutdownAfterburner' property is set to false by default.
     * @param message JsonObject
     */
    public KillTaskEvent(JsonObject message) {
        this.message = message;
    }

    /**
     * This method returns the current state of the 'shutdownAfterburner' property.
     * @return boolean
     */
    public boolean isShutdownAfterburner() {
        return shutdownAfterburner;
    }

    /**
     * This method returns the current state of the 'message' property.
     * @return JsonObject
     */
    public JsonObject getMessage() {
        return message;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
