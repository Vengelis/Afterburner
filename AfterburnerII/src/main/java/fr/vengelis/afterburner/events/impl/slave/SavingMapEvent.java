package fr.vengelis.afterburner.events.impl.slave;

import fr.vengelis.afterburner.events.AbstractCancelableEvent;

/**
 * This class represents the SavingMapEvent in the application.
 * It extends the AbstractCancelableEvent class, which means it inherits all of its methods and properties.
 * <p>
 * The SavingMapEvent class is used to handle the saving of a map.
 * This event is called as many times as there are maps to save
 * <p>
 * It has three properties:
 * <ul>
 *     <li>initialName: a String that represents the initial name of the map.</li>
 *     <li>destination: a String that represents the destination of the map.</li>
 *     <li>modifiedName: a String that represents the modified name of the map.</li>
 * </ul>
 * It provides one constructor:
 * <ul>
 *     <li>SavingMapEvent(String initialName, String destination, String modifiedName): This constructor initializes the 'initialName', 'destination', and 'modifiedName' properties.</li>
 * </ul>
 * It also provides six public methods:
 * <ul>
 *     <li>getInitialName(): This method returns the current state of the 'initialName' property.</li>
 *     <li>setInitialName(String initialName): This method updates the state of the 'initialName' property.</li>
 *     <li>getDestination(): This method returns the current state of the 'destination' property.</li>
 *     <li>setDestination(String destination): This method updates the state of the 'destination' property.</li>
 *     <li>getModifiedName(): This method returns the current state of the 'modifiedName' property.</li>
 *     <li>setModifiedName(String modifiedName): This method updates the state of the 'modifiedName' property.</li>
 * </ul>
 */
public class SavingMapEvent extends AbstractCancelableEvent {

    private String initialName;
    private String destination;
    private String modifiedName;

    /**
     * This constructor initializes the 'initialName', 'destination', and 'modifiedName' properties.
     * @param initialName String
     * @param destination String
     * @param modifiedName String
     */
    public SavingMapEvent(String initialName, String destination, String modifiedName) {
        this.initialName = initialName;
        this.destination = destination;
        this.modifiedName = modifiedName;
    }

    /**
     * This method returns the current state of the 'initialName' property.
     * @return String
     */
    public String getInitialName() {
        return initialName;
    }

    /**
     * This method updates the state of the 'initialName' property.
     * @param initialName String
     */
    public void setInitialName(String initialName) {
        this.initialName = initialName;
    }

    /**
     * This method returns the current state of the 'destination' property.
     * @return String
     */
    public String getDestination() {
        return destination;
    }

    /**
     * This method updates the state of the 'destination' property.
     * @param destination String
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * This method returns the current state of the 'modifiedName' property.
     * @return String
     */
    public String getModifiedName() {
        return modifiedName;
    }

    /**
     * This method updates the state of the 'modifiedName' property.
     * @param modifiedName String
     */
    public void setModifiedName(String modifiedName) {
        this.modifiedName = modifiedName;
    }
}
