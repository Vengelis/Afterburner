package fr.vengelis.afterburner.events.impl.slave;

import com.google.gson.JsonObject;
import fr.vengelis.afterburner.events.AbstractCancelableEvent;

/**
 * This class represents the ReturnGetInfosEvent event in the application.
 * It extends the AbstractCancelableEvent class, which means it inherits all of its methods and properties.
 * <p>
 * The ReturnGetInfosEvent class is used to represent the return get infos event in the application.
 * <p>
 * It has one property:
 * <ul>
 *     <li>datas: a JsonObject that represents the data returned from the get infos request.</li>
 * </ul>
 * It provides one constructor:
 * <ul>
 *     <li>ReturnGetInfosEvent(JsonObject datas): This constructor initializes the 'datas' property.</li>
 * </ul>
 * It also provides one public method:
 * <ul>
 *     <li>getDatas(): This method returns the current state of the 'datas' property.</li>
 * </ul>
 */
public class ReturnGetInfosEvent extends AbstractCancelableEvent {
    private final JsonObject datas;

    /**
     * This constructor initializes the 'datas' property.
     * @param datas JsonObject
     */
    public ReturnGetInfosEvent(JsonObject datas) {
        this.datas = datas;
    }

    /**
     * This method returns the current state of the 'datas' property.
     * @return JsonObject
     */
    public JsonObject getDatas() {
        return datas;
    }
}
