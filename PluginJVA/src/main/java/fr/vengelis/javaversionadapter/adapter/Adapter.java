package fr.vengelis.javaversionadapter.adapter;

import fr.vengelis.afterburner.AfterburnerSlaveApp;

import java.util.regex.Pattern;

public class Adapter {

    private final String name;
    private final String regex;
    private final String exec;

    public Adapter(String name, String regex, String exec) {
        this.name = name;
        this.regex = regex;
        this.exec = exec;
    }

    public boolean matchName() {
        return Pattern.compile(this.regex, Pattern.CASE_INSENSITIVE).matcher(AfterburnerSlaveApp.get().getMachineName()).find();
    }

    public String getName() {
        return name;
    }

    public String getRegex() {
        return regex;
    }

    public String getExec() {
        return exec;
    }
}
