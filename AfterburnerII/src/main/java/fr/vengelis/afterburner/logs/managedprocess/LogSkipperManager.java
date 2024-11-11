package fr.vengelis.afterburner.logs.managedprocess;

import java.util.ArrayList;
import java.util.List;

/**
 * The LogSkipperManager class is used to manage a list of Skipper objects in the application.
 * <p>
 * The Skipper system is used to skip certain logs from being displayed in the console.
 * By default, skippers are available in Afterburner such as the 'Skipper' class.
 * It is possible to integrate your own skippers to complete the automatic skipping of logs in the template system.
 * <p>
 * It provides two public methods:
 * <ul>
 *     <li>getSkipperList(): This method returns the current state of the 'skipperList' property.</li>
 *     <li>register(Skipper skipper): This method adds a new Skipper object to the 'skipperList' list.</li>
 * </ul>
 */
public class LogSkipperManager {

    private final List<Skipper> skipperList = new ArrayList<>();

    /**
     * This method returns the current state of the 'skipperList' property.
     * @return List<Skipper>
     */
    public List<Skipper> getSkipperList() {
        return skipperList;
    }

    /**
     * This method adds a new Skipper object to the 'skipperList' list.
     * @param skipper Skipper
     */
    public void register(Skipper skipper) {
        skipperList.add(skipper);
    }
}
