package fr.vengelis.afterburner.cli;

import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.*;
import java.util.function.Consumer;
import java.util.logging.Level;

public class AtbCommand {

    private final String name;
    private final String description;
    private Map<String, AtbCommand> subCommands = new HashMap<>();
    private Consumer<String[]> action;
    private final State commandeState;
    private Set<String> aliases = new HashSet<>();
    private boolean requiresArgument = false;

    public enum State {
        CONTINIOUS,
        FINAL,
        ;
    }

    public static class AtbCommandBuilder {
        private String name;
        private String description;
        private AtbCommand.State commandeState;
        private Consumer<String[]> action;
        private Set<String> aliases = new HashSet<>();
        private Map<String, AtbCommand> subCommands = new HashMap<>();
        private boolean requiresArgument = false;

        public AtbCommandBuilder(State commandeState) {
            this.commandeState = commandeState;
        }

        public AtbCommandBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public AtbCommandBuilder setDescription(String description) {
            this.description = description;
            return this;
        }

        public AtbCommandBuilder setCommandeState(AtbCommand.State commandeState) {
            this.commandeState = commandeState;
            return this;
        }

        public AtbCommandBuilder setAction(Consumer<String[]> action) {
            this.action = action;
            return this;
        }

        public AtbCommandBuilder addAlias(String... alias) {
            this.aliases.addAll(Arrays.asList(alias));
            return this;
        }

        public AtbCommandBuilder addSubCommand(AtbCommand subCommand) {
            this.subCommands.put(subCommand.name, subCommand);
            return this;
        }

        public AtbCommandBuilder requiresArgument() {
            this.requiresArgument = true;
            return this;
        }

        public AtbCommand build() {
            AtbCommand command = new AtbCommand(name, description, commandeState);
            command.setAction(action);
            aliases.forEach(command::addAlias);
            subCommands.values().forEach(command::addSubCommand);
            command.setRequiresArgument(this.requiresArgument);
            return command;
        }
    }

    public AtbCommand(String name, String description, State commandeState) {
        this.name = name;
        this.description = description;
        this.commandeState = commandeState;
    }

    public void addSubCommand(AtbCommand subCommand) {
        subCommands.put(subCommand.name, subCommand);
    }

    public Map<String, AtbCommand> getSubCommands() {
        return subCommands;
    }

    public void setAction(Consumer<String[]> action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void addAlias(String... alias) {
        aliases.addAll(Arrays.asList(alias));
    }

    public Set<String> getAliases() {
        return aliases;
    }

    public boolean isRequiresArgument() {
        return requiresArgument;
    }

    public void setRequiresArgument(boolean requiresArgument) {
        this.requiresArgument = requiresArgument;
    }

    public void execute(String[] args) {
        if (requiresArgument && (args == null || args.length == 0)) {
            ConsoleLogger.printLine(Level.SEVERE, "Error: This command requires an argument.");
            return;
        }

        if (commandeState.equals(State.FINAL)) {
            if (this.action != null) {
                this.action.accept(args);
            }
        } else {
            if (args.length == 0) {
                if (!requiresArgument && this.action != null) {
                    this.action.accept(args);
                } else {
                    ConsoleLogger.printLine(Level.SEVERE, "Error: Unknown command. Arguments available: ");
                    subCommands.forEach((cn, c) -> ConsoleLogger.printLine(Level.SEVERE, " - " + cn + (c.getAliases().isEmpty() ? "" : "|" + String.join("|", c.getAliases())) + " : " + c.getDescription()));
                }
            } else {
                String nextArg = args[0];
                AtbCommand subCommand = subCommands.get(nextArg);
                if (subCommand == null) {
                    for (AtbCommand cmd : subCommands.values()) {
                        if (cmd.getAliases().contains(nextArg)) {
                            subCommand = cmd;
                            break;
                        }
                    }
                }
                if (subCommand != null) {
                    subCommand.execute(shiftArgs(args));
                } else {
                    ConsoleLogger.printLine(Level.SEVERE, "Error: Unknown command or alias. Arguments available: ");
                    subCommands.forEach((cn, c) -> ConsoleLogger.printLine(Level.SEVERE, " - " + cn + (c.getAliases().isEmpty() ? "" : "|" + String.join("|", c.getAliases())) + " : " + c.getDescription()));
                }
            }
        }
    }

    private String[] shiftArgs(String[] args) {
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

}
