package fr.vengelis.afterburner.events.impl.client;

import fr.vengelis.afterburner.cli.command.CommandResult;
import fr.vengelis.afterburner.events.AbstractEvent;

public class CommandReceiveResultEvent extends AbstractEvent {

    private final CommandResult<?> commandResult;

    public CommandReceiveResultEvent(CommandResult<?> commandResult) {
        this.commandResult = commandResult;
    }

    public CommandResult<?> getCommandResult() {
        return commandResult;
    }
}
