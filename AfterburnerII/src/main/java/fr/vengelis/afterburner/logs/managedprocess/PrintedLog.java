package fr.vengelis.afterburner.logs.managedprocess;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.LevelTypeAdapter;

import java.util.logging.Level;

public class PrintedLog {

    // Needed when you want to transform the Level type into a Json object by Gson. Another neat trick...
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Level.class, new LevelTypeAdapter())
            .create();

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
        // This method is only used by SLAVE mode hence the fact that we directly use the slave instance here
        AfterburnerSlaveApp.get().getLogHistory().add(this);
    }

    public String serialize() {
//        ConsoleLogger.printLine(Level.CONFIG, "-------");
//        ConsoleLogger.printLine(Level.CONFIG, level.getName());
//        ConsoleLogger.printLine(Level.CONFIG, line);
        return gson.toJson(this);
    }

    public static PrintedLog deserialize(String json) {
        return gson.fromJson(json, PrintedLog.class);
    }
}
