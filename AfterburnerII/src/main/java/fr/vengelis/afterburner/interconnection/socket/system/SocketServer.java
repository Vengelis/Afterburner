package fr.vengelis.afterburner.interconnection.socket.system;

import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.handler.PreInitHandler;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class SocketServer implements PreInitHandler {
    private InetAddress endpoint = null;
    private int port;
    private final ConcurrentHashMap<UUID, ClientHandler> clients = new ConcurrentHashMap<>();
    private boolean running = true;

    public SocketServer() {
        HandlerRecorder.get().register(this);
    }

    public void init() {
        this.port = (int) ConfigGeneral.QUERY_PORT.getData();
        new Thread(this::startServer).start();
    }

    public void startServer() {

        try {
            if(Boolean.parseBoolean(ConfigGeneral.QUERY_AUTO_BIND.getData().toString()))
                endpoint = InetAddress.getLocalHost();
            else
                endpoint = InetAddress.getByName(
                        ConfigGeneral.QUERY_HOST.getData().toString().equalsIgnoreCase("localhost") ?
                                "127.0.0.1" : ConfigGeneral.QUERY_HOST.getData().toString()
                );
        } catch (UnknownHostException ex) {
            System.out.println("Hostname can not be resolved");
            System.exit(1);
        }

        this.port = (Integer) ConfigGeneral.QUERY_PORT.getData();

        try (ServerSocket serverSocket = new ServerSocket(
                this.port,
                50,
                this.endpoint
        )) {
            ConsoleLogger.printLine(Level.INFO, "Query socket server started on '" + endpoint.getHostAddress() + ":" + ConfigGeneral.QUERY_PORT.getData() + "'");
            while (running) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                UUID clientId = UUID.fromString(in.readLine());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, clientId);
                addClient(clientId, clientHandler);
                new Thread(clientHandler).start();

            }
        } catch (IOException e) {
            ConsoleLogger.printStacktrace(e);
        }
    }

    public void addClient(UUID clientId, ClientHandler clientHandler) {
        if(clients.containsKey(clientId)) return;
        clients.put(clientId, clientHandler);
        ConsoleLogger.printLine(Level.INFO, "Query client connected : " + clientId);
    }

    public void removeClient(UUID clientId) {
        if(!clients.containsKey(clientId)) return;
        boolean disconnected = clients.get(clientId).isDisconnected();
        clients.remove(clientId);
        if(!disconnected) ConsoleLogger.printLine(Level.INFO, "Query client disconnected: " + clientId);
    }

    public void forceDisconnectClient(UUID clientId) {
        ClientHandler clientHandler = clients.get(clientId);
        if (clientHandler != null) {
            clientHandler.closeConnection();
            removeClient(clientId);
            ConsoleLogger.printLine(Level.INFO, "Client socket " + clientId + " closed.");
        }
    }

    public void stop() {
        clients.forEach((id, c) -> forceDisconnectClient(id));
        running = false;
    }

    public InetAddress getEndpoint() {
        return endpoint;
    }

    public int getPort() {
        return port;
    }

    public void sendAllClient(String message) {
        clients.forEach((id, c) -> c.sendMessage(message));
    }
}