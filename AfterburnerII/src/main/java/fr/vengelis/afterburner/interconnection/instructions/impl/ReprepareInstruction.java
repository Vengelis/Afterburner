package fr.vengelis.afterburner.interconnection.instructions.impl;

import com.google.gson.JsonObject;
import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.events.impl.ReprepareRequestEvent;
import fr.vengelis.afterburner.interconnection.instructions.BaseCommunicationInstruction;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public class ReprepareInstruction extends BaseCommunicationInstruction<Boolean> {

    private final String message;

    public ReprepareInstruction(String message) {
        this.message = message;
    }

    @Override
    public Boolean execute() {
        JsonObject msg = new JsonObject();
        msg.addProperty("message", message);
        ReprepareRequestEvent event1 = new ReprepareRequestEvent(msg);
        AfterburnerApp.get().getEventManager().call(event1);
        if(!event1.isCancelled()) {
            AfterburnerApp.get().setReprepareEnabled(true);
            ConsoleLogger.printLine(Level.CONFIG, "Afterburner will run the process again at the end of this process.");
            return true;
        } else {
            return false;
        }
    }

}
