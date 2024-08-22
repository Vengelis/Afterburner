package fr.vengelis.afterburner.utils;

import fr.vengelis.afterburner.Afterburner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ProviderAndPluginUtils {

    public static boolean isValidClassEntry(JarEntry entry) {
        return !entry.isDirectory() && entry.getName().endsWith(".class") && !entry.getName().startsWith("META-INF/");
    }

    public static boolean isValidYmlEntry(JarEntry entry) {
        return !entry.isDirectory() && entry.getName().endsWith(".yml");
    }

    public static void extractYmlFile(JarEntry entry, JarFile jarFile, String zone) {
        String jarName = jarFile.getName().substring(jarFile.getName().lastIndexOf(File.separator) + 1).split("\\.")[0];
        String path = Afterburner.WORKING_AREA + File.separator + zone + File.separator + jarName;
        File outputFile = new File(path + File.separator + entry.getName());
        if (!outputFile.getParentFile().exists()) {
            try {
                outputFile.getParentFile().mkdirs();
            } catch (SecurityException e) {
                ConsoleLogger.printStacktrace(e,
                        "Unable to create directory " + outputFile.getParentFile(),
                        "Has the program the right to create directories ?",
                        "Please check the permissions.",
                        "YAML file extraction aborted.");
                return;
            }
        }
        if (!outputFile.exists()) {
            try (InputStream inputStream = jarFile.getInputStream(entry);
                 FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                ConsoleLogger.printStacktrace(e,
                        "Unable to extract " + entry.getName() + " from " + jarName,
                        "Has the program the right to write in the file ?",
                        "Please check the permissions.",
                        "YAML file extraction aborted.");
            }
        }
    }

}
