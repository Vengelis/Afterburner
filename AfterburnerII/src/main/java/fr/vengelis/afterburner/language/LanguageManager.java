package fr.vengelis.afterburner.language;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private static final Map<String, Map<String, String>> languages = new HashMap<>();
    private static String currentLanguage = "en_US";

    public static void loadLanguage(String languageCode, InputStream inputStream) {
        Yaml yaml = new Yaml();
        Map<String, String> translations = yaml.load(inputStream);
        languages.put(languageCode, translations);
    }

    public static void setCurrentLanguage(String languageCode) {
        if (languages.containsKey(languageCode)) {
            currentLanguage = languageCode;
        } else {
            throw new IllegalArgumentException("Language not loaded: " + languageCode);
        }
    }

    public static String translate(String key) {
        Map<String, String> translations = languages.get(currentLanguage);
        return translations != null ? translations.getOrDefault(key.trim(), "voided") : key.trim();
    }

    public static void loadLanguagesFromPath(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (files != null) {
            for (File file : files) {
                String languageCode = file.getName().replace(".yml", "");
                try (InputStream inputStream = new FileInputStream(file)) {
                    loadLanguage(languageCode, inputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}