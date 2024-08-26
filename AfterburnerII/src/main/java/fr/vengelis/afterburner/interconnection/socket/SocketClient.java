package fr.vengelis.afterburner.interconnection.socket;

import fr.vengelis.afterburner.configurations.ConfigGeneral;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.UUID;

public class SocketClient {
    private final String host;
    private final int port;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private UUID clientId;

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        clientSocket = new Socket(host, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        clientId = UUID.randomUUID();
        System.out.println("Connected to server with ID: " + clientId);

        out.println(clientId.toString());

        String password = (String) ConfigGeneral.QUERY_PASSWORD.getData();
        String hashedPassword = hashPassword(password, clientId.toString());
        System.out.println("'" + hashedPassword + "'");
        out.println(hashedPassword);

        String response = in.readLine();
        if (!"Authentication successful".equals(response)) {
            System.out.println("Authentication failed");
            stop();
            return;
        }

        new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            String message;
            while (true) {
                System.out.print("Enter message: ");
                message = scanner.nextLine();
                sendCommand(message);
                if (message.equalsIgnoreCase("disconnect")) {
                    break;
                }
            }
        }).start();

        String serverMessage;
        while ((serverMessage = in.readLine()) != null) {
            if (serverMessage.equalsIgnoreCase("Server: wrong password")) {
                System.out.println("Authentification failed. Disconnected by server");
                stop();
                break;
            } else if (serverMessage.equalsIgnoreCase("Server: disconnect")) {
                System.out.println("Disconnected by server");
                stop();
                break;
            } else {
                System.out.println("Server: " + serverMessage);
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

    public void sendCommand(String command) {
        out.println(command);
    }

    public void stop() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        String host = "localhost";
        int port = 46798;
        SocketClient client = new SocketClient(host, port);
        try {
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}