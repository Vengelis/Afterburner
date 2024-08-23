package fr.vengelis.afterburner.mprocess.argwrapper.impl;

import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.mprocess.argwrapper.BaseArgumentWrapper;

public class JavaArguments extends BaseArgumentWrapper {
    @Override
    public String getType() {
        return "java";
    }

    @Override
    public String getBaseLauncher() {
        return ConfigGeneral.PATH_JAVA.getData().toString();
    }

    @Override
    protected String getPrefixMinimalRam() {
        return "-Xms";
    }

    @Override
    protected String getPrefixMaximumRam() {
        return "-Xmx";
    }

    @Override
    protected String getPrefixExecutable() {
        return "-jar";
    }
}
