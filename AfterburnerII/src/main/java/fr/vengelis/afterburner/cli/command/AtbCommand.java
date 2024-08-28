package fr.vengelis.afterburner.cli.command;

import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;

public class AtbCommand {

    private final String name;
    private final String description;
    private Map<String, AtbCommand> subCommands = new HashMap<>();
    private Function<CommandInstruction, ExecutionResult<?>> actionServer;
    private Function<CommandInstruction, ExecutionResult<?>> actionClient;
    private final State commandeState;
    private Set<String> aliases = new HashSet<>();
    private boolean requiresArgument = false;

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
        private Function<CommandInstruction, ExecutionResult<?>> actionServer;
        private Function<CommandInstruction, ExecutionResult<?>> actionClient;
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

        public AtbCommandBuilder setActionServer(Function<CommandInstruction, ExecutionResult<?>> actionServer) {
            this.actionServer = actionServer;
            return this;
        }

        public AtbCommandBuilder setActionClient(Function<CommandInstruction, ExecutionResult<?>> actionClient) {
            this.actionClient = actionClient;
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
            command.setActionServer(actionServer);
            command.setActionClient(actionClient);
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

    public void setActionServer(Function<CommandInstruction, ExecutionResult<?>> actionServer) {
        this.actionServer = actionServer;
    }

    public void setActionClient(Function<CommandInstruction, ExecutionResult<?>> actionClient) {
        this.actionClient = actionClient;
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
            return new CommandResult<>(
                    instruction,
                    CommandResult.ResponseType.ERROR,
                    new ExecutionResult<>(false, "Error: This command requires an argument.")
            );
        }

        Function<CommandInstruction, ExecutionResult<?>> action =
                instruction.getSide() == CommandSide.CLIENT ? actionClient : actionServer;

        if (commandeState.equals(State.FINAL)) {
            if (action != null) {
                ExecutionResult<?> rtn = action.apply(instruction);
                return new CommandResult<>(
                        instruction,
                        (rtn.isSuccess() ? CommandResult.ResponseType.SUCCESS : CommandResult.ResponseType.ERROR),
                        rtn
                );
            } else {
                return new CommandResult<>(
                        instruction,
                        CommandResult.ResponseType.ERROR,
                        new ExecutionResult<>(false, "Error: Missing action on final command.")
                );
            }
        } else {
            ArrayDeque<String> logs = new ArrayDeque<>();
            if (instruction.getArgs().length == 0) {
                if (!requiresArgument && action != null) {
                    ExecutionResult<?> rtn = action.apply(instruction);
                    return new CommandResult<>(
                            instruction,
                            rtn.isSuccess() ? CommandResult.ResponseType.SUCCESS : CommandResult.ResponseType.ERROR,
                            rtn
                    );
                } else {
                    logs.add("Error: Unknown command or alias. Arguments available: ");
                    subCommands.forEach((cn, c) -> {
                        logs.add(" - " + cn + (c.getAliases().isEmpty() ? "" : "|" + String.join("|", c.getAliases())) + " : " + c.getDescription());
                    });
                    return new CommandResult<>(
                            instruction,
                            CommandResult.ResponseType.ERROR,
                            new ExecutionResult<>(false, logs)
                    );
                }
            } else {
                CommandResult actionResult = null;
                if (action != null) {
                    ExecutionResult<?> rtn = action.apply(instruction);
                    actionResult = new CommandResult<>(
                            instruction,
                            rtn.isSuccess() ? CommandResult.ResponseType.COMBINED_SUCCESS : CommandResult.ResponseType.COMBINED_ERROR,
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
                    CommandInstruction shiftedCommand = new CommandInstruction(instruction.getInput(), shiftArgs(instruction.getArgs()), instruction.getSide());
                    CommandResult<?> master = subCommand.execute(shiftedCommand);
                    if(actionResult != null) master.addCombinedResult(actionResult);
                    return master;
                } else {
                    logs.add("Error: Unknown command or alias. Arguments available: ");
                    subCommands.forEach((cn, c) -> {
                        logs.add(" - " + cn + (c.getAliases().isEmpty() ? "" : "|" + String.join("|", c.getAliases())) + " : " + c.getDescription());
                    });
                    CommandResult<?> master = new CommandResult<>(
                            instruction,
                            CommandResult.ResponseType.ERROR,
                            new ExecutionResult<>(false, logs)
                    );
                    if(actionResult != null) master.addCombinedResult(actionResult);
                    return master;
                }
            }
        }
    }

}