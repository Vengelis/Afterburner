package fr.vengelis.afterburner.handler;

import fr.vengelis.afterburner.*;
import fr.vengelis.afterburner.events.impl.common.PreInitEvent;

import java.util.ArrayList;
import java.util.List;

public class HandlerRecorder {

    private static HandlerRecorder instance;
    private final List<PreInitHandler> initHandlers = new ArrayList<>();
    private final List<SuperPreInitHandler> preInitHandlers = new ArrayList<>();

    public HandlerRecorder() {
        instance = this;
    }

    public void register(SuperPreInitHandler handler) {
        preInitHandlers.add(handler);
    }

    public void register(PreInitHandler handler) {
        initHandlers.add(handler);
    }

    public void executeSuperPreInit() {
        preInitHandlers.forEach(SuperPreInitHandler::init);
    }

    public void executePreInit(AApp aapp) {
        initHandlers.forEach(PreInitHandler::init);
        aapp.getEventManager().call(new PreInitEvent());
    }

    public static HandlerRecorder get() {
        return instance;
    }
}
