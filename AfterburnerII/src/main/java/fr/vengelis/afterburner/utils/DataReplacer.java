package fr.vengelis.afterburner.utils;

import java.io.*;

public class DataReplacer {

    public static void replace(String oldText, String newText, String filePath) {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        StringBuilder inputBuffer = new StringBuilder();
        String line;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            while ((line = reader.readLine()) != null) {
                inputBuffer.append(line.replace(oldText, newText));
                inputBuffer.append('\n');
            }
            reader.close();
            writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(inputBuffer.toString());
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
        } finally {
            try {
                if (reader != null) reader.close();
                if (writer != null) writer.close();
            } catch (IOException e) {
                ConsoleLogger.printStacktrace(e);
            }
        }
    }
}
