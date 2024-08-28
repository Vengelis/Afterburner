package fr.vengelis.afterburner.interconnection.socket.system;

import fr.vengelis.afterburner.AfterburnerClientApp;
import fr.vengelis.afterburner.cli.command.AtbCommand;
import fr.vengelis.afterburner.cli.command.CommandInstruction;
import fr.vengelis.afterburner.cli.command.CommandResultReader;
import fr.vengelis.afterburner.logs.PrintedLog;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.UUID;
import java.util.logging.Level;

public class SocketEmbarkedClient {
    private final String host;
    private final int port;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private UUID clientId;

    public SocketEmbarkedClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        clientSocket = new Socket(host, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientId = UUID.randomUUID();

        out.println(clientId.toString());

        String password = "strong-password";
        String hashedPassword = hashPassword(password, clientId.toString());
        out.println(hashedPassword);

        String response = in.readLine();
        if (!"Authentication successful".equals(response)) {
            System.out.println("Authentication failed");
            stop();
            return;
        } else {
            System.out.println("Authentication successfully !");
        }

        new Thread(() -> {
            String serverMessage;
            try {
                while ((serverMessage = in.readLine()) != null) {
                    if (serverMessage.equalsIgnoreCase("Server: wrong password")) {
                        ConsoleLogger.printLine(Level.INFO, "Authentification failed. Disconnected by server");
                        stop();
                        break;
                    } else if (serverMessage.equalsIgnoreCase("Server: disconnect")) {
                        ConsoleLogger.printLine(Level.INFO, "Disconnected by server");
                        stop();
                        break;
                    }  else {
                        String msg = serverMessage;
                        if(serverMessage.startsWith("Server:EchoLog:")) {
                            msg = serverMessage.replace("Server:EchoLog:", "");
                            if(msg.startsWith("SkipEmbarked:")) msg = msg.replace("SkipEmbarked:", "");
                            PrintedLog log = PrintedLog.deserialize(msg);
                            msg = "[" + log.getLevel().getName() + "] " + log.getLine();
                        }
                        ConsoleLogger.printLine(Level.INFO, "[Server command response] > " + msg);
                    }
                }
                System.out.println("err");
            } catch (IOException ignored) {
            }
        }).start();

        Scanner keyboard = new Scanner(System.in);
        String input;

        while (true) {
            input = keyboard.nextLine();
            if (input != null && !input.trim().isEmpty()) {
                AfterburnerClientApp.get().getCliManager().execute(
                        new CommandInstruction(
                                input,
                                input.split("\\s+"),
                                AtbCommand.CommandSide.CLIENT));
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
    }

    public UUID getClientId() {
        return clientId;
    }

    public static void main(String[] args) {
        SocketEmbarkedClient client = new SocketEmbarkedClient("192.168.0.100", 46798);
        try {
            client.start();
        } catch (Exception ignored) {
        }
    }
}