package fr.vengelis.afterburner.interconnection.socket.system;

import fr.vengelis.afterburner.AfterburnerClientApp;
import fr.vengelis.afterburner.cli.command.AtbCommand;
import fr.vengelis.afterburner.cli.command.CommandInstruction;
import fr.vengelis.afterburner.cli.command.CommandResult;
import fr.vengelis.afterburner.cli.command.CommandResultReader;
import fr.vengelis.afterburner.events.impl.client.CommandReceiveResultEvent;
import fr.vengelis.afterburner.events.impl.client.ServerConnectEvent;
import fr.vengelis.afterburner.events.impl.client.ServerDisconnectEvent;
import fr.vengelis.afterburner.events.impl.common.PrintedLogEvent;
import fr.vengelis.afterburner.events.impl.common.SendInstructionEvent;
import fr.vengelis.afterburner.language.LanguageManager;
import fr.vengelis.afterburner.logs.PrintedLog;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;

public class SocketEmbarkedClient {
    private final String host;
    private final int port;
    private final String password;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ClientInformations clientInformations;

    private Boolean displaylog;

    public SocketEmbarkedClient(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public void start() throws IOException {
        clientSocket = new Socket(host, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientInformations = new ClientInformations(UUID.randomUUID(), clientSocket.getInetAddress());

        // Sending to server UUID for said "new connection attempted"
        out.println(clientInformations.getUuid());

        String hashedPassword = hashPassword(this.password, clientInformations.getUuid().toString());
        out.println(hashedPassword);

        String response = in.readLine();
        if (!"Authentication successful".equals(response)) {
            ConsoleLogger.printLine(Level.SEVERE, LanguageManager.translate("socket-auth-failed"));
            stop();
            return;
        } else {
            ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("socket-auth-successfully"));
            AfterburnerClientApp.get().getEventManager().call(new ServerConnectEvent());
        }

        out.println("getdisplay");
        response = in.readLine();
        String[] msgd = response.split(":");
        displaylog = Boolean.parseBoolean(msgd[1]);
        ConsoleLogger.printLine(Level.INFO, "Display MA logs : " + displaylog);

        new Thread(() -> {
            String serverMessage;
            try {
                while ((serverMessage = in.readLine()) != null) {
                    if (serverMessage.equalsIgnoreCase("Server: wrong password")) {
                        ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("socket-auth-failed") + " " + LanguageManager.translate("socket-disconnected-by-server"));
                        stop();
                        break;
                    } else if (serverMessage.equalsIgnoreCase("Server: disconnect")) {
                        ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("socket-disconnected-by-server"));
                        stop();
                        break;
                    } else if (serverMessage.equalsIgnoreCase("Server:RequireClientDisconnect")) {
                        ConsoleLogger.printLine(Level.INFO, LanguageManager.translate("socket-disconnected-by-server"));
                        stop();
                        break;
                    } else if (serverMessage.startsWith("dli:")) {
                        String[] msg = serverMessage.split(":");
                        displaylog = Boolean.parseBoolean(msg[1]);
                    } else {
                        String msg;
                        if(serverMessage.startsWith("Server:EchoLog:")) {
                            if(displaylog) {
                                msg = serverMessage.replace("Server:EchoLog:", "");
                                if(msg.startsWith("SkipEmbarked:")) msg = msg.replace("SkipEmbarked:", "");
                                PrintedLog log = PrintedLog.deserialize(msg);
                                PrintedLogEvent event = new PrintedLogEvent(log, PrintedLogEvent.Handler.CLI);
                                AfterburnerClientApp.get().getEventManager().call(event);
                                if(event.isCancelled())
                                    continue;
                                msg = "[MA] [" + event.getLog().getLevel().getName() + "] " + event.getLog().getLine();
                                ConsoleLogger.printLine(Level.INFO, "[SCR] > " + msg);
                            }
                        } else if(serverMessage.startsWith("Echo:")){
                            msg = serverMessage.substring(5);
                            CommandResult<?> rtn;
                            String[] splited = msg.split("---ATBSPLITER---");
//                            String type = splited[0].replace("Type=", "");
                            rtn = CommandResult.deserialize(splited[1]);
                            CommandResultReader.read(rtn);
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }).start();

        Scanner keyboard = new Scanner(System.in);
        String input;

        ConsoleLogger.printLinesBox(Level.INFO,
                new String[] {
                        "Afterburner operational !",
                        "Write \"help\" to display all available commands",
                });
        while (true) {
            input = keyboard.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                CommandInstruction instruction = new CommandInstruction(
                        input,
                        input.split("\\s+"),
                        AtbCommand.CommandSide.CLIENT);
                SendInstructionEvent event = new SendInstructionEvent(instruction);
                AfterburnerClientApp.get().getEventManager().call(event);
                if(event.isCancelled())
                    ConsoleLogger.printLine(Level.INFO, "Command cancel reason : " + event.getCancelReason());
                else {
                    CommandResult<?> rtn = AfterburnerClientApp.get().getCliManager().execute(event.getInstruction());
                    AfterburnerClientApp.get().getEventManager().call(new CommandReceiveResultEvent(rtn));
                    if(rtn.getType().equals(CommandResult.ResponseType.ERROR)) {
                        AtbCommand.ExecutionResult<?> result = rtn.getExecutionResult();
                        if(result.getResponseData() instanceof String)
                            ConsoleLogger.printLine(Level.SEVERE, rtn.getResponseData().getResponseData().toString());
                        else if(result.getResponseData() instanceof ArrayDeque)
                            ((ArrayDeque<String>) result.getResponseData()).forEach(l -> ConsoleLogger.printLine(Level.SEVERE, l));
                    }
                }
            } else {
                ConsoleLogger.printLine(Level.SEVERE, "No command entered. Please try again.");
            }
        }
    }

    private String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes());
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCommand(CommandInstruction command) {
        out.println(command.getInput());
        out.flush();
    }

    public void sendCommand(String command) {
        out.println(command);
        out.flush();
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
        AfterburnerClientApp.get().getEventManager().call(new ServerDisconnectEvent());
    }

    public ClientInformations getClientInformations() {
        return clientInformations;
    }

}