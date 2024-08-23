package fr.vengelis.afterburner.mprocess.argwrapper;

public interface IArgWrapper {

    String getType();
    String getBaseLauncher();

    String getFinalMinimalRam();
    String getFinalMaximumRam();
    String getFinalExecutable();
}
