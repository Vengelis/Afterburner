package fr.vengelis.afterburner.arguments;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ArgumentManager {
    private final Map<String, Argument> arguments = new HashMap<>();

    public void addArgument(String key, String value, String alias, BiConsumer<Argument, String> applyFunction) {
        Argument argument = new Argument(key, value, alias, applyFunction);
        arguments.put(key, argument);
        if (alias != null && !alias.isEmpty()) {
            arguments.put(alias, argument);
        }
    }

    public Argument getArgument(String keyOrAlias) {
        return arguments.get(keyOrAlias);
    }

    public void parseArguments(String[] args) {
        for (String arg : args) {
            if (arg.contains("=")) {
                String[] parts = arg.split("=", 2);
                String key = parts[0];
                String value = parts[1];
                Argument argument = getArgument(key);
                if (argument != null) {
                    argument.apply(value);
                }
            } else {
                Argument argument = getArgument(arg);
                if (argument != null) {
                    argument.apply(arg);
                }
            }
        }
    }
}
