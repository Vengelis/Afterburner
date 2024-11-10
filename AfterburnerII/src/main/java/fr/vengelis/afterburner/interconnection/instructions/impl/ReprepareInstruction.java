package fr.vengelis.afterburner.interconnection.instructions.impl;

import com.google.gson.JsonObject;
import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.events.impl.slave.ReprepareRequestEvent;
import fr.vengelis.afterburner.interconnection.instructions.BaseCommunicationInstruction;
import fr.vengelis.afterburner.language.LanguageManager;
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
        AfterburnerSlaveApp.get().getEventManager().call(event1);
        if(!event1.isCancelled()) {
            AfterburnerSlaveApp.get().setReprepareEnabled(true);
            ConsoleLogger.printLine(Level.CONFIG, LanguageManager.translate("atb-reprepare-instruction"));
            return true;
        } else {
            return false;
        }
    }

}
