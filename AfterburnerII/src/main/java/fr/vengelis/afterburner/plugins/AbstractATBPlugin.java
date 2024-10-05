package fr.vengelis.afterburner.plugins;

import fr.vengelis.afterburner.Afterburner;
import fr.vengelis.afterburner.cli.command.AtbCommand;

import java.io.File;

public abstract class AbstractATBPlugin {

    protected String getWorkingDirectory() {
        return Afterburner.WORKING_AREA + File.separator + "plugins" + File.separator;
    }

    public abstract void registerCommands(final AtbCommand root);
    public abstract void onLoad();

}
