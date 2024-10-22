package fr.vengelis.afterburner;

import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.updater.VersionChecker;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.logging.Level;

public class Afterburner {

    public static String WORKING_AREA;
    public static boolean DISABLE_TEST_TEMPLATE = false;
    public static boolean VERBOSE = false;
    public static String TEMPLATE = null;

    private static boolean DEFAULT_DISPLAY_PROGRAM_OUTPUT = true;
    private static LaunchType LAUNCH_TYPE = LaunchType.SLAVE;
    private static boolean CLIENT_COMPLEXE = false;
    private static String STRING_CONNECT = "";

    public enum LaunchType {
        SLAVE,
        PANEL,
        BROADCASTER,
        ;
    }

    public static void start() {
        ConsoleLogger.printLine(Level.INFO, "#-------------------------------------------------------------------------------------------------------------------------#");
        ConsoleLogger.printLine(Level.INFO, "|     _       __   _                   _                                                                                  |");
        ConsoleLogger.printLine(Level.INFO, "|    / \\     / _| | |_    ___   _ __  | |__    _   _   _ __   _ __     ___   _ __                                         |");
        ConsoleLogger.printLine(Level.INFO, "|   / _ \\   | |_  | __|  / _ \\ | '__| | '_ \\  | | | | | '__| | '_ \\   / _ \\ | '__|                                        |");
        ConsoleLogger.printLine(Level.INFO, "|  / ___ \\  |  _| | |_  |  __/ | |    | |_) | | |_| | | |    | | | | |  __/ | |                                           |");
        ConsoleLogger.printLine(Level.INFO, "| /_/   \\_\\ |_|    \\__|  \\___| |_|    |_.__/   \\__,_| |_|    |_| |_|  \\___| |_|                                           |");
        ConsoleLogger.printLine(Level.INFO, "|                                                                                                                         |");
        ConsoleLogger.printLine(Level.INFO, "|                                                                                    By Vengelis_  - " + getVersion());
        ConsoleLogger.printLine(Level.INFO, "#-------------------------------------------------------------------------------------------------------------------------#");

        String startupCommand = System.getProperty("sun.java.command");
        String[] stArgs = startupCommand.split(" ");

        ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
        logger.setLevel(ch.qos.logback.classic.Level.ERROR);

        VersionChecker.check();

        try {
            WORKING_AREA = new File(Afterburner.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        } catch (URISyntaxException e) {
            ConsoleLogger.printStacktrace(e);
            System.exit(1);
        }
        for (String arg : stArgs) {
            if (arg.startsWith("DbaseDirectory=")) {
                WORKING_AREA = arg.substring("DbaseDirectory=".length()).replace("\"", "").replace("<space>", " ");
            } else if (arg.startsWith("Dtemplate=")) {
                TEMPLATE = arg.substring("Dtemplate=".length()).replace("\"", "").replace("<space>", " ");
            } else if (arg.startsWith("DtestTemplateDisabled=")) {
                DISABLE_TEST_TEMPLATE = Boolean.parseBoolean(arg.substring("DtestTemplateDisabled=".length()).replace("\"", ""));
                if(DISABLE_TEST_TEMPLATE) {
                    ConsoleLogger.printLine(Level.CONFIG, "Example template ('templates/example.yml') was disabled");
                }
            } else if (arg.startsWith("Dverbose=")) {
                VERBOSE = Boolean.parseBoolean(arg.substring("Dverbose=".length()).replace("\"", ""));
                ConsoleLogger.printVerbose(Level.CONFIG, "Verbose enabled");
            } else if (arg.startsWith("--no-default-output") || arg.startsWith("-ndo")) {
                DEFAULT_DISPLAY_PROGRAM_OUTPUT = false;
                ConsoleLogger.printLine(Level.CONFIG, "The managed program will not display the log by default");
            } else if (arg.startsWith("--type-launch") || arg.startsWith("-tl")) {
                String type = "SLAVE";
                if(arg.startsWith("--type-launch"))
                    type = arg.substring("--type-launch:".length()).toUpperCase();
                if(arg.startsWith("-tl"))
                    type = arg.substring("-tl:".length()).toUpperCase();
                try {
                    LAUNCH_TYPE = LaunchType.valueOf(type);
                    ConsoleLogger.printLine(Level.CONFIG, "Afterburner instance type '" + LAUNCH_TYPE.name() + "'");
                } catch (IllegalArgumentException e) {
                    ConsoleLogger.printStacktrace(e, "Unrecognized launcher type !");
                    System.exit(1);
                }
            } else if(arg.startsWith("--credentials") || arg.startsWith("-c")) {
                String[] c = arg.split(":");
                CLIENT_COMPLEXE = true;
                try {
                    STRING_CONNECT = c[1] + ":" + c[2] + ":" + c[3];
                } catch (Exception e) {
                    ConsoleLogger.printStacktrace(e, "Malformed client credentials informations !");
                    System.exit(1);
                }
            }
        }

        ConsoleLogger.printLine(Level.CONFIG, "Working Area : " + WORKING_AREA);

        HandlerRecorder handlerRecorder = new HandlerRecorder();
        AApp app = null;

        try {
            InetAddress addr = InetAddress.getLocalHost();
            final String MACHINE_NAME = addr.getHostName();
            if(LAUNCH_TYPE.equals(LaunchType.SLAVE))
                app = new AfterburnerSlaveApp(MACHINE_NAME, TEMPLATE, DEFAULT_DISPLAY_PROGRAM_OUTPUT);
            else if(LAUNCH_TYPE.equals(LaunchType.PANEL))
                if(!CLIENT_COMPLEXE)
                    app = new AfterburnerClientApp();
                else {
                    String[] c = STRING_CONNECT.split(":");
                    app = new AfterburnerClientApp(c[0], Integer.parseInt(c[1]), c[2]);
                }
            else if(LAUNCH_TYPE.equals(LaunchType.BROADCASTER))
                app = new AfterburnerBroadcasterApp();
        } catch (UnknownHostException ex) {
            ConsoleLogger.printStacktrace(ex, "Hostname can not be resolved");
            System.exit(1);
        }

        app.boot(handlerRecorder);

    }

    public static void main(String[] args) {
        start();
    }

    public static String getVersion() {
        Properties properties = new Properties();
        try (InputStream input = Afterburner.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                ConsoleLogger.printLine(Level.SEVERE,"Sorry, unable to find version.properties");
                return null;
            }
            properties.load(input);
            return properties.getProperty("version");
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
            return null;
        }
    }

    public static LaunchType getLaunchType() {
        return LAUNCH_TYPE;
    }
}
