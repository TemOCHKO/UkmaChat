package org.temochko.Business.Controllers;

import org.temochko.Business.DTOs.User.SearchUserResponseDto;
import org.temochko.Business.DTOs.User.UserSummaryDto;
import org.temochko.Presentation.ChatFrame;
import org.temochko.NetworkLayer.NetworkClient;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class MainController {

    private final ChatFrame view;
    private final NetworkClient networkClient;
    private ChatFrame.ContactItem currentChatUser;

    public MainController(ChatFrame view, NetworkClient networkClient) {
        this.view = view;
        this.networkClient = networkClient;

        initView();
        wireEvents();
    }

    private void initView() {
        // TODO: Replace with an actual call to fetch friends/contacts from DB
        view.contactModel.addElement(new ChatFrame.ContactItem("Vincent Porter", true, "online"));
        view.contactModel.addElement(new ChatFrame.ContactItem("Aiden Chavez", false, "left 7 min ago"));
        view.contactModel.addElement(new ChatFrame.ContactItem("Mike Thomas", true, "online"));
        view.contactModel.addElement(new ChatFrame.ContactItem("Erika Hughes", true, "online"));
        view.contactModel.addElement(new ChatFrame.ContactItem("Monica Ward", true, "online"));

        view.messageInputField.setEnabled(false);
        view.sendButton.setEnabled(false);
    }

    private void wireEvents() {
        view.contactList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onContactSelected();
            }
        });

        view.sendButton.addActionListener(e -> onSendMessage());

        view.messageInputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onSendMessage();
                }
            }
        });

        view.searchField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    onSearching();
                }
            }
        });

        // TODO: Here you should also start a background thread (or use an existing listener in NetworkClient)
        // to constantly listen for incoming messages from the server and call view.addMessage() when one arrives.
    }

    private void onContactSelected() {
        currentChatUser = view.contactList.getSelectedValue();
        if (currentChatUser == null) return;

        view.messageInputField.setEnabled(true);
        view.sendButton.setEnabled(true);

        view.chatHeaderName.setText("Chat with " + currentChatUser.username);
        view.chatHeaderDetails.setText(currentChatUser.isOnline ? "online" : currentChatUser.statusText);

        view.clearMessages();

        // TODO: Make a network call to fetch message history for this user
        // SwingWorker<HistoryDto, Void> worker = new SwingWorker<>() { ... }

        if (currentChatUser.username.equals("Vincent Porter")) {
            view.addMessage("Vincent", "Are we meeting today? Project has been already finished and I have results to show you.", "10:12 AM", false);
            view.addMessage("You", "Well I am not sure. The rest of the team is not here yet. Maybe in an hour or so?", "10:14 AM", true);
            view.addMessage("Vincent", "Actually everything was fine. I'm very excited to show this to our team.", "10:20 AM", false);
        }
    }

    private void onSendMessage() {
        String text = view.messageInputField.getText().trim();
        if (text.isEmpty() || currentChatUser == null) return;

        // Clear the field
        view.messageInputField.setText("");

        // Get current time
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));

        // 1. Instantly display the message on the UI (Optimistic UI update)
        view.addMessage("You", text, time, true);

        // 2. Send the message to the server in the background
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                // TODO: Implement this in NetworkClient
                // networkClient.sendMessage(currentChatUser.username, text);
                System.out.println("Message sent to " + currentChatUser.username + ": " + text);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get(); // Catch any network errors
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // Optionally show an error indicator next to the message
                }
            }
        };
        worker.execute();
    }

    private void onSearching() {
        String text = view.searchField.getText().trim();
        if (text.isEmpty()) return;

        // Clear the field
        view.searchField.setText("");
        view.contactModel.removeAllElements();

        SwingWorker<SearchUserResponseDto, Void> worker = new SwingWorker<>() {
            @Override
            protected SearchUserResponseDto doInBackground() throws Exception {
                return networkClient.sendSearchUserRequest(text);
            }

            @Override
            protected void done() {
                try {
                    SearchUserResponseDto dto = get();
                    if (dto == null) return;
                    for (int i = 0; i < dto.foundUsers.size(); i++) {
                        UserSummaryDto user = dto.foundUsers.get(i);
                        String statusText = "";
                        if (user.isOnline) statusText = "online";
                        else statusText = "offline";
                        if (!user.username.equals(view.getCurrentUser())) {
                            view.contactModel.addElement(new ChatFrame.ContactItem(user.username, user.isOnline, statusText));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    // Optionally show an error indicator next to the message
                }
            }
        };
        worker.execute();
    }
}