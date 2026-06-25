package org.temochko.Business.Controllers;

import org.temochko.Business.DTOs.Message.ChatMessage;
import org.temochko.Business.DTOs.User.UserSummaryDto;
import org.temochko.Presentation.Frames.ChatFrame;
import org.temochko.NetworkLayer.ClientSide.NetworkClient;

import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The main controller that manages the main chat functionality
 * It is responsible for displaying messages, searching for users,
 * and giving responsibility to NetworkClient with the server.
 */
public class MainController {

    private final ChatFrame view;
    private final NetworkClient networkClient;
    private ChatFrame.ContactItem currentChatUser;

    private final Map<String, List<ChatMessage>> localMessageCache = new HashMap<>();
    private final java.util.Set<String> loadedHistories = new java.util.HashSet<>();

    public MainController(ChatFrame view, NetworkClient networkClient) {
        this.view = view;
        this.networkClient = networkClient;

        initView();
        initNetwork();
        wireEvents();
    }

    private void initView() {
        // TODO: Replace with an actual call to fetch friends/contacts from DB

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
    }

    /**
     * Activated when the user clicks on a contact in the list.
     * Enables the input field and attempts to load chat history from local cache or server.
     */
    private void onContactSelected() {
        ChatFrame.ContactItem selected = view.contactList.getSelectedValue();
        if (selected == null) return;

        currentChatUser = selected;

        view.messageInputField.setEnabled(true);
        view.sendButton.setEnabled(true);
        view.chatHeaderName.setText("Chat with " + currentChatUser.username);
        view.chatHeaderDetails.setText(currentChatUser.isOnline ? "online" : "offline");
        view.clearMessages();

        if (!loadedHistories.contains(currentChatUser.username)) {
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    networkClient.sendHistoryRequest(currentChatUser.username);
                    return null;
                }
            };
            worker.execute();
        } else {
            List<ChatMessage> history = localMessageCache.get(currentChatUser.username);
            for (ChatMessage msg : history) {
                boolean isMine = msg.from.equals(view.getCurrentUser());
                view.addMessage(isMine ? "You" : msg.from, msg.message, msg.timestamp, isMine);
            }
        }
    }

    /**
     * Takes the text from message input field, draws a sent message on client and
     * sends a message dto to the NetwrokClient
     */
    private void onSendMessage() {
        String text = view.messageInputField.getText().trim();
        if (text.isEmpty() || currentChatUser == null) return;

        view.messageInputField.setText("");
        String formattedTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        view.addMessage("You", text, formattedTime, true);
        ChatMessage msg = new ChatMessage(currentChatUser.username, view.getCurrentUser(), text, formattedTime);

        localMessageCache.putIfAbsent(currentChatUser.username, new ArrayList<>());
        localMessageCache.get(currentChatUser.username).add(msg);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                networkClient.sendMessage(msg);
                return null;
            }
        };
        worker.execute();
    }

    /**
     * Responsible for searching users in the searchField
     */
    private void onSearching() {
        String text = view.searchField.getText().trim();
        if (text.isEmpty()) return;

        // Clear the field
        view.searchField.setText("");
        view.contactModel.removeAllElements();

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                networkClient.sendSearchUserRequest(text);
                return null;
            }
        };
        worker.execute();
    }

    /**
     * Configures network to react to server responses.
     * Listens for search results, incoming messages, and chat history.
     */
    private void initNetwork() {
        networkClient.setOnSearchResponseReceived(dto -> {
            SwingUtilities.invokeLater(() -> {
                if (dto == null) return;
                view.contactModel.removeAllElements();

                for (int i = 0; i < dto.foundUsers.size(); i++) {
                    UserSummaryDto user = dto.foundUsers.get(i);

                    String statusText = user.isOnline ? "online" : "offline";

                    if (!user.username.equals(view.getCurrentUser())) {
                        view.contactModel.addElement(new ChatFrame.ContactItem(user.username, user.isOnline, statusText));
                    }
                }

                // update ui
                view.contactList.revalidate();
                view.contactList.repaint();
            });
        });

        networkClient.setOnMessageReceived(msg -> {
            SwingUtilities.invokeLater(() -> {

                localMessageCache.putIfAbsent(msg.from, new ArrayList<>());
                localMessageCache.get(msg.from).add(msg);

                if (currentChatUser != null && currentChatUser.username.equals(msg.from)) {
                    view.addMessage(msg.from, msg.message, msg.timestamp, false);

                    // find the sender and get him to the top of the stack
                    view.contactModel.removeElement(currentChatUser);
                    view.contactModel.add(0, currentChatUser);
                    view.contactList.repaint();
                } else {
                    // chat with sender is not open
                    updateSidebarWithUnreadNotification(msg.from);
                }
            });
        });

        networkClient.setOnHistoryReceived(dto -> {
            SwingUtilities.invokeLater(() -> {
                loadedHistories.add(dto.targetUsername);
                localMessageCache.put(dto.targetUsername, dto.history);

                if (currentChatUser != null && currentChatUser.username.equals(dto.targetUsername)) {
                    view.clearMessages();
                    for (ChatMessage msg : dto.history) {
                        boolean isMine = msg.from.equals(view.getCurrentUser());
                        view.addMessage(isMine ? "You" : msg.from, msg.message, msg.timestamp, isMine);
                    }
                }
            });
        });

        networkClient.startListening();
    }

    private void updateSidebarWithUnreadNotification(String senderUsername) {
        boolean userFound = false;
        ChatFrame.ContactItem targetItem = null;

        // search in out left sidebar
        for (int i = 0; i < view.contactModel.size(); i++) {
            ChatFrame.ContactItem item = view.contactModel.get(i);
            if (item.username.equals(senderUsername)) {
                targetItem = item;
                targetItem.statusText = "new message";
                userFound = true;

                view.contactModel.remove(i);
                break;
            }
        }

        // if he is not on the left, we create him
        if (!userFound) {
            targetItem = new ChatFrame.ContactItem(senderUsername, true, "new message");
        }

        // add to the top of the "stack"
        view.contactModel.add(0, targetItem);

        // update ui
        view.contactList.revalidate();
        view.contactList.repaint();
    }

}