package fr.vengelis.afterburner.cli.command;

import fr.vengelis.afterburner.AfterburnerClientApp;

public class ClientCommandAction {

    public static AtbCommand.ExecutionResult<Boolean> perform(CommandInstruction instruction) {
        AfterburnerClientApp.get().getClient().sendCommand(instruction);
        return new AtbCommand.ExecutionResult<>(
                true,
                true
        );
    }

}
