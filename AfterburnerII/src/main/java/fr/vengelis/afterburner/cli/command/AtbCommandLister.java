package fr.vengelis.afterburner.cli.command;

import fr.vengelis.afterburner.language.LanguageManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.Stack;
import java.util.function.Function;
import java.util.logging.Level;

public class AtbCommandLister implements Function<CommandInstruction, AtbCommand.ExecutionResult<?>> {

    private final AtbCommand rootCommand;

    public AtbCommandLister(AtbCommand rootCommand) {
        this.rootCommand = rootCommand;
    }

    @Override
    public AtbCommand.ExecutionResult<?> apply(CommandInstruction instruction) {
        listCommands(rootCommand);
        return new AtbCommand.ExecutionResult<>(true, "Commands listed successfully");
    }

    private void listCommands(AtbCommand command) {
        Stack<AtbCommand> stack = new Stack<>();
        stack.push(command);

        while (!stack.isEmpty()) {
            AtbCommand currentCommand = stack.pop();
            String indent = getIndent(currentCommand);

            if (!currentCommand.getName().equalsIgnoreCase("root")) {
                ConsoleLogger.printLine(Level.INFO, indent + " " + currentCommand.getName() +
                        (currentCommand.getAliases().isEmpty() ? "" : "|" + String.join("|", currentCommand.getAliases())) +
                        ": " + currentCommand.getDescription());
            } else {
                ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("command-help-center"));
            }

            currentCommand.getSubCommands().values().forEach(stack::push);
        }
    }

    private String getIndent(AtbCommand command) {
        int depth = 0;
        AtbCommand current = command;
        while (current != null) {
            depth++;
            current = findParentCommand(current);
        }
        return new String(new char[depth - 1]).replace("\0", "  ");
    }

    private AtbCommand findParentCommand(AtbCommand command) {
        for (AtbCommand parent : rootCommand.getSubCommands().values()) {
            if (parent.getSubCommands().containsValue(command)) {
                return parent;
            }
            AtbCommand found = findParentCommandRecursive(parent, command);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private AtbCommand findParentCommandRecursive(AtbCommand parent, AtbCommand command) {
        for (AtbCommand subCommand : parent.getSubCommands().values()) {
            if (subCommand.equals(command)) {
                return parent;
            }
            AtbCommand found = findParentCommandRecursive(subCommand, command);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

}
