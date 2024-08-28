package fr.vengelis.afterburner.interconnection.instructions.impl;

import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.interconnection.instructions.BaseCommunicationInstruction;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public class CleanLogHistoryInstruction extends BaseCommunicationInstruction<Boolean> {

    @Override
    public Boolean execute() {
        AfterburnerSlaveApp.get().getLogHistory().clear();
        ConsoleLogger.printLine(Level.INFO, "Log history has been cleaned.");
        return true;
    }

}
