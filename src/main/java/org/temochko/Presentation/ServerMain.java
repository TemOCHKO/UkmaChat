package org.temochko.Presentation;

import org.temochko.DataAccess.DatabaseManager;
import org.temochko.DataAccess.Repositories.User.UserRepository;
import org.temochko.Business.AuthService;
import org.temochko.NetworkLayer.ServerSide.Server;

public class ServerMain {
    public static void main(String[] args) {
        System.out.println("Server started");

        try {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            UserRepository userRepository = new UserRepository(dbManager);
            AuthService authService = new AuthService(userRepository);
            Server server = new Server(authService);

            int port = 8080;
            server.start(port);

        } catch (Exception e) {
            System.err.println("Server couldnt start");
            e.printStackTrace();
        }
    }
}