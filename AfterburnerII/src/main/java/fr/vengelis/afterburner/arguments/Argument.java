package fr.vengelis.afterburner.arguments;

import java.util.function.BiConsumer;

public class Argument {
    private final String key;
    private final String value;
    private final String alias;
    private final BiConsumer<Argument, String> applyFunction;

    public Argument(String key, String value, String alias, BiConsumer<Argument, String> applyFunction) {
        this.key = key;
        this.value = value;
        this.alias = alias;
        this.applyFunction = applyFunction;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getAlias() {
        return alias;
    }

    public void apply(String value) {
        applyFunction.accept(this, value);
    }
}
