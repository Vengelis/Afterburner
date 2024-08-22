package fr.vengelis.afterburner.exceptions;

public class UnknownProviderException extends Exception{

    public UnknownProviderException(String inputProvider) {
        super("The requested provider (" + inputProvider + ") was not found in the list of loaded providers");
    }
}
