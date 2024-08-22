package fr.vengelis.afterburner.logs;

import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public class PrintedLog {

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
        AfterburnerApp.get().getLogHistory().add(this);
    }
}
