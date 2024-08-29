package fr.vengelis.afterburner.events.impl.common;

import fr.vengelis.afterburner.cli.command.CommandInstruction;
import fr.vengelis.afterburner.events.AbstractCancelableEvent;

public class SendInstructionEvent extends AbstractCancelableEvent {

    private final CommandInstruction instruction;
    private String cancelReason = "No reason was specified";

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
}
