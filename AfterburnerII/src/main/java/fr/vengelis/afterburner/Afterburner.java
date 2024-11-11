package fr.vengelis.afterburner;

import fr.vengelis.afterburner.arguments.ArgumentManager;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.language.LanguageManager;
import fr.vengelis.afterburner.logs.internal.InternalLogManager;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import fr.vengelis.afterburner.utils.ResourceExporter;
import fr.vengelis.afterburner.utils.updater.VersionChecker;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.function.Supplier;
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
    private static String MACHINE_NAME = "";
    private static final ResourceExporter exporter = new ResourceExporter();

    public enum LaunchType {
        SLAVE(() -> new AfterburnerSlaveApp(MACHINE_NAME, TEMPLATE, DEFAULT_DISPLAY_PROGRAM_OUTPUT)),
        PANEL(() -> CLIENT_COMPLEXE ? new AfterburnerClientApp(STRING_CONNECT.split(":")[0], Integer.parseInt(STRING_CONNECT.split(":")[1]), STRING_CONNECT.split(":")[2]) : new AfterburnerClientApp()),
        BROADCASTER(AfterburnerBroadcasterApp::new);

        private final Supplier<AApp> appSupplier;

        LaunchType(Supplier<AApp> appSupplier) {
            this.appSupplier = appSupplier;
        }

        public AApp createApp() {
            return appSupplier.get();
        }
    }

    public static void start() {

        try {
            WORKING_AREA = new File(Afterburner.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
            System.out.println("Working Area : " + WORKING_AREA);
        } catch (URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }

        final InternalLogManager ILM = new InternalLogManager();
        ILM.init();

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

        ArgumentManager argumentManager = new ArgumentManager();

        exporter.createFolder(Afterburner.WORKING_AREA + File.separator + "languages");
        try {
            exporter.saveResource(new File(Afterburner.WORKING_AREA), "/languages/fr_FR.yml", false);
            exporter.saveResource(new File(Afterburner.WORKING_AREA), "/languages/en_US.yml", false);
        } catch (Exception e) {
            ConsoleLogger.printStacktrace(e);
            System.exit(1);
        }


        LanguageManager.loadLanguagesFromPath(Afterburner.WORKING_AREA + File.separator + "languages");

        argumentManager.addArgument("Dlanguage", null, "-l", (arg, value) -> {
            LanguageManager.setCurrentLanguage(value);
        });
        argumentManager.addArgument("Dtemplate", null, null, (arg, value) -> {
            TEMPLATE = value.replace("\"", "").replace("<space>", " ");
        });
        argumentManager.addArgument("DtestTemplateDisabled", null, null, (arg, value) -> {
            DISABLE_TEST_TEMPLATE = Boolean.parseBoolean(value.replace("\"", ""));
            if (DISABLE_TEST_TEMPLATE) {
                ConsoleLogger.printLine(Level.CONFIG, "Example template ('templates/example.yml') was disabled");
            }
        });
        argumentManager.addArgument("Dverbose", null, null, (arg, value) -> {
            VERBOSE = Boolean.parseBoolean(value.replace("\"", ""));
            ConsoleLogger.printVerbose(Level.CONFIG, "Verbose enabled");
        });
        argumentManager.addArgument("--no-default-output", null, "-ndo", (arg, value) -> {
            DEFAULT_DISPLAY_PROGRAM_OUTPUT = false;
            ConsoleLogger.printLine(Level.CONFIG, "The managed program will not display the log by default");
        });
        argumentManager.addArgument("--type-launch", null, "-tl", (arg, value) -> {
            try {
                LAUNCH_TYPE = LaunchType.valueOf(value.toUpperCase());
                ConsoleLogger.printLine(Level.CONFIG, "Afterburner instance type '" + LAUNCH_TYPE.name() + "'");
            } catch (IllegalArgumentException e) {
                ConsoleLogger.printStacktrace(e, "Unrecognized launcher type !");
                System.exit(1);
            }
        });
        argumentManager.addArgument("--credentials", null, "-c", (arg, value) -> {
            String[] c = value.split(":");
            CLIENT_COMPLEXE = true;
            try {
                STRING_CONNECT = c[0] + ":" + c[1] + ":" + c[2];
            } catch (Exception e) {
                ConsoleLogger.printStacktrace(e, "Malformed client credentials informations !");
                System.exit(1);
            }
        });

        argumentManager.parseArguments(stArgs);

        HandlerRecorder handlerRecorder = new HandlerRecorder();
        AApp app = null;

        try {
            InetAddress addr = InetAddress.getLocalHost();
            MACHINE_NAME = addr.getHostName();
            app = LAUNCH_TYPE.createApp();
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

    public static ResourceExporter getExporter() {
        return exporter;
    }

    public static LaunchType getLaunchType() {
        return LAUNCH_TYPE;
    }
}
