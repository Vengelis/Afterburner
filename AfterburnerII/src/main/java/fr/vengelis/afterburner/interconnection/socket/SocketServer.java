package fr.vengelis.afterburner.interconnection.socket;

import fr.vengelis.afterburner.AfterburnerApp;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.handler.PreInitHandler;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SocketServer implements PreInitHandler {
    private int port;
    private final ConcurrentHashMap<UUID, ClientHandler> clients = new ConcurrentHashMap<>();
    private boolean running = true;

    public SocketServer() {
        HandlerRecorder.get().register(this);
        AfterburnerApp.get().setSocketServer(this);
    }

    public void init() {
        this.port = (int) ConfigGeneral.QUERY_PORT.getData();
        new Thread(this::start).start();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);
            while (running) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                UUID clientId = UUID.fromString(in.readLine());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, clientId);
                clients.put(clientId, clientHandler);
                new Thread(clientHandler).start();
                System.out.println("Client connected: " + clientId);
            }
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
        }
    }

    public void addClient(UUID clientId, ClientHandler clientHandler) {
        clients.put(clientId, clientHandler);
        System.out.println("Client connected: " + clientId);
    }

    public void removeClient(UUID clientId) {
        boolean disconnected = clients.get(clientId).isDisconnected();
        clients.remove(clientId);
        if(!disconnected) System.out.println("Client disconnected: " + clientId);
    }

    public void forceDisconnectClient(UUID clientId) {
        ClientHandler clientHandler = clients.get(clientId);
        if (clientHandler != null) {
            clientHandler.closeConnection();
            removeClient(clientId);
        }
    }

    public void stop() {
        clients.forEach((id, c) -> forceDisconnectClient(id));
        running = false;
    }
}