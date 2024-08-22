package fr.vengelis.afterburner.events.impl;

import fr.vengelis.afterburner.events.AbstractEvent;

/**
 * This class represents the PreparedExecutableEvent in the application.
 * It extends the AbstractEvent class, which means it inherits all of its methods and properties.
 * <p>
 * This event is triggered after the preparation of the command line to be executed.
 * <p>
 * It has one property:
 * <ul>
 *     <li>cmdline: a StringBuilder that represents the command line to be executed.</li>
 * </ul>
 * It provides one constructor:
 * <ul>
 *     <li>PreparedExecutableEvent(StringBuilder cmdline): This constructor initializes the 'cmdline' property.</li>
 * </ul>
 * It also provides two public methods:
 * <ul>
 *     <li>getCmdline(): This method returns the current state of the 'cmdline' property.</li>
 *     <li>setCmdline(StringBuilder cmdline): This method sets a new state for the 'cmdline' property.</li>
 * </ul>
 * This event is called as many times as the server will be re-prepared.
 */
public class PreparedExecutableEvent extends AbstractEvent {
    private StringBuilder cmdline;

    /**
     * This constructor initializes the 'cmdline' property.
     * @param cmdline StringBuilder
     */
    public PreparedExecutableEvent(StringBuilder cmdline) {
        this.cmdline = cmdline;
    }

    /**
     * This method returns the current state of the 'cmdline' property.
     * @return StringBuilder
     */
    public StringBuilder getCmdline() {
        return cmdline;
    }

    /**
     * This method sets a new state for the 'cmdline' property.
     * @param cmdline StringBuilder
     */
    public void setCmdline(StringBuilder cmdline) {
        this.cmdline = cmdline;
    }
}
