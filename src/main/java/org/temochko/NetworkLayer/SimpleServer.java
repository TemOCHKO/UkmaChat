package org.temochko.NetworkLayer;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class SimpleServer {
    private static final CopyOnWriteArrayList<ObjectOutputStream> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(8080);
        System.out.println("MVP Server running on port 8080...");

        while (true) {
            Socket socket = serverSocket.accept();
            new Thread(() -> handleClient(socket)).start();
        }
    }

    private static void handleClient(Socket socket) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            clients.add(out);

            while (!socket.isClosed()) {
                String message = (String) in.readObject();
                broadcast(message);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected.");
        }
    }

    private static void broadcast(String message) {
        for (ObjectOutputStream clientOut : clients) {
            try {
                clientOut.writeObject(message);
                clientOut.flush();
            } catch (IOException e) {
                clients.remove(clientOut);
            }
        }
    }
}