package fr.vengelis.afterburner.events;

/**
 * This is the AbstractCancelableEvent class.
 * It is an abstract class that extends the AbstractEvent class.
 * This class introduces a cancellation feature to the events in the application.
 * <p>
 * It has a private boolean property 'cancelled' which is initially set to false.
 * <p>
 * It provides two public methods:
 * <ul>
 *     <li>isCancelled(): This method is used to check if the event is cancelled.</li>
 *     <li>setCancelled(boolean cancelled): This method is used to set the 'cancelled' state of the event.</li>
 * </ul>
 */
public interface CancellableEvent {

    /**
     * This method is used to check if the event is cancelled.
     * @return boolean
     */
    boolean isCancelled();

    /**
     * This method is used to set the 'cancelled' state of the event.
     * @param cancelled boolean
     */
    void setCancelled(boolean cancelled);
}
