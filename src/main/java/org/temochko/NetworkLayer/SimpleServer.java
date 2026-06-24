package org.temochko.NetworkLayer;
import org.temochko.Business.AuthService;
import org.temochko.DataAccess.DatabaseManager;
import org.temochko.DataAccess.Repositories.User.IUserRepository;
import org.temochko.DataAccess.Repositories.User.UserRepository;

import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class SimpleServer {
    private static final CopyOnWriteArrayList<ObjectOutputStream> clients = new CopyOnWriteArrayList<>();

    private final AuthService authService;

    public SimpleServer(AuthService authService) {
        this.authService = authService;
    }

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, authService);
                clientHandler.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}