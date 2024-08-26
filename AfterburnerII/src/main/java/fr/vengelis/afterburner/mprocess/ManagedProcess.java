package fr.vengelis.afterburner.mprocess;

import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.configurations.ConfigTemplate;
import fr.vengelis.afterburner.events.impl.ExecutableEvent;
import fr.vengelis.afterburner.events.impl.PrintedLogEvent;
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
import java.util.logging.Level;

public class ManagedProcess {

    private Process process;
    private final UUID uniqueId;
    private final IArgWrapper wrapper;

    public ManagedProcess(UUID uniqueId) {
        this.uniqueId = uniqueId;
        Optional<BaseArgumentWrapper> gwrap = AfterburnerApp.get().getArgumentWrapperManager().get(ConfigTemplate.EXECUTABLE_TYPE.getData().toString());
        if(!gwrap.isPresent()) {
            wrapper = new JavaArguments();
            ConsoleLogger.printLine(Level.SEVERE, "Unrecognized managed program type ! Applying default : " + wrapper.getType());
        } else {
            wrapper = gwrap.get();
        }
    }

    public boolean execute() {
        StringBuilder stb = new StringBuilder();
        stb.append(wrapper.getBaseLauncher()).append(" ")
                .append(wrapper.getFinalMinimalRam()).append(" ")
                .append(wrapper.getFinalMaximumRam()).append(" ");
        for (String s : ((List<String>) ConfigTemplate.EXECUTABLE_MORE_ARGS.getData())) {
            stb.append(" " + s);
        }
        stb.append(wrapper.getFinalExecutable())
                .append(" DafterbunerUuid=" + uniqueId)
                .append(" Djobid=" + AfterburnerApp.get().getProviderManager().getResultInstruction(ProviderInstructions.JOB_ID).toString().replace("\"", ""))
                .append(" DserverOwner=" + AfterburnerApp.get().getProviderManager().getResultInstruction(ProviderInstructions.PLAYER_REQUESTER).toString().replace("\"", ""));

        ExecutableEvent event = new ExecutableEvent(stb);
        AfterburnerApp.get().getEventManager().call(event);

        ConsoleLogger.printLine(Level.INFO, "Final built java command : " + event.getCmdline());

        try {
            ConsoleLogger.printLine(Level.INFO, "Forcing the program '" + ConfigTemplate.EXECUTABLE_NAME.getData() + "' into execution mode.");
            File execFile = new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData() + "/" + ConfigTemplate.EXECUTABLE_NAME.getData());
            if(execFile.setExecutable(true)) ConsoleLogger.printLine(Level.INFO, "Forcing success !");
            else ConsoleLogger.printLine(Level.SEVERE, "The program could not have execution rights due to insufficient permission to Afterburner");
        } catch (SecurityException e) {
            ConsoleLogger.printStacktrace(e, "Forcing failed !");
        }

        try {
            process = new ProcessBuilder()
                    .command(Arrays.asList(event.getCmdline().toString().split(" ")))
                    .directory(new File(ConfigGeneral.PATH_RENDERING_DIRECTORY.getData().toString()))
                    .start();

            Signal.handle(new Signal("INT"), sig -> {
                AfterburnerApp.get().killTask("Ordered by CLI");
                System.exit(0);
            });
            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;
            int skip = 0;
            if(!AfterburnerApp.get().isDisplayOutput()) ConsoleLogger.printLine(Level.INFO, "The managed program is running. Type 'help' to list available commands");
            try {
                while ((line = br.readLine()) != null) {
                    PrintedLog log = new PrintedLog(line);
                    PrintedLogEvent event1 = new PrintedLogEvent(log, PrintedLogEvent.Handler.PROCESS);
                    AfterburnerApp.get().getEventManager().call(event);
                    if(event1.isCancelled()) {
                        log.setSkip(true).save();
                        continue;
                    }
                    for(Skipper value : AfterburnerApp.get().getLogSkipperManager().getSkipperList()) {
                        if(value.getPattern().matcher(line).find()) {
                            if(value.isCast()) ConsoleLogger.printLine(Level.INFO, "Skipper trigger : " + log.getLine() + " (" + value.getLineSkip() + " lines ignored)");
                            if(value.getAction() != null) value.getAction().accept(log.getLine());
                            skip += value.getLineSkip();
                        }
                    }
                    if(skip == 0) {
                        // TODO : Rendre compatible avec l'envoie de la data aux sockets clients
                        if(AfterburnerApp.get().isDisplayOutput()) log.print();
                        log.save();
                    }
                    else {
                        log.setSkip(true).save();
                        skip--;
                    }
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
