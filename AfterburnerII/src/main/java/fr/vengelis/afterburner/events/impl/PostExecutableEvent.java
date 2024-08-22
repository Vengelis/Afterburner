package fr.vengelis.afterburner.events.impl;

import fr.vengelis.afterburner.events.AbstractEvent;

/**
 * This class represents the PostExecutableEvent in the application.
 * It extends the AbstractEvent class, which means it inherits all of its methods and properties.
 * <p>
 * This class is used to represent the event that occurs after running the minecraft server.
 * This event is called as many times as the server will be shut down if it was reprepared before
 */
public class PostExecutableEvent extends AbstractEvent {
}
