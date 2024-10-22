package fr.vengelis.javaversionadapter.adapter;

import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

import static fr.vengelis.afterburner.Afterburner.VERBOSE;

public class AdapterManager {

    private final Map<String, Adapter> adapterMap = new HashMap<>();

    public void register(Adapter adapter) {
        ConsoleLogger.printVerbose(Level.INFO, "Registering new java adapter : " + adapter.getName());
        adapterMap.put(adapter.getName(), adapter);
    }

    public Map<String, Adapter> getAdapters() {
        return adapterMap;
    }

    public Optional<Adapter> getAdapter(String name) {
        return Optional.ofNullable(adapterMap.get(name));
    }
}
