package fr.vengelis.afterburner.logs.internal;

import fr.vengelis.afterburner.Afterburner;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class InternalLogManager {

    private static InternalLogManager instance;

    private final String FILE_LOG_NAME;
    private final String FILE_PATH;

    public InternalLogManager() {
        instance = this;
        Date date = Calendar.getInstance().getTime();
        FILE_LOG_NAME = "log_" + date.getYear() + date.getDay() + date.getMonth() + "_" + Calendar.getInstance().toInstant().getEpochSecond() + ".log";
        FILE_PATH = Afterburner.WORKING_AREA + File.separator + "interlogs" + File.separator + FILE_LOG_NAME;
    }

    public void init() {
        Afterburner.getExporter().createFolder(Afterburner.WORKING_AREA + File.separator + "interlogs");
        try {
            (new File(FILE_PATH)).createNewFile();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /*
    * TODO : Vengelis's notes
    *       - Realy dude, improve this ...
    *       - Seriously, make the writing be done asynchronously with a system of closing the file at the end of the service execution and not at the end of the method
    * */
    public void save(String line) {
        try (FileWriter fw = new FileWriter(FILE_PATH, true);
             BufferedWriter bw = new BufferedWriter(fw)) {
            bw.write(line);
            bw.newLine();
        } catch (IOException e) {
            // Deliberately not using ConsoleLogger's printStacktrace since it will log internally
            e.printStackTrace();
        }
    }

    public static InternalLogManager get() {
        return instance;
    }
}
