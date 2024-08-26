package fr.vengelis.afterburner.cli.command.printer.impl;

import fr.vengelis.afterburner.cli.command.printer.IConsolePrinter;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.Collection;
import java.util.logging.Level;

public class CollectionPrinter implements IConsolePrinter<Object> {

    @Override
    public void print(Object data) {
        if (data instanceof Collection) {
            ((Collection<?>) data).forEach(d -> ConsoleLogger.printLine(Level.INFO, d.toString()));
        } else if (data.getClass().isArray()) {
            int length = java.lang.reflect.Array.getLength(data);
            for (int i = 0; i < length; i++) {
                Object element = java.lang.reflect.Array.get(data, i);
                ConsoleLogger.printLine(Level.INFO, element.toString());
            }
        } else {
            throw new IllegalArgumentException("Unsupported data type: " + data.getClass().getName());
        }
    }

}
