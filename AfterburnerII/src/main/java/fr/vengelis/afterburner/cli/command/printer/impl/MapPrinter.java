package fr.vengelis.afterburner.cli.command.printer.impl;

import fr.vengelis.afterburner.cli.command.printer.IConsolePrinter;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.Map;
import java.util.logging.Level;

public class MapPrinter implements IConsolePrinter<Map<?, ?>> {

    @Override
    public void print(Map<?, ?> data) {
        data.forEach((k,v) -> ConsoleLogger.printLine(Level.INFO, k.toString() + " -> " + v.toString()));
    }

}
