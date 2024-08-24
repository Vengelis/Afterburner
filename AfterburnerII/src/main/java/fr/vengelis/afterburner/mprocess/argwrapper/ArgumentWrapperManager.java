package fr.vengelis.afterburner.mprocess.argwrapper;

import fr.vengelis.afterburner.mprocess.argwrapper.impl.JavaArguments;
import fr.vengelis.afterburner.mprocess.argwrapper.impl.SimpleArguments;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;

public class ArgumentWrapperManager {

    private final HashMap<String, BaseArgumentWrapper> argumentWrapperMap = new HashMap<>();
    private boolean alreadyInit = false;

    public void init() {
        if(alreadyInit) return;
        alreadyInit = true;
        register(new JavaArguments());
        register(new SimpleArguments());
    }

    public void register(BaseArgumentWrapper wrapper) {
        argumentWrapperMap.put(wrapper.getType(), wrapper);
        ConsoleLogger.printLine(Level.INFO, "Registering new Argument Wrapper '" + wrapper.getType() + "'");
    }

    public Optional<BaseArgumentWrapper> get(String wrapperType) {
        return Optional.ofNullable(argumentWrapperMap.get(wrapperType));
    }

}
