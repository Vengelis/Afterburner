package fr.vengelis.afterburner;

import fr.vengelis.afterburner.cli.command.AtbCommand;
import fr.vengelis.afterburner.cli.command.CommandInstruction;
import fr.vengelis.afterburner.cli.command.CommandResultReader;
import fr.vengelis.afterburner.events.impl.common.SendInstructionEvent;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Properties;
import java.util.Scanner;
import java.util.logging.Level;

public class Afterburner {

    public static String WORKING_AREA;
    public static boolean DISABLE_TEST_TEMPLATE = false;
    public static boolean VERBOSE_PROVIDERS = false;
    private static boolean DEFAULT_DISPLAY_PROGRAM_OUTPUT = true;
    private static LaunchType LAUNCH_TYPE = LaunchType.SLAVE;

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

            }
        }

        ConsoleLogger.printLine(Level.CONFIG, "Working Area : " + WORKING_AREA);

        HandlerRecorder handlerRecorder = new HandlerRecorder();
        AApp app = null;

        try {
            InetAddress addr = InetAddress.getLocalHost();
            final String MACHINE_NAME = addr.getHostName();
            if(LAUNCH_TYPE.equals(LaunchType.SLAVE))
                app = new AfterburnerSlaveApp(MACHINE_NAME, template, DEFAULT_DISPLAY_PROGRAM_OUTPUT);
            else if(LAUNCH_TYPE.equals(LaunchType.PANEL))
                app = new AfterburnerClientApp();
            else if(LAUNCH_TYPE.equals(LaunchType.BROADCASTER))
                app = new AfterburnerBroadcasterApp();
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
            System.exit(1);
        }

        if(app instanceof AfterburnerSlaveApp) {
            AfterburnerSlaveApp finalApp = (AfterburnerSlaveApp) app;

            if(template == null) {
                ConsoleLogger.printLine(Level.SEVERE, "Missing template argument !");
                System.exit(1);
            }

            new Thread(() -> {
                finalApp.exportRessources();
                handlerRecorder.executeSuperPreInit();
                finalApp.loadPluginsAndProviders();
                finalApp.loadGeneralConfigs();
                handlerRecorder.executePreInit();
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
                finalApp.getSocketServer().stop();
                if(finalApp.getRepreparedCount() > 1) {
                    ConsoleLogger.printLine(Level.INFO, "Number of times reprepared : " + finalApp.getRepreparedCount());
                }
                System.exit(0);

            }).start();

            Scanner keyboard = new Scanner(System.in);
            String input;

            while (true) {
                input = keyboard.nextLine();
                if (input != null && !input.trim().isEmpty()) {
                    CommandInstruction instruction = new CommandInstruction(
                            input,
                            input.split("\\s+"),
                            AtbCommand.CommandSide.SERVER);
                    SendInstructionEvent event = new SendInstructionEvent(instruction);
                    AfterburnerSlaveApp.get().getEventManager().call(event);
                    if(event.isCancelled())
                        ConsoleLogger.printLine(Level.INFO, "Command cancel reason : " + event.getCancelReason());
                    else
                        CommandResultReader.read(app.getCliManager().execute(event.getInstruction()));
                } else {
                    ConsoleLogger.printLine(Level.SEVERE, "No command entered. Please try again.");
                }
            }

        } else if(app instanceof AfterburnerClientApp) {
            AfterburnerClientApp finalApp = (AfterburnerClientApp) app;
            new Thread(() -> {
                finalApp.exportRessources();
                handlerRecorder.executeSuperPreInit();
                finalApp.loadPluginsAndProviders();
                finalApp.loadGeneralConfigs();
                handlerRecorder.executePreInit();
                finalApp.initialize();
                finalApp.preparing();
                finalApp.execute();
                finalApp.ending();
            }).start();
        } else if(app instanceof AfterburnerBroadcasterApp) {
            AfterburnerBroadcasterApp finalApp = (AfterburnerBroadcasterApp) app;
            new Thread(() -> {
                finalApp.exportRessources();
                handlerRecorder.executeSuperPreInit();
                finalApp.loadPluginsAndProviders();
                finalApp.loadGeneralConfigs();
                handlerRecorder.executePreInit();
                finalApp.initialize();
                finalApp.preparing();
                finalApp.execute();
                finalApp.ending();
            }).start();
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        start();
    }

    public static String getVersion() {
        Properties properties = new Properties();
        try (InputStream input = Afterburner.class.getClassLoader().getResourceAsStream("version.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find version.properties");
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
