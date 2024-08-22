package fr.vengelis.afterburner.exceptions;

import java.io.IOException;

public class BrokenPluginException extends IOException {

    public BrokenPluginException(Throwable cause) {
        super("The plugin entered has a problem and could not be loaded", cause);
    }

}
