package fr.vengelis.afterburner.interconnection.socket.system;

import fr.vengelis.afterburner.AfterburnerSlaveApp;
import fr.vengelis.afterburner.configurations.ConfigGeneral;
import fr.vengelis.afterburner.events.impl.common.ClientConnectEvent;
import fr.vengelis.afterburner.events.impl.common.ClientDisconnectEvent;
import fr.vengelis.afterburner.handler.HandlerRecorder;
import fr.vengelis.afterburner.handler.PreInitHandler;
import fr.vengelis.afterburner.utils.ConsoleLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Enumeration;
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
            if(Boolean.parseBoolean(ConfigGeneral.QUERY_AUTO_BIND.getData().toString())) {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while(interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();
                    Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if(!inetAddress.getHostAddress().startsWith("127.0.0.1")) {
                            endpoint = InetAddress.getByName(inetAddress.getHostAddress());
                            break;
                        }
                    }
                }
                if(endpoint == null) {
                    endpoint = InetAddress.getLocalHost();
                }
            } else {
                endpoint = InetAddress.getByName(
                        ConfigGeneral.QUERY_HOST.getData().toString().equalsIgnoreCase("localhost") ?
                                "127.0.0.1" : ConfigGeneral.QUERY_HOST.getData().toString()
                );
            }
        } catch (UnknownHostException | SocketException ex) {
            System.out.println("Hostname can not be resolved");
            System.exit(1);
        }

        this.port = (Integer) ConfigGeneral.QUERY_PORT.getData();

        try (ServerSocket serverSocket = new ServerSocket(
                this.port,
                50,
                this.endpoint
        )) {
            ConsoleLogger.printLineBox(Level.INFO, "Query socket server started on '" + endpoint.getHostAddress() + ":" + ConfigGeneral.QUERY_PORT.getData() + "'");
            while (running) {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                UUID clientId = UUID.fromString(in.readLine());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this, new ClientInformations(clientId));
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
        AfterburnerSlaveApp.get().getEventManager().call(new ClientConnectEvent(clientHandler.getClientInformations()));
    }

    public void removeClient(UUID clientId) {
        if(!clients.containsKey(clientId)) return;
        ClientInformations ci = clients.get(clientId).getClientInformations();
        boolean disconnected = clients.get(clientId).isDisconnected();
        clients.remove(clientId);
        if(!disconnected) {
            ConsoleLogger.printLine(Level.INFO, "Query client disconnected: " + clientId);
            AfterburnerSlaveApp.get().getEventManager().call(new ClientDisconnectEvent(ci));
        }
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