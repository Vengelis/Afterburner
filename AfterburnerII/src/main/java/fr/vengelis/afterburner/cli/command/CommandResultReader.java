package fr.vengelis.afterburner.cli.command;

import fr.vengelis.afterburner.cli.command.printer.IConsolePrinter;
import fr.vengelis.afterburner.cli.command.printer.ConsolePrinterFactory;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.ArrayDeque;
import java.util.logging.Level;

public class CommandResultReader {

    public static void read(CommandResult<?> result) {
        Object data = result.getExecutionResult().getResponseData();
        if (data != null) {
            IConsolePrinter<Object> printer = (IConsolePrinter<Object>) ConsolePrinterFactory.getPrinter(data.getClass());
            if (printer != null) {
                printer.print(data);
            } else {
                ConsoleLogger.printLine(Level.WARNING, "No printer found for class: " + data.getClass().getName());
            }
        } else {
            ConsoleLogger.printLine(Level.WARNING, "No response data to print.");
        }

//        // TODO : A mieux implémenter
//        if(result.getCommandSide().equals(AtbCommand.CommandSide.SERVER)) {
//            // TODO : Print dans la console et renvoyer la commande au client
//        } else {
//            // TODO : Print le resultat renvoyé par le serveur
//        }
    }

}
