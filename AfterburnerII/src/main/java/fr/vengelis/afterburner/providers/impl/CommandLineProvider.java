package fr.vengelis.afterburner.providers.impl;

import fr.vengelis.afterburner.providers.IAfterburnerProvider;
import fr.vengelis.afterburner.providers.ProviderInstructions;

import java.util.Arrays;

public class CommandLineProvider implements IAfterburnerProvider {

    @Override
    public Object getInstructionValue(ProviderInstructions providerInstructions) {
        String instructionPrefix = "ATB_" + providerInstructions.name() + "=";
        return Arrays.stream(System.getProperty("sun.java.command").split(" "))
                .filter(arg -> arg.startsWith(instructionPrefix))
                .findFirst()
                .map(arg -> arg.substring(instructionPrefix.length()).replace("\"", "").replace("<space>", " "))
                .orElse("N/A");
    }
}
