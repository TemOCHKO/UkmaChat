package org.temochko.Presentation;

import org.temochko.NetworkLayer.ChatClient;

import javax.swing.*;
import java.awt.*;

public class ChatUI extends JFrame {
    private JTextArea chatHistory;
    private JTextField inputField;
    private ChatClient client;
    private final String username;

    public ChatUI(String username) {
        this.username = username;
        setTitle("UKMA Chat MVP - " + username);
        setSize(400, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Chat History Area
        chatHistory = new JTextArea();
        chatHistory.setEditable(false);
        chatHistory.setLineWrap(true);
        add(new JScrollPane(chatHistory), BorderLayout.CENTER);

        // Input Area
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        JButton sendButton = new JButton("Send");

        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Action Listeners
        sendButton.addActionListener(e -> send());
        inputField.addActionListener(e -> send()); // Allows pressing Enter

        // Initialize the network client
        client = new ChatClient("localhost", 8080, this::displayMessage);

        setVisible(true);
    }

    private void send() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            String formattedMessage = username + ": " + text;
            client.sendMessage(formattedMessage);
            inputField.setText("");
        }
    }

    // Safely updates the UI from the network thread
    private void displayMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            chatHistory.append(message + "\n");
            // Auto-scroll to bottom
            chatHistory.setCaretPosition(chatHistory.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        // Launch two clients for testing
        SwingUtilities.invokeLater(() -> new ChatUI("Student A"));
        SwingUtilities.invokeLater(() -> new ChatUI("Student B"));
    }
}