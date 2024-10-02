package fr.vengelis.afterburner.plugins;

import fr.vengelis.afterburner.Afterburner;

import java.io.File;

public abstract class AbstractATBPlugin {

    protected String getWorkingDirectory() {
        return Afterburner.WORKING_AREA + File.separator + "plugins" + File.separator;
    }

    public abstract void onLoad();

}
