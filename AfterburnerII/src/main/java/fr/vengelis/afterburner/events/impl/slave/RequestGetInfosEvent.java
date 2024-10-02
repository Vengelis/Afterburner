package fr.vengelis.afterburner.events.impl.slave;

import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.events.CancellableEvent;

/**
 * This class represents the RequestGetInfosEvent event in the application.
 * It extends the AbstractCancelableEvent class, which means it inherits all of its methods and properties.
 * <p>
 * The RequestGetInfosEvent class is used to represent the request get infos event in the application.
 */
public class RequestGetInfosEvent extends AbstractEvent implements CancellableEvent {

    private boolean cancelled = false;

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
