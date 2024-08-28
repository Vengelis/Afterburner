package fr.vengelis.afterburner.cli.command.printer;

import com.sun.org.apache.xpath.internal.operations.Number;
import fr.vengelis.afterburner.cli.command.printer.impl.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ConsolePrinterFactory {

    private static final Map<Class<?>, IConsolePrinter<?>> printers = new HashMap<>();

    static {
        printers.put(String.class, new SimplePrinter());
        printers.put(Boolean.class, new SimplePrinter());
        printers.put(Number.class, new SimplePrinter());

        printers.put(String[].class, new CollectionPrinter());
        printers.put(Number[].class, new CollectionPrinter());
        printers.put(Boolean[].class, new CollectionPrinter());
        printers.put(Collection.class, new CollectionPrinter());

        printers.put(Map.class, new MapPrinter());
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
