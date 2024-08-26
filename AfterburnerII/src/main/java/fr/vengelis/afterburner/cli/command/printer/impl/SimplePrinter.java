package fr.vengelis.afterburner.cli.command.printer.impl;

import fr.vengelis.afterburner.cli.command.printer.IConsolePrinter;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public class SimplePrinter implements IConsolePrinter<String> {

    @Override
    public void print(String data) {
        ConsoleLogger.printLine(Level.INFO, data);
    }

}
