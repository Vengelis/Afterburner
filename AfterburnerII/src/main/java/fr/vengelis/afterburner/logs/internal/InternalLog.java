package fr.vengelis.afterburner.logs.internal;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.vengelis.afterburner.logs.managedprocess.PrintedLog;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.LevelTypeAdapter;

import java.util.logging.Level;

public class InternalLog {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Level.class, new LevelTypeAdapter())
            .create();

    private final Level level;
    private final String line;

    public InternalLog(Level level, String line) {
        this.level = level;
        this.line = line;
    }

    public Level getLevel() {
        return level;
    }

    public String getLine() {
        return line;
    }

    public void save() {
        InternalLogManager.get().save(ConsoleLogger.getLog(getLevel(), getLine()));
    }

    public String serialize() {
        return gson.toJson(this);
    }

    public static PrintedLog deserialize(String json) {
        return gson.fromJson(json, PrintedLog.class);
    }
}
