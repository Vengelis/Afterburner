package fr.vengelis.afterburner.mprocess.argwrapper.impl;

import fr.vengelis.afterburner.mprocess.argwrapper.BaseArgumentWrapper;

public class ShArguments extends BaseArgumentWrapper {

    public ShArguments() {
        super(true);
    }

    @Override
    public String getType() {
        return "sh";
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
