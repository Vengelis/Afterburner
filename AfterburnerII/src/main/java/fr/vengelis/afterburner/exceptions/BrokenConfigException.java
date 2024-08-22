package fr.vengelis.afterburner.exceptions;

import java.io.IOException;

public class BrokenConfigException extends IOException {

    public BrokenConfigException(Throwable cause) {
        super("Configurations was broken", cause);
    }
}
