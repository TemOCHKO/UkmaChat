package org.temochko.NetworkLayer;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class ChatClient {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private final Consumer<String> onMessageReceived;

    // The callback allows the network layer to pass data to the UI without depending on Swing classes
    public ChatClient(String host, int port, Consumer<String> onMessageReceived) {
        this.onMessageReceived = onMessageReceived;
        connect(host, port);
    }

    private void connect(String host, int port) {
        new Thread(() -> {
            try {
                socket = new Socket(host, port);
                out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                in = new ObjectInputStream(socket.getInputStream());

                // Continuous listening loop
                while (!socket.isClosed()) {
                    String message = (String) in.readObject();
                    onMessageReceived.accept(message); // Pass message to UI
                }
            } catch (Exception e) {
                onMessageReceived.accept("System: Disconnected from server.");
            }
        }).start(); // Run network listening in a background thread
    }

    public void sendMessage(String msg) {
        try {
            if (out != null) {
                out.writeObject(msg);
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}