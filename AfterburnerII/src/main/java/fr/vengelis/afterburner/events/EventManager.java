package fr.vengelis.afterburner.events;

import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.lang.reflect.Method;
import java.util.*;

public class EventManager {
    private final Map<Class<? extends AbstractEvent>, Map<EventPriority, List<Listener>>> eventListeners = new HashMap<>();

    public void register(Listener listener) {
        Arrays.stream(listener.getClass().getDeclaredMethods())
            .filter(method -> method.isAnnotationPresent(EventHandler.class))
            .forEach(method -> {
                EventHandler annotation = method.getAnnotation(EventHandler.class);
                EventPriority priority = annotation.priority();
                Class<? extends AbstractEvent> eventType = (Class<? extends AbstractEvent>) method.getParameterTypes()[0];
                eventListeners.computeIfAbsent(eventType, k -> new HashMap<>())
                    .computeIfAbsent(priority, k -> new ArrayList<>())
                    .add(listener);
            });
    }

    public void call(AbstractEvent event) {
        Optional.ofNullable(eventListeners.get(event.getClass()))
                .ifPresent(priorityListeners -> {
                    for (EventPriority priority : EventPriority.values()) {
                        Optional.ofNullable(priorityListeners.get(priority))
                                .ifPresent(listeners -> listeners.forEach(listener -> invokeMethods(listener, event, priority)));
                    }
                });
    }

    private void invokeMethods(Listener listener, AbstractEvent event, EventPriority priority) {
        Arrays.stream(listener.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(EventHandler.class))
                .filter(method -> method.getAnnotation(EventHandler.class).priority() == priority)
                .filter(method -> isSingleParameterAssignableFromEvent(method, event))
                .forEach(method -> invokeMethod(listener, event, method));
    }

    private boolean isSingleParameterAssignableFromEvent(Method method, AbstractEvent event) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        return parameterTypes.length == 1 && parameterTypes[0].isAssignableFrom(event.getClass());
    }

    private void invokeMethod(Listener listener, AbstractEvent event, Method method) {
        try {
            method.invoke(listener, event);
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
        }
    }
}
