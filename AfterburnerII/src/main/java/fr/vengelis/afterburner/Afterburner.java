package fr.vengelis.afterburner;

import fr.vengelis.afterburner.cli.CliManager;
import fr.vengelis.afterburner.commonfiles.impl.McPlugins;
import fr.vengelis.afterburner.utils.ConsoleLogger;

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

    public static void main(String[] args) {
        ConsoleLogger.printLine(Level.INFO, "#-------------------------------------------------------------------------------------------------------------------------#");
        ConsoleLogger.printLine(Level.INFO, "|     _       __   _                   _                                                                                  |");
        ConsoleLogger.printLine(Level.INFO, "|    / \\     / _| | |_    ___   _ __  | |__    _   _   _ __   _ __     ___   _ __                                         |");
        ConsoleLogger.printLine(Level.INFO, "|   / _ \\   | |_  | __|  / _ \\ | '__| | '_ \\  | | | | | '__| | '_ \\   / _ \\ | '__|                                        |");
        ConsoleLogger.printLine(Level.INFO, "|  / ___ \\  |  _| | |_  |  __/ | |    | |_) | | |_| | | |    | | | | |  __/ | |                                           |");
        ConsoleLogger.printLine(Level.INFO, "| /_/   \\_\\ |_|    \\__|  \\___| |_|    |_.__/   \\__,_| |_|    |_| |_|  \\___| |_|                                           |");
        ConsoleLogger.printLine(Level.INFO, "|                                                                                                                         |");
        ConsoleLogger.printLine(Level.INFO, "|                                                                                          By Vengelis_  - v4.2.5         |");
        ConsoleLogger.printLine(Level.INFO, "#-------------------------------------------------------------------------------------------------------------------------#");


        CliManager cliManager = new CliManager();
        cliManager.init();

        System.out.println("--- System READY ---");
        Scanner keyboard = new Scanner(System.in);
        String input;
        while(true) {
            input = keyboard.nextLine();
            if(input != null && !input.trim().isEmpty()) {
                cliManager.getRootCommand().execute(input.split("\\s+"));
            } else {
                ConsoleLogger.printLine(Level.SEVERE, "No command entered. Please try again.");
            }
        }

//        String startupCommand = System.getProperty("sun.java.command");
//        String[] stArgs = startupCommand.split(" ");
//
//        try {
//            WORKING_AREA = new File(Afterburner.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
//        } catch (URISyntaxException e) {
//            ConsoleLogger.printStacktrace(e);
//            System.exit(1);
//        }
//        String template = null;
//        for (String arg : stArgs) {
//            if (arg.startsWith("DbaseDirectory=")) {
//                WORKING_AREA = arg.substring("DbaseDirectory=".length()).replace("\"", "").replace("<space>", " ");
//            } else if (arg.startsWith("Dtemplate=")) {
//                template = arg.substring("Dtemplate=".length()).replace("\"", "").replace("<space>", " ");
//            } else if (arg.startsWith("DtestTemplateDisabled=")) {
//                DISABLE_TEST_TEMPLATE = Boolean.parseBoolean(arg.substring("DtestTemplateDisabled=".length()).replace("\"", ""));
//                if(DISABLE_TEST_TEMPLATE) {
//                    ConsoleLogger.printLine(Level.CONFIG, "Example template ('templates/example.yml') was disabled");
//                }
//            } else if (arg.startsWith("DverboseProviders=")) {
//                VERBOSE_PROVIDERS = Boolean.parseBoolean(arg.substring("DverboseProviders=".length()).replace("\"", ""));
//                if(VERBOSE_PROVIDERS) {
//                    ConsoleLogger.printLine(Level.CONFIG, "Verbose provider results");
//                }
//            }
//        }
//
//        ConsoleLogger.printLine(Level.CONFIG, "Working Area : " + WORKING_AREA);
//
//        if(template == null) {
//            ConsoleLogger.printLine(Level.SEVERE, "Missing template argument !");
//            System.exit(1);
//        }
//
//        try {
//            InetAddress addr = InetAddress.getLocalHost();
//            final String MACHINE_NAME = addr.getHostName();
//            AfterburnerApp app = new AfterburnerApp(MACHINE_NAME, template);
//
//            app.exportRessources();
//            app.loadPluginsAndProviders();
//            app.loadConfigs();
//            app.initialize();
//            app.setReprepareEnabled(true);
//            while (app.isReprepareEnabled()) {
//                app.setReprepareEnabled(false);
//                app.setRepreparedCount(app.getRepreparedCount() + 1);
//                app.preparing();
//                app.execute();
//                app.ending();
//            }
//            app.getRunnableManager().shutdown();
//            if(app.getRepreparedCount() > 1) {
//                ConsoleLogger.printLine(Level.INFO, "Number of times reprepared : " + app.getRepreparedCount());
//            }
//            System.exit(0);
//        } catch (UnknownHostException ex) {
//            System.out.println("Hostname can not be resolved");
//            System.exit(1);
//        }

    }

}
