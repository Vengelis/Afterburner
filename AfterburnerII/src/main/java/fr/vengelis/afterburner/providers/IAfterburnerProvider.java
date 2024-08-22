package fr.vengelis.afterburner.providers;

import fr.vengelis.afterburner.Afterburner;

import java.io.File;

public interface IAfterburnerProvider {

    default String getWorkingDirectory() {
        return Afterburner.WORKING_AREA + File.separator + "providers" + File.separator;
    }
    Object getInstructionValue(ProviderInstructions providerInstructions);

}
