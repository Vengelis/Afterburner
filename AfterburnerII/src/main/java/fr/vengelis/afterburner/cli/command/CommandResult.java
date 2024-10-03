package fr.vengelis.afterburner.cli.command;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

public class CommandResult<T> {

    private static final Gson gson = new GsonBuilder().create();

    private final CommandInstruction instruction;
    private final ResponseType type;
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

    public CommandResult(CommandInstruction instruction, ResponseType type, AtbCommand.ExecutionResult<T> responseData) {
        this.instruction = instruction;
        this.type = type;
        this.responseData = responseData;
    }

    public ResponseType getType() {
        return type;
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

    public CommandInstruction getInstruction() {
        return instruction;
    }

    public AtbCommand.ExecutionResult<T> getResponseData() {
        return responseData;
    }

    public String serialize() {
        return gson.toJson(this);
    }

    public static <T> CommandResult deserialize(String json, Class<T> clazz) {
        return gson.fromJson(json, CommandResult.class);
    }

    public static CommandResult<?> deserialize(String json) {
        return gson.fromJson(json, CommandResult.class);
    }
}
