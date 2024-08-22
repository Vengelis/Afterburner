package fr.vengelis.afterburner.instructions;

import fr.vengelis.afterburner.providers.ProviderInstructions;

public abstract class BaseDeadlineInstruction {

    private final ProviderInstructions instruction;

    public BaseDeadlineInstruction(ProviderInstructions providerInstructions) {
        this.instruction = providerInstructions;
    }

    public ProviderInstructions getInstruction() {
        return instruction;
    }

    public abstract String execute();

}
