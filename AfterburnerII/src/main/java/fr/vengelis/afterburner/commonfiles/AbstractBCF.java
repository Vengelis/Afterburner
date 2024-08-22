package fr.vengelis.afterburner.commonfiles;

import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public abstract class AbstractBCF implements BaseCommonFile{

    protected final String name;
    protected final boolean enabled;

    public AbstractBCF(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
        ConsoleLogger.printLine(Level.INFO, "    | " + this.name + " (Enabled : " + this.enabled + ")");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
