package fr.vengelis.afterburner.mprocess.argwrapper.impl;

import fr.vengelis.afterburner.mprocess.argwrapper.BaseArgumentWrapper;

public class BashArguments extends BaseArgumentWrapper {

    public BashArguments() {
        super(true);
    }

    @Override
    public String getType() {
        return "bash";
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
