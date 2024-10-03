package fr.vengelis.afterburner.interconnection.socket.system;

import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.cli.command.AtbCommand;
import fr.vengelis.afterburner.cli.command.CommandResult;
import fr.vengelis.afterburner.cli.command.CommandResultReader;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;

public class ClientHandler implements Runnable {
    private ClientInformations clientInformations;
    private final Socket clientSocket;
    private final SocketServer server;
    private PrintWriter out;
    private BufferedReader in;
    private boolean authenticated = false;
    private boolean disconnected = false;

    public ClientHandler(Socket clientSocket, SocketServer server, ClientInformations clientInformations) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.clientInformations = clientInformations;
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String hashedPassword = in.readLine();
            if (verifyPassword(hashedPassword)) {
                authenticated = true;
                out.println("Authentication successful");
                server.addClient(clientInformations.getUuid(), this);
            } else {
                out.println("Authentication failed");
                closeConnection();
                return;
            }

            String inputLine;
            try {
                while ((inputLine = in.readLine()) != null) {
                    if (!authenticated) {
                        out.println("Server: wrong password");
                        closeConnection();
                    } else if (inputLine.equalsIgnoreCase("disconnect")) {
                        out.println("Server: disconnect");
                        closeConnection();
                        break;
                    } else if (inputLine.equalsIgnoreCase("getdisplay")) {
                        out.println("dli:" + AfterburnerSlaveApp.get().isDisplayOutput());
                    }

                    /*
                    // Explicitement utile pour l'application cliente
                    else if(inputLine.startsWith("Server:EchoLog:")) {
                        String msg = inputLine.replace("Server:EchoLog:", "");
                        PrintedLog log = PrintedLog.deserialize(msg);
                        ConsoleLogger.printLine(Level.INFO, "SocketEmbarkedClient Receive Log : [" + log.getLevel().getName() + "] " + log.getLine());
                    }
                    */
                    else {
                        if(inputLine.startsWith("Server:EchoLog:")) continue;
                        ConsoleLogger.printLine(Level.INFO, "[SocketServer] Received from " + clientInformations.getUuid() + ": " + inputLine);
                        if(!inputLine.trim().isEmpty()) {
                            CommandResult<?> rtn = AfterburnerSlaveApp.get().getCliManager().execute(inputLine, AtbCommand.CommandSide.SERVER);
                            CommandResultReader.read(rtn);
                            AfterburnerSlaveApp.get().getSocketServer().sendAllClient("Echo:Type=" + rtn.getResponseData().getResponseData().getClass().getName() + "---ATBSPLITER---" + rtn.serialize());
                        } else {
                            out.println("Echo: No command entered. Please try again.");
                        }
                    }
                }
            } catch (SocketException e) {
                ConsoleLogger.printLine(Level.INFO, "Query client disconnected: " + clientInformations.getUuid());
                disconnected = true;
                AfterburnerSlaveApp.get().getSocketServer().forceDisconnectClient(clientInformations.getUuid());

            }
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
        } finally {
            closeConnection();
            server.removeClient(clientInformations.getUuid());
        }
    }

    private boolean verifyPassword(String hashedPassword) {
        String correctHashedPassword = hashPassword((String) ConfigGeneral.QUERY_PASSWORD.getData(), clientInformations.getUuid().toString());
        return correctHashedPassword.equals(hashedPassword);
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

    public void closeConnection() {
        out.println("Server:RequireClientDisconnect");
//        try {
//            if (in != null) in.close();
//            if (out != null) out.close();
//            if (clientSocket != null) clientSocket.close();
//        } catch (IOException e) {
//            ConsoleLogger.printStacktrace(e);
//        }
    }

    public boolean isDisconnected() {
        return disconnected;
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    public ClientInformations getClientInformations() {
        return clientInformations;
    }

}