package fr.vengelis.afterburner.cli.command;

import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public class AtbCommand {

    private final String name;
    private final String description;
    private Map<String, AtbCommand> subCommands = new HashMap<>();
    private Function<CommandInstruction, ExecutionResult<?>> action;
    private final State commandeState;
    private Set<String> aliases = new HashSet<>();
    private boolean requiresArgument = false;
    private CommandSide commandSide = CommandSide.SERVER;

    public enum CommandSide {
        CLIENT,
        SERVER,
        ;
    }

    public enum State {
        CONTINIOUS,
        FINAL,
        ;
    }

    public static class ExecutionResult<T> {
        private final boolean success;
        private final T responseData;

        public ExecutionResult(boolean success, T responseData) {
            this.success = success;
            this.responseData = responseData;
        }

        public boolean isSuccess() {
            return success;
        }

        public T getResponseData() {
            return responseData;
        }
    }

    public static class AtbCommandBuilder {
        private String name;
        private String description;
        private AtbCommand.State commandeState;
        private Function<CommandInstruction, ExecutionResult<?>> action;
        private Set<String> aliases = new HashSet<>();
        private Map<String, AtbCommand> subCommands = new HashMap<>();
        private boolean requiresArgument = false;
        private CommandSide commandSide = CommandSide.SERVER;

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

        public AtbCommandBuilder setAction(Function<CommandInstruction, ExecutionResult<?>> action) {
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

        public AtbCommandBuilder isClientSide() {
            this.commandSide = CommandSide.CLIENT;
            return this;
        }

        public AtbCommand build() {
            AtbCommand command = new AtbCommand(name, description, commandeState);
            command.setAction(action);
            aliases.forEach(command::addAlias);
            subCommands.values().forEach(command::addSubCommand);
            command.setRequiresArgument(this.requiresArgument);
            command.setCommandSide(commandSide);
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

    public void setAction(Function<CommandInstruction, ExecutionResult<?>> action) {
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

    private String[] shiftArgs(String[] args) {
        if (args.length <= 1) {
            return new String[0];
        }
        String[] newArgs = new String[args.length - 1];
        System.arraycopy(args, 1, newArgs, 0, args.length - 1);
        return newArgs;
    }

    public CommandResult<?> execute(CommandInstruction instruction) {
        if (requiresArgument && (instruction.getArgs() == null || instruction.getArgs().length == 0)) {
            String rtn = "Error: This command requires an argument.";
            ConsoleLogger.printLine(Level.SEVERE, rtn);
            return new CommandResult<>(CommandResult.ResponseType.ERROR, commandSide, new ExecutionResult<>(false, "Error: This command requires an argument."));
        }

        if (commandeState.equals(State.FINAL)) {
            if (this.action != null) {
                ExecutionResult<?> rtn = this.action.apply(instruction);
                return new CommandResult<>(
                        rtn.isSuccess() ? CommandResult.ResponseType.SUCCESS : CommandResult.ResponseType.ERROR,
                        commandSide,
                        rtn
                );
            } else {
                return new CommandResult<>(
                        CommandResult.ResponseType.ERROR,
                        commandSide,
                        new ExecutionResult<>(false, "Error: Missing action on final command.")
                );
            }
        } else {
            ArrayDeque<String> logs = new ArrayDeque<>();
            if (instruction.getArgs().length == 0) {
                if (!requiresArgument && this.action != null) {
                    ExecutionResult<?> rtn = this.action.apply(instruction);
                    return new CommandResult<>(
                            rtn.isSuccess() ? CommandResult.ResponseType.SUCCESS : CommandResult.ResponseType.ERROR,
                            commandSide,
                            rtn
                    );
                } else {
                    logs.add("Error: Unknown command or alias. Arguments available: ");
                    subCommands.forEach((cn, c) -> {
                        logs.add(" - " + cn + (c.getAliases().isEmpty() ? "" : "|" + String.join("|", c.getAliases())) + " : " + c.getDescription());
                    });
                    return new CommandResult<>(
                            CommandResult.ResponseType.ERROR,
                            commandSide,
                            new ExecutionResult<>(false, logs)
                    );
                }
            } else {
                CommandResult actionResult = null;
                if (this.action != null) {
                    ExecutionResult<?> rtn = this.action.apply(instruction);
                    actionResult = new CommandResult<>(
                            rtn.isSuccess() ? CommandResult.ResponseType.COMBINED_SUCCESS : CommandResult.ResponseType.COMBINED_ERROR,
                            commandSide,
                            rtn
                    );
                }
                String nextArg = instruction.getArgs()[0];
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
                    CommandInstruction shiftedCommand = new CommandInstruction(instruction.getInput(), shiftArgs(instruction.getArgs()));
                    CommandResult<?> master = subCommand.execute(shiftedCommand);
                    if(actionResult != null) master.addCombinedResult(actionResult);
                    return master;
                } else {
                    logs.add("Error: Unknown command or alias. Arguments available: ");
                    subCommands.forEach((cn, c) -> {
                        logs.add(" - " + cn + (c.getAliases().isEmpty() ? "" : "|" + String.join("|", c.getAliases())) + " : " + c.getDescription());
                    });
                    CommandResult<?> master = new CommandResult<>(
                            CommandResult.ResponseType.ERROR,
                            commandSide,
                            new ExecutionResult<>(false, logs)
                    );
                    if(actionResult != null) master.addCombinedResult(actionResult);
                    return master;
                }
            }
        }
    }

    public CommandSide getCommandSide() {
        return commandSide;
    }

    public void setCommandSide(CommandSide commandSide) {
        this.commandSide = commandSide;
    }

}
