package fr.vengelis.afterburner.events;

/**
 * The EventPriority enum represents the priority levels for event handling in the system.
 * These priority levels determine the order in which event handlers are executed when an event occurs.
 * <br>
 * The priority levels are as follows:
 * MONITOR: This is the highest priority level. Event handlers with this priority level are executed first.
 * HIGHEST: This is the second highest priority level.
 * HIGH: This is the third highest priority level.
 * NORMAL: This is the default priority level. This is the middle priority level. Event handlers with this priority level are executed after HIGH and before LOW.
 * LOW: This is the third lowest priority level.
 * LOWEST: This is the second lowest priority level.
 * LAST: This is the lowest priority level. Event handlers with this priority level are executed last.
 */
public enum EventPriority {
    MONITOR,
    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST,
    LAST
}
