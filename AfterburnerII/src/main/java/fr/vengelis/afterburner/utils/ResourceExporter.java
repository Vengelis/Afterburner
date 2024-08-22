package fr.vengelis.afterburner.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ResourceExporter {

    public File saveResource(File outputDirectory, String name) throws IOException {
        return saveResource(outputDirectory, name, true);
    }

    public File saveResource(File outputDirectory, String name, boolean replace)
            throws IOException {
        File out = new File(outputDirectory, name);
        if (!replace && out.exists())
            return out;
        InputStream resource = this.getClass().getResourceAsStream(name);
        if (resource == null)
            throw new FileNotFoundException(name + " (resource not found)");
        try(InputStream in = resource;
            OutputStream writer = new BufferedOutputStream(
                    Files.newOutputStream(out.toPath()))) {
            byte[] buffer = new byte[1024 * 4];
            int length;
            while((length = in.read(buffer)) >= 0) {
                writer.write(buffer, 0, length);
            }
        }
        return out;
    }

    public void createFolder(String path) {
        Path folder = Paths.get(path);
        if (!Files.exists(folder)) {
            try {
                Files.createDirectories(folder);
            } catch (IOException e) {
                ConsoleLogger.printStacktrace(e);
            }
        }
    }

}
