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
public abstract class AbstractCancelableEvent extends AbstractEvent{

    private boolean cancelled = false;

    /**
     * This method is used to check if the event is cancelled.
     * @return boolean
     */
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * This method is used to set the 'cancelled' state of the event.
     * @param cancelled boolean
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
