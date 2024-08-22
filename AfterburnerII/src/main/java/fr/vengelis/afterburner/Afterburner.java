package fr.vengelis.afterburner;

import fr.vengelis.afterburner.utils.ConsoleLogger;
import sun.misc.Signal;

import java.io.File;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;

public class Afterburner {

    public static String WORKING_AREA;
    public static boolean DISABLE_TEST_TEMPLATE = false;
    public static boolean VERBOSE_PROVIDERS = false;
    private static boolean DEFAULT_DISPLAY_PROGRAM_OUTPUT = true;

    public static void main(String[] args) {
        ConsoleLogger.printLine(Level.INFO, "#-------------------------------------------------------------------------------------------------------------------------#");
        ConsoleLogger.printLine(Level.INFO, "|     _       __   _                   _                                                                                  |");
        ConsoleLogger.printLine(Level.INFO, "|    / \\     / _| | |_    ___   _ __  | |__    _   _   _ __   _ __     ___   _ __                                         |");
        ConsoleLogger.printLine(Level.INFO, "|   / _ \\   | |_  | __|  / _ \\ | '__| | '_ \\  | | | | | '__| | '_ \\   / _ \\ | '__|                                        |");
        ConsoleLogger.printLine(Level.INFO, "|  / ___ \\  |  _| | |_  |  __/ | |    | |_) | | |_| | | |    | | | | |  __/ | |                                           |");
        ConsoleLogger.printLine(Level.INFO, "| /_/   \\_\\ |_|    \\__|  \\___| |_|    |_.__/   \\__,_| |_|    |_| |_|  \\___| |_|                                           |");
        ConsoleLogger.printLine(Level.INFO, "|                                                                                                                         |");
        ConsoleLogger.printLine(Level.INFO, "|                                                                                          By Vengelis_  - v4.3.0         |");
        ConsoleLogger.printLine(Level.INFO, "#-------------------------------------------------------------------------------------------------------------------------#");

        String startupCommand = System.getProperty("sun.java.command");
        String[] stArgs = startupCommand.split(" ");

        try {
            WORKING_AREA = new File(Afterburner.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException e) {
            ConsoleLogger.printStacktrace(e);
            System.exit(1);
        }
        String template = null;
        for (String arg : stArgs) {
            if (arg.startsWith("DbaseDirectory=")) {
                WORKING_AREA = arg.substring("DbaseDirectory=".length()).replace("\"", "").replace("<space>", " ");
            } else if (arg.startsWith("Dtemplate=")) {
                template = arg.substring("Dtemplate=".length()).replace("\"", "").replace("<space>", " ");
            } else if (arg.startsWith("DtestTemplateDisabled=")) {
                DISABLE_TEST_TEMPLATE = Boolean.parseBoolean(arg.substring("DtestTemplateDisabled=".length()).replace("\"", ""));
                if(DISABLE_TEST_TEMPLATE) {
                    ConsoleLogger.printLine(Level.CONFIG, "Example template ('templates/example.yml') was disabled");
                }
            } else if (arg.startsWith("DverboseProviders=")) {
                VERBOSE_PROVIDERS = Boolean.parseBoolean(arg.substring("DverboseProviders=".length()).replace("\"", ""));
                if(VERBOSE_PROVIDERS) {
                    ConsoleLogger.printLine(Level.CONFIG, "Verbose provider results");
                }
            } else if (arg.startsWith("--no-default-output") || arg.startsWith("-ndo")) {
                DEFAULT_DISPLAY_PROGRAM_OUTPUT = false;
                ConsoleLogger.printLine(Level.CONFIG, "The managed program will not display the log by default");
            }
        }

        ConsoleLogger.printLine(Level.CONFIG, "Working Area : " + WORKING_AREA);

        if(template == null) {
            ConsoleLogger.printLine(Level.SEVERE, "Missing template argument !");
            System.exit(1);
        }

        AfterburnerApp app = null;

        try {
            InetAddress addr = InetAddress.getLocalHost();
            final String MACHINE_NAME = addr.getHostName();
            app = new AfterburnerApp(MACHINE_NAME, template, DEFAULT_DISPLAY_PROGRAM_OUTPUT);
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
            System.exit(1);
        }

        AfterburnerApp finalApp = app;
        new Thread(() -> {
            finalApp.exportRessources();
            finalApp.loadPluginsAndProviders();
            finalApp.loadConfigs();
            finalApp.initialize();
            finalApp.setReprepareEnabled(true);
            while (finalApp.isReprepareEnabled()) {
                finalApp.setReprepareEnabled(false);
                finalApp.setRepreparedCount(finalApp.getRepreparedCount() + 1);
                finalApp.preparing();
                finalApp.execute();
                finalApp.ending();
            }
            finalApp.getRunnableManager().shutdown();
            if(finalApp.getRepreparedCount() > 1) {
                ConsoleLogger.printLine(Level.INFO, "Number of times reprepared : " + finalApp.getRepreparedCount());
            }
            System.exit(0);

        }).start();

        Scanner keyboard = new Scanner(System.in);
        String input;

        while(true) {
            input = keyboard.nextLine();
            if(input != null && !input.trim().isEmpty()) {
                app.getCliManager().getRootCommand().execute(input.split("\\s+"));
            } else {
                ConsoleLogger.printLine(Level.SEVERE, "No command entered. Please try again.");
            }
        }
    }
}
