package fr.vengelis.afterburner.cli.command;

import java.util.ArrayList;
import java.util.List;

public class CommandResult<T> {

    private final ResponseType type;
    private final AtbCommand.CommandSide commandSide;
    private final AtbCommand.ExecutionResult<T> responseData;
    private final List<CommandResult<?>> combinedResults = new ArrayList<>();

    public enum ResponseProcess {
        PRIMARY,
        COMBINED,
        ;
    }

    public enum ResponseType {
        SUCCESS,
        COMBINED_SUCCESS,
        ERROR,
        COMBINED_ERROR,
        ;
    }

    public CommandResult(ResponseType type, AtbCommand.CommandSide commandSide, AtbCommand.ExecutionResult<T> responseMessage) {
        this.type = type;
        this.commandSide = commandSide;
        this.responseData = responseMessage;
    }

    public ResponseType getType() {
        return type;
    }

    public AtbCommand.CommandSide getCommandSide() {
        return commandSide;
    }

    public AtbCommand.ExecutionResult<T> getExecutionResult() {
        return responseData;
    }

    public ResponseProcess getResponseProcess() {
        return !combinedResults.isEmpty() ? ResponseProcess.COMBINED : ResponseProcess.PRIMARY;
    }

    public List<CommandResult<?>> getCombinedResults() {
        return combinedResults;
    }

    public void addCombinedResult(CommandResult<?> result) {
        combinedResults.add(result);
    }
}
