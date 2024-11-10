package fr.vengelis.afterburner.commonfiles;

import fr.vengelis.afterburner.language.LanguageManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.logging.Level;

public abstract class AbstractBCF implements BaseCommonFile{

    protected final String name;
    protected boolean enabled;

    public AbstractBCF(String name, boolean enabled) {
        this.name = name;
        this.enabled = enabled;
        ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("bcf-enabled"), this.name, this.enabled));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
