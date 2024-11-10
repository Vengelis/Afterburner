package fr.vengelis.afterburner.mprocess;

import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.configurations.ConfigTemplate;
import fr.vengelis.afterburner.events.impl.common.ExecutableEvent;
import fr.vengelis.afterburner.events.impl.common.PrintedLogEvent;
import fr.vengelis.afterburner.language.LanguageManager;
import fr.vengelis.afterburner.logs.PrintedLog;
import fr.vengelis.afterburner.logs.Skipper;
import fr.vengelis.afterburner.mprocess.argwrapper.BaseArgumentWrapper;
import fr.vengelis.afterburner.mprocess.argwrapper.IArgWrapper;
import fr.vengelis.afterburner.mprocess.argwrapper.impl.JavaArguments;
import fr.vengelis.afterburner.providers.ProviderInstructions;
import fr.vengelis.afterburner.utils.ConsoleLogger;
import sun.misc.Signal;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ManagedProcess {

    private Process process;
    private final UUID uniqueId;
    private final IArgWrapper wrapper;

    public ManagedProcess(UUID uniqueId) {
        this.uniqueId = uniqueId;
        Optional<BaseArgumentWrapper> gwrap = AfterburnerSlaveApp.get().getArgumentWrapperManager().get(ConfigTemplate.EXECUTABLE_TYPE.getData().toString().toLowerCase());
        if(!gwrap.isPresent()) {
            wrapper = new JavaArguments();
            ConsoleLogger.printLine(Level.SEVERE, String.format(LanguageManager.translate("error-unrecognized-program-type"),wrapper.getType()));
        } else {
            wrapper = gwrap.get();
        }
    }

    public boolean execute() {
        StringBuilder stb = new StringBuilder();
        stb.append(wrapper.getBaseLauncher()).append(" ")
                .append(wrapper.getFinalMinimalRam()).append(" ")
                .append(wrapper.getFinalMaximumRam()).append(" ");
        for (String s : ((List<String>) ConfigTemplate.EXECUTABLE_MORE_ARGS_ENGINE.getData())) {
            stb.append(s + " ");
        }
        String exe;
        if(!System.getProperty("os.name").toLowerCase().startsWith("windows"))
            exe = wrapper.getFinalExecutable().replace("\"", "");
        else
            exe = wrapper.getFinalExecutable();
        stb.append(exe)
                .append(" DafterbunerUuid=" + uniqueId)
                .append(" Djobid=" + AfterburnerSlaveApp.get().getProviderManager().getResultInstruction(ProviderInstructions.JOB_ID).toString().replace("\"", ""))
                .append(" DserverOwner=" + AfterburnerSlaveApp.get().getProviderManager().getResultInstruction(ProviderInstructions.PLAYER_REQUESTER).toString().replace("\"", ""));

        for (String s : ((List<String>) ConfigTemplate.EXECUTABLE_MORE_ARGS_APPLICATION.getData())) {
            stb.append(" " + s);
        }

        ExecutableEvent event = new ExecutableEvent(wrapper, stb, ExecutableEvent.Stage.PRE);
        AfterburnerSlaveApp.get().getEventManager().call(event);

        ConsoleLogger.printLine(Level.INFO, "Final built java command : " + event.getCmdline());

        try {
            ConsoleLogger.printLine(Level.INFO, String.format(LanguageManager.translate("atb-forcing-mp"),ConfigTemplate.EXECUTABLE_NAME.getData()));
            File execFile = new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData() + "/" + ConfigTemplate.EXECUTABLE_NAME.getData());
            if(execFile.setExecutable(true)) ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("atb-forcing-success"));
            else ConsoleLogger.printLine(Level.SEVERE, LanguageManager.translate("error-missing-permission"));
        } catch (SecurityException e) {
            ConsoleLogger.printStacktrace(e, LanguageManager.translate("error-forcing-failed"));
        }

        try {
            process = new ProcessBuilder()
                    .command(Arrays.asList(event.getCmdline().toString().split(" ")))
                    .directory(new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString()))
                    .start();

            Signal.handle(new Signal("INT"), sig -> {
                AfterburnerSlaveApp.get().killTask("Ordered by CLI");
                System.exit(0);
            });
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            if(!process.isAlive() || br.readLine() == null) {
                ConsoleLogger.printLinesBox(Level.WARNING, new String[]{
                        "The server did not start correctly. Java simply could not start it for the following reasons :",
                        "  ",
                        "- The command line is not correct.",
                        "- The language used to start the application is not defined.",
                        "- The path where the server program is located contains spaces and therefore it can be interpreted as an additional argument to the command.",
                        "- The path contains quotes (with java for example this is not supported for the -jar argument). ",
                        "- Afterburner simply does not have the execution right despite the attempt to escalate privileges.",
                        "- The server file used to start the application is simply not present (check the name in the template config file).",
                        "  ",
                        "Before reporting an issue on github, check your settings carefully. "
                });
            }

            int skip = 0;
            if(!AfterburnerSlaveApp.get().isDisplayOutput()) ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("atb-managed-program-running"));
            try {
                while ((line = br.readLine()) != null) {
                    PrintedLog log = new PrintedLog(line);
                    PrintedLogEvent event1 = new PrintedLogEvent(log, PrintedLogEvent.Handler.PROCESS);
                    AfterburnerSlaveApp.get().getEventManager().call(event1);
                    if(event1.isCancelled()) {
                        log.setSkip(true).save();
                        continue;
                    }
                    for(Skipper value : AfterburnerSlaveApp.get().getLogSkipperManager().getSkipperList()) {
                        if(value.getPattern().matcher(line).find()) {
                            if(value.isCast()) ConsoleLogger.printLine(Level.INFO, "Skipper trigger : " + log.getLine() + " (" + value.getLineSkip() + " lines ignored)");
                            if(value.getAction() != null) value.getAction().accept(log.getLine());
                            skip += value.getLineSkip();
                        }
                    }
                    if(skip == 0) {
                        if(AfterburnerSlaveApp.get().isDisplayOutput()) log.print();
                        log.save();
                    } else {
                        log.setSkip(true).save();
                        skip--;
                    }
                    AfterburnerSlaveApp.get().getSocketServer().sendAllClient("Server:EchoLog:SkipEmbarked:" + log.serialize());
                }
            } catch (IOException e) {
                ConsoleLogger.printLine(Level.SEVERE, "Managed program was suddenly closed");
            }
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
            return false;
        }

        return true;

    }

    public Optional<Process> getProcess() {
        return Optional.ofNullable(process);
    }

    public boolean sendCommandToProcess(String command) {
        if(process != null) {
            if (process.isAlive()) {
                OutputStream outputStream = process.getOutputStream();
                PrintWriter writer = new PrintWriter(outputStream, true);
                writer.println(command);
                writer.flush();
                return true;
            } else {
                ConsoleLogger.printLine(Level.INFO, "The process is not running.");
            }
        } else {
            ConsoleLogger.printLine(Level.INFO, "The process is not running.");
        }
        return false;
    }

}
