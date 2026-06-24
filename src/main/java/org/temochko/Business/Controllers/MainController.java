package org.temochko.Business.Controllers;

import org.temochko.NetworkLayer.NetworkClient;
import org.temochko.Presentation.LoginFrame;
import org.temochko.Presentation.MainFrame;
import org.temochko.Presentation.RegistrationPage;

import javax.swing.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * MainController — wires all buttons and events in MainFrame.
 *
 * Follows the MVC pattern: the controller holds references to both the
 * view (MainFrame) and the service layer (injected via constructor), and
 * translates UI events into service calls, then updates the view with results.
 *
 * Nothing in this class touches Swing layout directly; it only reads/writes
 * the public fields and methods exposed by MainFrame.
 */
public class MainController {

    private final MainFrame view;
    // private final ChatService chatService;   // inject your service layer here

    private static final DateTimeFormatter TIME_FMT =
            DateTimeFormatter.ofPattern("HH:mm");

    public MainController(MainFrame view /*, ChatService chatService */) {
        this.view = view;
        // this.chatService = chatService;
        wireEvents();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WIRING
    // ═══════════════════════════════════════════════════════════════════════════
    private void wireEvents() {
        wireNavButtons();
        wireSendButton();
        wireEnterKey();
        wireConversationRows();
        wireNewChatButton();
        wireAttachButton();
        wireSearchField();
    }

    // ── nav buttons ───────────────────────────────────────────────────────────
    private void wireNavButtons() {
        view.btnNavChats.addActionListener(e -> onNavChats());
        view.btnNavSettings.addActionListener(e -> onNavSettings());
        view.btnNavLogout.addActionListener(e -> onNavLogout());
    }

    private void onNavChats() {
        view.markNavActive(view.btnNavChats);
        // If you later swap in a settings panel, restore the chat panel here.
    }

    private void onNavSettings() {
        view.markNavActive(view.btnNavSettings);
        view.showSettingsPanel();
        // After dialog closes the nav stays on Settings; clicking Chats restores.
        view.markNavActive(view.btnNavChats);
    }

    private void onNavLogout() {
        int choice = JOptionPane.showConfirmDialog(
                view,
                "Are you sure you want to log out?",
                "Log out",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            view.dispose();
            // Restart login screen
            SwingUtilities.invokeLater(() -> {
                LoginFrame login = new LoginFrame();
                RegistrationPage registrationPage = new RegistrationPage();
                new LoginController(login, registrationPage, new NetworkClient("127.0.0.1", 8080));
                login.setVisible(true);
            });
        }
    }

    // ── send button ───────────────────────────────────────────────────────────
    private void wireSendButton() {
        view.btnSend.addActionListener(e -> onSend());
    }

    /**
     * Enter sends; Shift+Enter inserts a newline.
     * We consume the bare Enter event so the textarea does not add a blank line.
     */
    private void wireEnterKey() {
        view.messageInput.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown()) {
                    e.consume();
                    onSend();
                }
            }
        });
    }

    private void onSend() {
        String text = view.messageInput.getText().trim();
        if (text.isEmpty()) return;

        String time = LocalTime.now().format(TIME_FMT);

        // Optimistic local render
        view.addMessage(text, true, time);
        view.messageInput.setText("");
        view.messageInput.requestFocusInWindow();

        // TODO: hand off to service layer
        // chatService.sendMessage(selectedContactId, text);

        // Simulate a reply after 1 s (remove once real service is wired)
        simulateFakeReply(time);
    }

    /** Remove this once you have a real server connection. */
    private void simulateFakeReply(String sentTime) {
        Timer timer = new Timer(1000, e -> {
            String replyTime = LocalTime.now().format(TIME_FMT);
            view.addMessage("Got it! 👍", false, replyTime);
        });
        timer.setRepeats(false);
        timer.start();
    }

    // ── conversation rows ─────────────────────────────────────────────────────
    /**
     * Attach click-listeners to every ConversationRow that is already in the
     * panel.  Call this again after dynamically adding new rows.
     */
    private void wireConversationRows() {
        for (java.awt.Component c : view.conversationList.getComponents()) {
            if (c instanceof MainFrame.ConversationRow row) {
                wireRow(row);
            }
        }
    }

    public void wireRow(MainFrame.ConversationRow row) {
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                onConversationSelected(row);
            }
        });
    }

    private void onConversationSelected(MainFrame.ConversationRow row) {
        if (row == view.selectedRow) return;   // already open

        view.selectRow(row);
        view.clearMessages();

        // TODO: load real history from chatService.getHistory(row.contactName)
        view.addDateDivider("Today");
        view.addMessage("Hey, how are you?",        false, "09:00");
        view.addMessage("Doing well, thanks!",       true,  "09:01");
        view.addMessage("Good to hear 😊",           false, "09:02");
    }

    // ── new chat button ───────────────────────────────────────────────────────
    private void wireNewChatButton() {
        view.btnNewChat.addActionListener(e -> onNewChat());
    }

    private void onNewChat() {
        String name = JOptionPane.showInputDialog(
                view, "Enter username to start a chat:", "New conversation",
                JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.isBlank()) return;

        // TODO: validate via userService.findUser(name)

        java.awt.Color color = randomAvatarColor();
        MainFrame.ConversationRow newRow = view.addConversationRow(
                name.trim(), "New conversation", "Now", 0, color);
        wireRow(newRow);
        onConversationSelected(newRow);
    }

    // ── attach button ─────────────────────────────────────────────────────────
    private void wireAttachButton() {
        view.btnAttach.addActionListener(e -> onAttach());
    }

    private void onAttach() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a file to send");
        int result = chooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            String fileName = chooser.getSelectedFile().getName();
            String time     = LocalTime.now().format(TIME_FMT);
            view.addMessage("📎 " + fileName, true, time);
            // TODO: chatService.sendFile(selectedContactId, chooser.getSelectedFile())
        }
    }

    // ── search field ──────────────────────────────────────────────────────────
    private void wireSearchField() {
        view.searchField.getDocument().addDocumentListener(
                new javax.swing.event.DocumentListener() {
                    @Override public void insertUpdate (javax.swing.event.DocumentEvent e) { onSearch(); }
                    @Override public void removeUpdate (javax.swing.event.DocumentEvent e) { onSearch(); }
                    @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { onSearch(); }
                });
    }

    private void onSearch() {
        String query = view.searchField.getText().trim().toLowerCase();
        for (java.awt.Component c : view.conversationList.getComponents()) {
            if (c instanceof MainFrame.ConversationRow row) {
                row.setVisible(query.isEmpty()
                        || row.contactName.toLowerCase().contains(query));
            }
        }
        view.conversationList.revalidate();
        view.conversationList.repaint();
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private static final java.awt.Color[] AVATAR_COLORS = {
            new java.awt.Color(0xE8A87C), new java.awt.Color(0x6C8EFF),
            new java.awt.Color(0xFF7EB3), new java.awt.Color(0x50C8A8),
            new java.awt.Color(0xA78BFA), new java.awt.Color(0xFBBF24),
    };

    private int avatarColorIndex = 0;
    private java.awt.Color randomAvatarColor() {
        return AVATAR_COLORS[(avatarColorIndex++) % AVATAR_COLORS.length];
    }
}