package fr.vengelis.afterburner.mprocess.argwrapper.impl;

import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.mprocess.argwrapper.BaseArgumentWrapper;

public class JavaArguments extends BaseArgumentWrapper {

    public JavaArguments() {
        super(false);
    }

    @Override
    public String getType() {
        return "java";
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
