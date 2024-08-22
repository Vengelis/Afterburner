package fr.vengelis.afterburner.providers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AfterburnerProvider {
    String name();
}
