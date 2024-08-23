package fr.vengelis.afterburner.cli.consumers;

import fr.vengelis.afterburner.cli.AtbCommand;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.function.Consumer;
import java.util.logging.Level;

public class AtbCommandLister implements Consumer<String[]> {

    private final AtbCommand rootCommand;

    public AtbCommandLister(AtbCommand rootCommand) {
        this.rootCommand = rootCommand;
    }

    @Override
    public void accept(String[] strings) {
        listCommands(rootCommand, "");
    }

    private void listCommands(AtbCommand command, String indent) {
        if(!command.getName().equalsIgnoreCase("root"))
            ConsoleLogger.printLine(Level.INFO, indent + " " + command.getName() + (command.getAliases().isEmpty() ? "" : "|" + String.join("|", command.getAliases())) + ": " + command.getDescription());
        else
            ConsoleLogger.printLine(Level.INFO, " HELP CENTER :");
        command.getSubCommands().values().forEach(subCommand -> listCommands(subCommand, indent + "  "));
    }

}
