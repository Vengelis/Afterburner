package fr.vengelis.afterburner.mprocess.argwrapper;

import fr.vengelis.afterburner.exceptions.BrokenConfigException;
import fr.vengelis.afterburner.handler.PreInitHandler;
import fr.vengelis.afterburner.mprocess.argwrapper.impl.*;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.handler.HandlerRecorder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;

import static fr.vengelis.afterburner.Afterburner.VERBOSE;

public class ArgumentWrapperManager implements PreInitHandler {

    private final HashMap<String, BaseArgumentWrapper> argumentWrapperMap = new HashMap<>();
    private boolean alreadyInit = false;

    public ArgumentWrapperManager() {
        HandlerRecorder.get().register(this);
    }

    public void init() {
        if(alreadyInit) return;
        alreadyInit = true;
        register(new JavaArguments());
        register(new SimpleArguments());
        register(new BashArguments());
        register(new ShArguments());
        register(new PythonArguments());
    }

    public void register(BaseArgumentWrapper wrapper) {
        try {
            wrapper.export();
            wrapper.load();
            argumentWrapperMap.put(wrapper.getType().toLowerCase(), wrapper);
            ConsoleLogger.printVerbose(Level.INFO, "Registering new Argument Wrapper '" + wrapper.getType() + "'");
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(new BrokenConfigException(e));
        }
    }

    public Optional<BaseArgumentWrapper> get(String wrapperType) {
        return Optional.ofNullable(argumentWrapperMap.get(wrapperType.toLowerCase()));
    }

}
