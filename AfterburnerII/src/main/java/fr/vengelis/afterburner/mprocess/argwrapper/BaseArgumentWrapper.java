package fr.vengelis.afterburner.mprocess.argwrapper;

import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.configurations.ConfigTemplate;

import java.io.File;

public abstract class BaseArgumentWrapper implements IArgWrapper{

    public abstract String getType();
    public abstract String getBaseLauncher();

    protected abstract String getPrefixMinimalRam();
    protected abstract String getPrefixMaximumRam();
    protected abstract String getPrefixExecutable();

    public String getFinalMinimalRam() {
        return getPrefixMinimalRam()+ ConfigTemplate.EXECUTABLE_MIN_RAM.getData();
    }

    public String getFinalMaximumRam() {
        return getPrefixMaximumRam() + ConfigTemplate.EXECUTABLE_MAX_RAM.getData();
    }

    public String getFinalExecutable() {
        return getPrefixExecutable() + " \"" + ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString() + File.separator + ConfigTemplate.EXECUTABLE_NAME.getData() + "\"";
    }

}
