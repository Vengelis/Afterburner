package fr.vengelis.afterburner.cli.command;

import fr.vengelis.afterburner.cli.command.printer.IConsolePrinter;
import fr.vengelis.afterburner.cli.command.printer.ConsolePrinterFactory;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public class CommandResultReader {

    public static void read(CommandResult<?> result) {
        Object data = result.getExecutionResult().getResponseData();
        if (data != null) {
            IConsolePrinter<Object> printer = (IConsolePrinter<Object>) ConsolePrinterFactory.getPrinter(data.getClass());
            if (printer != null) {
                printer.print(data);
            } else {
                ConsoleLogger.printVerbose(Level.WARNING, "No printer found for class: " + data.getClass().getName());
            }
        } else {
            ConsoleLogger.printVerbose(Level.WARNING, "No response data to print.");
        }
    }

}
