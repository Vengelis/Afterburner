package fr.vengelis.afterburner.events;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventHandler {

    EventPriority priority() default EventPriority.NORMAL;

}
