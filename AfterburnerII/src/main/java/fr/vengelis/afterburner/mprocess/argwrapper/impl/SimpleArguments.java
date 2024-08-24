package fr.vengelis.afterburner.mprocess.argwrapper.impl;

import fr.vengelis.afterburner.mprocess.argwrapper.BaseArgumentWrapper;

public class SimpleArguments extends BaseArgumentWrapper {
    public SimpleArguments() {
        super(true);
    }

    @Override
    public String getType() {
        return "simple";
    }

    @Override
    public String getBaseLauncher() {
        return getFinalExecutable();
    }

    @Override
    protected String getPrefixMinimalRam() {
        return "";
    }

    @Override
    protected String getPrefixMaximumRam() {
        return "";
    }

    @Override
    protected String getPrefixExecutable() {
        return "";
    }
}
