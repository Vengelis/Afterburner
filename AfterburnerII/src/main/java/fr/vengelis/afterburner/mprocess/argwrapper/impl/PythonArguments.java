package fr.vengelis.afterburner.mprocess.argwrapper.impl;

import fr.vengelis.afterburner.mprocess.argwrapper.BaseArgumentWrapper;

public class PythonArguments extends BaseArgumentWrapper {

    public PythonArguments() {
        super(true);
    }

    @Override
    public String getType() {
        return "python";
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
