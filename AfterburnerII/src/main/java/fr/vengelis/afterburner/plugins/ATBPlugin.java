package fr.vengelis.afterburner.plugins;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ATBPlugin {
    String name();
}
