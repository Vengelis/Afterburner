package fr.vengelis.afterburner.cli.command.printer.impl;

import fr.vengelis.afterburner.cli.command.printer.IConsolePrinter;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public class BooleanPrinter implements IConsolePrinter<Boolean> {
    @Override
    public void print(Boolean data) {
        ConsoleLogger.printLine(Level.INFO, data.toString());
    }
}
