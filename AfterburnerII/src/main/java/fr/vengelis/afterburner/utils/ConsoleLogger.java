/**
 * Created by Vengelis_.
 * Date: 10/17/2022
 * Time: 2:16 AM
 * Project: Lunatrix
 */

package fr.vengelis.afterburner.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.logging.Level;

import static java.util.logging.Level.*;

public class ConsoleLogger {

    private static Integer barLengh = 100;

    private static String getLevelString(Level level) {
        if (level.equals(OFF)) {
            return "MONITOR ";
        } else if (level.equals(SEVERE)) {
            return " ERROR  ";
        } else if (level.equals(WARNING)) {
            return "WARNING ";
        } else if (level.equals(INFO)) {
            return "  INFO  ";
        } else if (level.equals(CONFIG)) {
            return " CONFIG ";
        } else if (level.equals(FINE)) {
            return "LOG FINE";
        } else if (level.equals(FINER)) {
            return "  LOG   ";
        } else if (level.equals(ALL)) {
            return " NOLOG  ";
        }
        return "        ";
    }

    public static String getLog(Level level, String message) {
        return getCurrentTime(level) + message;
    }

    public static void printSeparator(Level level) {
        StringBuilder spaceBar = new StringBuilder("-");
        for(int i = 0; i <= barLengh-1; i++) {
            spaceBar.append("-");
        }
        System.out.println(getCurrentTime(level) + "#" + spaceBar +"#");
    }

    public static void printLine(Level level, String message) {
        if(!level.equals(Level.FINEST)) System.out.println(getCurrentTime(level) + message);
    }

    public static void printLines(Level level, String[] messages) {
        for(String message : messages) {
            System.out.println(getCurrentTime(level) + message);
        }
    }

    public static void printLineBox(Level level, String message) {
        StringBuilder spaceBar = new StringBuilder("=");
        StringBuilder spaceBarSpace = new StringBuilder(" ");
        for(int i = 0; i <= barLengh-1; i++) {
            spaceBar.append("=");
            spaceBarSpace.append(" ");
        }
        System.out.println(getCurrentTime(level) + "#" + spaceBar +"#");
        if(message.trim().toCharArray().clone().length < barLengh - 4) {
            StringBuilder spacePhrase = new StringBuilder();
            for(int i = 0; i < (barLengh  - message.toCharArray().clone().length) / 2; i++) {
                spacePhrase.append(" ");
            }
            System.out.println(getCurrentTime(level) + "| " + spacePhrase + message + spacePhrase + "|");
        } else {
            ArrayList<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            int i = 0;
            for(char c: message.trim().toCharArray().clone()) {
                i++;
                line.append(c);
                if(i == barLengh - 4) {
                    i = 0;
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
            }
            for(String linePhrase: lines) {
                StringBuilder spacePhrase = new StringBuilder();
                for(int j = 0; j < (barLengh - linePhrase.toCharArray().clone().length) / 2; j++) {
                    spacePhrase.append(" ");
                }
                System.out.println(getCurrentTime(level) + "| " + spacePhrase + linePhrase + spacePhrase + "|");
            }
        }
        System.out.println(getCurrentTime(level) + "#" + spaceBar +"#");
    }

    public static void printLinesBox(Level level, String[] message) {
        StringBuilder spaceBar = new StringBuilder("=");
        StringBuilder spaceBarSpace = new StringBuilder(" ");
        for(int i = 0; i <= barLengh-1; i++) {
            spaceBar.append("=");
            spaceBarSpace.append(" ");
        }
        System.out.println(getCurrentTime(level) + "#" + spaceBar +"#");

        for(String mess : message) {
            if(mess.trim().toCharArray().clone().length < barLengh - 4) {
                StringBuilder spacePhrase = new StringBuilder();
                for(int i = 0; i < (barLengh  - mess.toCharArray().clone().length) / 2; i++) {
                    spacePhrase.append(" ");
                }
                System.out.println(getCurrentTime(level) + "| " + spacePhrase + mess + spacePhrase + "|");
            } else {
                ArrayList<String> lines = new ArrayList<>();
                StringBuilder line = new StringBuilder();
                int i = 0;
                for(char c: mess.trim().toCharArray().clone()) {
                    i++;
                    line.append(c);
                    if(i == barLengh - 4) {
                        i = 0;
                        lines.add(line.toString());
                        line = new StringBuilder();
                    }
                }
                for(String linePhrase: lines) {
                    StringBuilder spacePhrase = new StringBuilder();
                    for(int j = 0; j < (barLengh - linePhrase.toCharArray().clone().length) / 2; j++) {
                        spacePhrase.append(" ");
                    }
                    System.out.println(getCurrentTime(level) + "| " + spacePhrase + linePhrase + spacePhrase + "|");
                }
            }
        }
        System.out.println(getCurrentTime(level) + "#" + spaceBar +"#");
    }


    public static void printStacktrace(Throwable throwable) {
        printStacktrace(throwable, new String[0]);
    }

    public static void printStacktrace(Throwable throwable, String... messages) {
        StringBuilder spaceBar = new StringBuilder("=");
        StringBuilder spaceBarSpace = new StringBuilder(" ");
        final String prefix = getCurrentTime(SEVERE);
        final String prefixt = prefix + "\t";
        for(int i = 0; i <= barLengh-1; i++) {
            spaceBar.append("=");
            spaceBarSpace.append(" ");
        }
        System.err.println(prefix + "#" + spaceBar +"#");
        System.err.println(prefixt);
        System.err.println(prefixt + "An error was occurred ! Printing stacktrace : ");
        System.err.println(prefixt);
        System.err.println(prefixt + "Abnormal behavior must be reported in order to be corrected. If the error that occurred");
        System.err.println(prefixt + "comes from a poor configuration of Afterburner, we invite you to look at your settings");
        System.err.println(prefixt + "before opening an issue.");
        if(messages.length > 0) {
            System.err.println(prefixt);
            System.err.println(prefixt + "Informations : ");
            for(String message : messages) {
                System.err.println(prefixt + " * " + message);
            }
        }
        System.err.println(prefixt);
        complementBoxStacktrace(throwable, prefixt);
        System.err.println(prefixt);
        System.err.println(prefix + "#" + spaceBar +"#");
    }

    private static void complementBoxStacktrace(Throwable throwable, String prefix) {
        System.err.println(prefix + throwable);
        StackTraceElement[] enclosingTrace = Thread.currentThread().getStackTrace();
        StackTraceElement[] trace = throwable.getStackTrace();
        int m = trace.length - 1, n = enclosingTrace.length - 1;
        while (m >= 0 && n >=0 && trace[m].equals(enclosingTrace[n])) {
            m--; n--;
        }
        int framesInCommon = trace.length - 1 - m;

        for (int i = 0; i <= m; i++) {
            System.err.println(prefix + "\tat " + trace[i]);
        }
        if (framesInCommon != 0) {
            System.err.println(prefix + "\t... " + framesInCommon + " more");
        }

        for (Throwable se : throwable.getSuppressed()) {
            System.err.println("Suppressed: ");
            complementBoxStacktrace(se, prefix);
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            System.err.println("Caused by: ");
            complementBoxStacktrace(cause, prefix);
        }
    }

    private static String getCurrentTime(Level level) {
        LocalTime time = LocalTime.now();
        return "[STDOUT " + time.getHour() + ":" + time.getMinute() + ":" + time.getSecond() + "] " + "|" + ConsoleLogger.getLevelString(level) +  "| > ";
    }
}
