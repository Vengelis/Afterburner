package fr.vengelis.afterburner.exceptions;

public class ProviderUnknownInstructionException extends Exception {

    public ProviderUnknownInstructionException(Throwable cause) {
        super("The requested instruction does not exist", cause);
    }
}
