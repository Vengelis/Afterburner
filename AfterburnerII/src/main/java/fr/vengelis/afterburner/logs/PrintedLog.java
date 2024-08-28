package fr.vengelis.afterburner.logs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public class PrintedLog {

    private static final Gson gson = new GsonBuilder().create();

    private final Level level;
    private final String line;
    private boolean skip = false;

    public PrintedLog(String line) {
        this(Level.FINER, line);
    }

    public PrintedLog(Level level, String line) {
        this.level = level;
        this.line = line;
    }

    public String getLine() {
        return line;
    }

    public boolean isSkip() {
        return skip;
    }

    public PrintedLog setSkip(boolean skip) {
        this.skip = skip;
        return this;
    }

    public Level getLevel() {
        return level;
    }

    public PrintedLog print() {
        ConsoleLogger.printLine(this.level, line);
        return this;
    }

    public void save() {
        AfterburnerSlaveApp.get().getLogHistory().add(this);
    }

    public String serialize() {
        return gson.toJson(this);
    }

    public static PrintedLog deserialize(String json) {
        return gson.fromJson(json, PrintedLog.class);
    }
}
