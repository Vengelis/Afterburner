package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.cli.command.CommandInstruction;
import fr.vengelis.afterburner.events.AbstractEvent;
import fr.vengelis.afterburner.events.CancellableEvent;

public class SendInstructionEvent extends AbstractEvent implements CancellableEvent {

    private final CommandInstruction instruction;
    private String cancelReason = "No reason was specified";
    private boolean cancelled = false;

    public SendInstructionEvent(CommandInstruction instruction) {
        this.instruction = instruction;
    }

    public CommandInstruction getInstruction() {
        return instruction;
    }

    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
