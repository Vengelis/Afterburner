package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.events.AbstractEvent;

/**
 * This class represents the LoadEvent in the application.
 * This event is called just after the configuration loading process has finished.
 * <p>
 * He depends on the "ready" status in the general configuration of Afterburner. It will not be called if the latter is set to false or if a problem loading configurations is issued
 */
public class LoadEvent extends AbstractEvent {
}
