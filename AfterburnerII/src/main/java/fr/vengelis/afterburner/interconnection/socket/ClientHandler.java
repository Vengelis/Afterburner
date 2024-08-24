package fr.vengelis.afterburner.interconnection.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class ClientHandler implements Runnable {
    private UUID clientId;
    private final Socket clientSocket;
    private final SocketServer server;
    private PrintWriter out;
    private BufferedReader in;
    private boolean authenticated = false;
    private boolean disconnected = false;

    public ClientHandler(Socket clientSocket, SocketServer server, UUID clientId) {
        this.clientSocket = clientSocket;
        this.server = server;
        this.clientId = clientId;
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
                server.addClient(clientId, this);
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
                    } else {
                        System.out.println("Received from " + clientId + ": " + inputLine);
                        out.println("Echo: " + inputLine);
                    }
                }
            } catch (SocketException e) {
                System.out.println("Client disconnected: " + clientId);
                disconnected = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeConnection();
            server.removeClient(clientId);
        }
    }

    private boolean verifyPassword(String hashedPassword) {
        String correctHashedPassword = hashPassword("correct_password", clientId.toString());
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
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isDisconnected() {
        return disconnected;
    }
}