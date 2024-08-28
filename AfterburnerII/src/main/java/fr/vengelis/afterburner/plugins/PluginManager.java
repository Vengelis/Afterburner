package fr.vengelis.afterburner.plugins;

import fr.vengelis.afterburner.Afterburner;
import fr.vengelis.afterburner.exceptions.BrokenPluginException;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.ProviderAndPluginUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;

public class PluginManager {

    private Map<String, AbstractATBPlugin> plugins = new HashMap<>();

    public void loadPlugins(String folderPath) {
        ConsoleLogger.printLine(Level.INFO, "Searching and registering plugin");
        File[] files = new File(folderPath).listFiles((dir, name) -> name.endsWith(".jar"));

        if (files != null) {
            for (File file : files) {
                processFile(file);
            }
        }
    }

    private void processFile(File file) {
        try (JarFile jarFile = new JarFile(file)) {
            URLClassLoader classLoader = new URLClassLoader(new URL[]{file.toURI().toURL()}, getClass().getClassLoader());
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (ProviderAndPluginUtils.isValidClassEntry(entry)) {
                    processClassEntry(entry, classLoader);
                }
                if (ProviderAndPluginUtils.isValidYmlEntry(entry)) {
                    ProviderAndPluginUtils.extractYmlFile(entry, jarFile, "plugins");
                }
            }
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(new BrokenPluginException(e),
                    "The plugin jar file could not be loaded : " + file.getName(),
                    "Please check the file integrity and try again",
                    "The dependencies added to your plugins are not well implemented.",
                    "ClassNotFound errors can occur depending on the circumstances");
        }
    }

    private void processClassEntry(JarEntry entry, URLClassLoader classLoader) {
        String className = entry.getName().replace("/", ".").replace(".class", "");
        try {
            Class<?> loadedClass = classLoader.loadClass(className);
            processAnnotations(loadedClass.getAnnotations(), loadedClass);
        } catch (ClassNotFoundException e) {
            ConsoleLogger.printLine(Level.SEVERE, "The class could not be loaded : " + className);
        }
    }

    private void processAnnotations(Annotation[] annotations, Class<?> loadedClass) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof ATBPlugin) {
                ATBPlugin atbPluginAnnotation = (ATBPlugin) annotation;
                registerPlugin(atbPluginAnnotation, loadedClass);
            }
        }
    }

    private void registerPlugin(ATBPlugin atbPluginAnnotation, Class<?> loadedClass) {
        if (!plugins.containsKey(atbPluginAnnotation.name())) {
            if(AbstractATBPlugin.class.isAssignableFrom(loadedClass)) {
                try {
                    if(atbPluginAnnotation.launchType().equals(Afterburner.getLaunchType())) {
                        AbstractATBPlugin pluginInstance = (AbstractATBPlugin) loadedClass.newInstance();
                        plugins.put(atbPluginAnnotation.name(), pluginInstance);
                        ConsoleLogger.printLine(Level.INFO, " - " + atbPluginAnnotation.name() + " finded");
                    } else {
                        ConsoleLogger.printLine(Level.INFO, " - " + atbPluginAnnotation.name() + " skipped (LaunchType is different from AApp)");
                    }

                } catch (InstantiationException | IllegalAccessException e) {
                    ConsoleLogger.printStacktrace(e);
                }
            } else {
                ConsoleLogger.printLine(Level.WARNING, " - " + atbPluginAnnotation.name() + " is not instance of AbstractATBPlugin. Plugin not loaded.");
            }
        } else {
            ConsoleLogger.printLine(Level.SEVERE, "A plugin with the same name already exists: " + atbPluginAnnotation.name());
        }
    }

    public AbstractATBPlugin getPlugin(String name) {
        return plugins.get(name);
    }

    public Map<String, AbstractATBPlugin> getPlugins() {
        return plugins;
    }

}
