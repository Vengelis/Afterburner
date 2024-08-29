package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.events.AbstractCancelableEvent;
import fr.vengelis.afterburner.logs.PrintedLog;

/**
 * This class represents the PrintedLogEvent in the application.
 * It extends the AbstractCancelableEvent class, which means it inherits all of its methods and properties.
 * <p>
 * This event is triggered after a log has been printed into console by managed application or cli manager.
 * <p>
 * It has one property:
 * <ul>
 *     <li>log: a PrintedLog that represents the log that has been printed.</li>
 * </ul>
 * It provides one constructor:
 * <ul>
 *     <li>PrintedLogEvent(PrintedLog log): This constructor initializes the 'log' property.</li>
 * </ul>
 * It also provides one public method:
 * <ul>
 *     <li>getLog(): This method returns the current state of the 'log' property.</li>
 * </ul>
 */
public class PrintedLogEvent extends AbstractCancelableEvent {

    private final PrintedLog log;
    private final Handler handler;


    public enum Handler {
        PROCESS,
        CLI,
        ;
    }

    /**
     * This constructor initializes the 'log' property.
     * @param log PrintedLog
     */
    public PrintedLogEvent(PrintedLog log, Handler handler) {
        this.log = log;
        this.handler = handler;
    }

    /**
     * This method returns the current state of the 'log' property.
     * @return PrintedLog
     */
    public PrintedLog getLog() {
        return log;
    }

    public Handler getHandler() {
        return handler;
    }
}
