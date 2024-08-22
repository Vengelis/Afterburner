package fr.vengelis.afterburner.events.impl;

import com.google.gson.JsonObject;
import fr.vengelis.afterburner.events.AbstractCancelableEvent;

/**
 * This class represents the ReprepareRequestEvent in the application.
 * It extends the AbstractCancelableEvent class, which means it inherits all of its methods and properties.
 * <p>
 * The ReprepareRequestEvent class is used to represent the reprepare request event in the application.
 * <p>
 * It has one property:
 * <ul>
 *     <li>message: a JsonObject that represents the message associated with the reprepare request.</li>
 * </ul>
 * It provides one constructor:
 * <ul>
 *     <li>ReprepareRequestEvent(JsonObject message): This constructor initializes the 'message' property.</li>
 * </ul>
 * It also provides one public method:
 * <ul>
 *     <li>getMessage(): This method returns the current state of the 'message' property.</li>
 * </ul>
 */
public class ReprepareRequestEvent extends AbstractCancelableEvent {
    private final JsonObject message;

    /**
     * This constructor initializes the 'message' property.
     * @param message JsonObject
     */
    public ReprepareRequestEvent(JsonObject message) {
        this.message = message;
    }

    /**
     * This method returns the current state of the 'message' property.
     * @return JsonObject
     */
    public JsonObject getMessage() {
        return message;
    }
}
