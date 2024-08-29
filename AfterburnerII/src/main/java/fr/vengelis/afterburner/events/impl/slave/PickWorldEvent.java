package fr.vengelis.afterburner.events.impl.slave;

import fr.vengelis.afterburner.events.AbstractCancelableEvent;

import java.io.File;

/**
 * This class represents the PickWorldEvent in the application.
 * It extends the AbstractCancelableEvent class, which means it inherits all of its methods and properties.
 * <p>
 * The PickWorldEvent class is used to represent the event that is fired when a world is picked.
 * <p>
 * It has one property:
 * <ul>
 *     <li>pickedWorld: a File that represents the world that has been picked.</li>
 * </ul>
 * <p>
 * It provides one constructor:
 * <ul>
 *     <li>PickWorldEvent(File pickedWorld): This constructor initializes the 'pickedWorld' property.</li>
 * </ul>
 * <p>
 * It also provides two public methods:
 * <ul>
 *     <li>getPickedWorld(): This method returns the current state of the 'pickedWorld' property.</li>
 *     <li>setPickedWorld(File pickedWorld): This method sets a new state for the 'pickedWorld' property.</li>
 * </ul>
 */
public class PickWorldEvent extends AbstractCancelableEvent {
    private File pickedWorld;

    /**
     * This constructor initializes the 'pickedWorld' property.
     * @param pickedWorld File
     */
    public PickWorldEvent(File pickedWorld) {
        this.pickedWorld = pickedWorld;
    }

    /**
     * This method returns the current state of the 'pickedWorld' property.
     * @return File
     */
    public File getPickedWorld() {
        return pickedWorld;
    }

    /**
     * This method sets a new state for the 'pickedWorld' property.
     * @param pickedWorld File
     */
    public void setPickedWorld(File pickedWorld) {
        this.pickedWorld = pickedWorld;
    }
}
