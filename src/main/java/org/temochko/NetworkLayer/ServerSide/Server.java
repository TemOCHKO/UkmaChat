package org.temochko.NetworkLayer.ServerSide;
import org.temochko.Business.AuthService;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.*;

public class Server {
    private static final CopyOnWriteArrayList<ObjectOutputStream> clients = new CopyOnWriteArrayList<>();

    private final AuthService authService;

    private static final List<ClientHandler> activeClients = new CopyOnWriteArrayList<>();
    public Server(AuthService authService) {
        this.authService = authService;
    }

    public void start(int port) {
        //startHeartbeatMonitor();
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, authService);
                activeClients.add(clientHandler);
                clientHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ClientHandler getClientByUsername(String username) {
        for (ClientHandler client : activeClients)
            if (username.equals(client.getUsername())) return client;
        return null;
    }

    public static void removeClient(ClientHandler handler) {
        activeClients.remove(handler);
    }

    /*private void startHeartbeatMonitor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        // every 30 secs
        scheduler.scheduleAtFixedRate(() -> {
            long now = System.currentTimeMillis();
            long timeoutLimit = 45000;

            for (ClientHandler client : activeClients) {
                if (now - client.getLastActivityTime() > timeoutLimit) {
                    client.forceDisconnect();
                    activeClients.remove(client);
                }
            }
        }, 30, 30, TimeUnit.SECONDS);
    }*/


}