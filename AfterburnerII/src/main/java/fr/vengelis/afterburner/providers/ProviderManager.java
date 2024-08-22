package fr.vengelis.afterburner.providers;

import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.exceptions.BrokenProviderException;
import fr.vengelis.afterburner.providers.impl.CommandLineProvider;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.ProviderAndPluginUtils;

import java.io.*;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class ProviderManager {

    private Map<String, IAfterburnerProvider> providers = new HashMap<>();

    public void loadProviders(String folderPath) {
        ConsoleLogger.printLine(Level.INFO, "Searching and registering providers");
        File[] files = new File(folderPath).listFiles((dir, name) -> name.endsWith(".jar"));

        registerProvider("CommandLine", new CommandLineProvider());

        if (files != null) {
            for (File file : files) {
                try (JarFile jarFile = new JarFile(file)) {
                    URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader());
                    Enumeration<JarEntry> entries = jarFile.entries();

                    while (entries.hasMoreElements()) {
                        JarEntry entry = entries.nextElement();
                        if (ProviderAndPluginUtils.isValidClassEntry(entry)) {
                            registerProviderFromClassEntry(entry, classLoader);
                        }
                        if (ProviderAndPluginUtils.isValidYmlEntry(entry)) {
                            ProviderAndPluginUtils.extractYmlFile(entry, jarFile, "providers");
                        }
                    }
                } catch (IOException e) {
                    ConsoleLogger.printStacktrace(new BrokenProviderException(e),
                            "The provider jar file could not be loaded : " + file.getName(),
                            "Please check the file integrity and try again",
                            "The dependencies added to your providers are not well implemented.",
                            "ClassNotFound errors can occur depending on the circumstances");
                }
            }
        }
    }

    private void registerProviderFromClassEntry(JarEntry entry, URLClassLoader classLoader) {
        String className = entry.getName().replace("/", ".").replace(".class", "");
        try {
            Class<?> loadedClass = classLoader.loadClass(className);
            for (Annotation annotation : loadedClass.getAnnotations()) {
                if (annotation instanceof AfterburnerProvider) {
                    AfterburnerProvider atbPluginAnnotation = (AfterburnerProvider) annotation;
                    if (!providers.containsKey(atbPluginAnnotation.name())) {
                        try {
                            registerProvider(atbPluginAnnotation.name(), (IAfterburnerProvider) loadedClass.newInstance());
                        } catch (InstantiationException | IllegalAccessException e) {
                            ConsoleLogger.printStacktrace(e);
                        }
                    } else {
                        ConsoleLogger.printLine(Level.SEVERE, "A provider with the same name already exists: " + atbPluginAnnotation.name());
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            ConsoleLogger.printLine(Level.SEVERE, "The class could not be loaded : " + className);
        }
    }

    private void registerProvider(String name, IAfterburnerProvider provider) {
        providers.put(name.toUpperCase(), provider);
        ConsoleLogger.printLine(Level.INFO, " - " + name + " registered");
    }

    public Object getResultInstruction(ProviderInstructions providerInstructions) {
        return ((Map<ProviderInstructions, IAfterburnerProvider>)ConfigGeneral.PROVIDERS.getData()).get(providerInstructions).getInstructionValue(providerInstructions);
    }

    public IAfterburnerProvider getProvider(String name) {
        return providers.get(name);
    }

    public Map<String, IAfterburnerProvider> getProviders() {
        return providers;
    }

}
