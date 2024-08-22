package fr.vengelis.afterburner.exceptions;

import java.io.IOException;

public class BrokenProviderException extends IOException {

    public BrokenProviderException(Throwable cause) {
        super("The provider entered has a problem and could not be loaded", cause);
    }
}
