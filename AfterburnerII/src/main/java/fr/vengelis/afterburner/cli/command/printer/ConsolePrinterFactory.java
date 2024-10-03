package fr.vengelis.afterburner.cli.command.printer;

import fr.vengelis.afterburner.cli.command.printer.impl.*;
import fr.vengelis.afterburner.logs.PrintedLog;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConsolePrinterFactory {

    private static final Map<Class<?>, IConsolePrinter<?>> printers = new HashMap<>();

    static {
        printers.put(String.class, new SimplePrinter());
        printers.put(Boolean.class, new BooleanPrinter());
        printers.put(Number.class, new SimplePrinter());

        printers.put(String[].class, new CollectionPrinter());
        printers.put(Number[].class, new CollectionPrinter());
        printers.put(Boolean[].class, new CollectionPrinter());
        printers.put(Collection.class, new CollectionPrinter());

        printers.put(Map.class, new MapPrinter());

        printers.put(PrintedLog.class, new LogPrinter());
    }

    public static void register(Class<?> clazz, IConsolePrinter<?> printer) {
        printers.put(clazz, printer);
    }

    @SuppressWarnings("unchecked")
    public static <T> IConsolePrinter<T> getPrinter(Class<T> clazz) {
        for (Map.Entry<Class<?>, IConsolePrinter<?>> entry : printers.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) {
                return (IConsolePrinter<T>) entry.getValue();
            }
        }
        return null;
    }
}
