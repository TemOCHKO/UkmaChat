package org.temochko.Presentation;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;


public class ChatFrame extends JFrame {

    // ── Palette ───────────────────────────────────────────────────────────────
    static final Color SIDEBAR_BG      = new Color(0x2C343F); // Dark gray
    static final Color SIDEBAR_HOVER   = new Color(0x363D4D);
    static final Color SEARCH_BG       = new Color(0x363D4D);
    static final Color CHAT_BG         = new Color(0xF2F5F8); // Light blueish gray
    static final Color BUBBLE_THEM     = new Color(0x86BB71); // Green
    static final Color BUBBLE_ME       = new Color(0x94C2ED); // Blue
    static final Color TEXT_DARK       = new Color(0x434651);
    static final Color TEXT_LIGHT      = new Color(0xFFFFFF);
    static final Color TEXT_MUTED      = new Color(0x92959E);
    static final Color STATUS_ONLINE   = new Color(0x86BB71);
    static final Color STATUS_OFFLINE  = new Color(0xE38968);

    // ── Components (package-private for Controller) ───────────────────────────
    public JTextField searchField;
    public JList<ContactItem> contactList;
    public DefaultListModel<ContactItem> contactModel;

    public JLabel chatHeaderName;
    public JLabel chatHeaderDetails;
    public JPanel messagePanel;
    public JScrollPane messageScrollPane;

    public JTextField messageInputField;
    public JButton sendButton;

    private String currentUser;

    public ChatFrame(String currentUser) {
        this.currentUser = currentUser;
        setTitle("UkmaChat — " + currentUser);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(buildSidebar(), BorderLayout.WEST);
        add(buildChatArea(), BorderLayout.CENTER);
    }

    // ── 1. Sidebar (Left Panel) ───────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(300, getHeight()));
        sidebar.setBackground(SIDEBAR_BG);

        // Search Bar Area
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        searchPanel.setBackground(SIDEBAR_BG);
        searchField = new JTextField("");
        searchField.setPreferredSize(new Dimension(260, 40));
        searchField.setBackground(SEARCH_BG);
        searchField.setForeground(TEXT_LIGHT);
        searchField.setCaretColor(TEXT_LIGHT);
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SEARCH_BG, 1, true),
                new EmptyBorder(5, 15, 5, 15)
        ));
        searchPanel.add(searchField);

        // Contacts List
        contactModel = new DefaultListModel<>();
        contactList = new JList<>(contactModel);
        contactList.setBackground(SIDEBAR_BG);
        contactList.setCellRenderer(new ContactListRenderer());
        contactList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactList.setFixedCellHeight(70);

        JScrollPane listScroll = new JScrollPane(contactList);
        listScroll.setBorder(null);
        listScroll.getVerticalScrollBar().setPreferredSize(new Dimension(5, 0)); // Thin scrollbar

        sidebar.add(searchPanel, BorderLayout.NORTH);
        sidebar.add(listScroll, BorderLayout.CENTER);

        return sidebar;
    }

    // ── 2. Chat Area (Right Panel) ────────────────────────────────────────────
    private JPanel buildChatArea() {
        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(CHAT_BG);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CHAT_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                new EmptyBorder(20, 30, 20, 30)
        ));

        JPanel headerText = new JPanel(new GridLayout(2, 1));
        headerText.setOpaque(false);
        chatHeaderName = new JLabel("Select a chat");
        chatHeaderName.setFont(new Font("Inter", Font.BOLD, 16));
        chatHeaderName.setForeground(TEXT_DARK);

        chatHeaderDetails = new JLabel(" ");
        chatHeaderDetails.setFont(new Font("Inter", Font.PLAIN, 13));
        chatHeaderDetails.setForeground(TEXT_MUTED);

        headerText.add(chatHeaderName);
        headerText.add(chatHeaderDetails);
        header.add(headerText, BorderLayout.WEST);

        // Message History
        messagePanel = new JPanel();
        messagePanel.setLayout(new BoxLayout(messagePanel, BoxLayout.Y_AXIS));
        messagePanel.setBackground(CHAT_BG);
        messagePanel.setBorder(new EmptyBorder(20, 30, 20, 30));

        messageScrollPane = new JScrollPane(messagePanel);
        messageScrollPane.setBorder(null);
        messageScrollPane.getViewport().setBackground(CHAT_BG);
        messageScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Input Area
        JPanel inputArea = new JPanel(new BorderLayout(10, 0));
        inputArea.setBackground(Color.WHITE);
        inputArea.setBorder(new EmptyBorder(15, 20, 15, 20));

        messageInputField = new JTextField();
        messageInputField.setFont(new Font("Inter", Font.PLAIN, 14));
        messageInputField.setForeground(TEXT_DARK);
        messageInputField.setBorder(null);
        TextPrompt tp = new TextPrompt("Type your message...", messageInputField); // Simple placeholder logic
        tp.setForeground(TEXT_MUTED);

        sendButton = new JButton("SEND");
        sendButton.setFont(new Font("Inter", Font.BOLD, 14));
        sendButton.setForeground(BUBBLE_ME);
        sendButton.setContentAreaFilled(false);
        sendButton.setBorderPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        inputArea.add(messageInputField, BorderLayout.CENTER);
        inputArea.add(sendButton, BorderLayout.EAST);

        chatArea.add(header, BorderLayout.NORTH);
        chatArea.add(messageScrollPane, BorderLayout.CENTER);
        chatArea.add(inputArea, BorderLayout.SOUTH);

        return chatArea;
    }

    // ── Public UI Methods for Controller ──────────────────────────────────────
    public void addMessage(String senderName, String text, String time, boolean isMine) {
        JLabel metaLabel = new JLabel(isMine ? time : senderName + "  " + time);
        metaLabel.setFont(new Font("Inter", Font.BOLD, 11));
        metaLabel.setForeground(TEXT_MUTED);

        ChatBubble bubble = new ChatBubble(text, isMine);

        // inner: stacks meta + bubble, sized to content
        JPanel inner = new JPanel();
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
        inner.setOpaque(false);
        metaLabel.setAlignmentX(isMine ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        bubble.setAlignmentX(isMine ? Component.RIGHT_ALIGNMENT : Component.LEFT_ALIGNMENT);
        inner.add(metaLabel);
        inner.add(Box.createVerticalStrut(3));
        inner.add(bubble);

        // outer: full-width row, pins inner to left or right via BorderLayout
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);
        outer.setBorder(new EmptyBorder(5, 0, 15, 0));
        outer.add(inner, isMine ? BorderLayout.EAST : BorderLayout.WEST);

        // must validate before reading preferred size, otherwise height = 0
        outer.validate();
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, outer.getPreferredSize().height));

        messagePanel.add(outer);
        messagePanel.revalidate();
        messagePanel.repaint();

        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = messageScrollPane.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }

    public void clearMessages() {
        messagePanel.removeAll();
        messagePanel.revalidate();
        messagePanel.repaint();
    }

    public String getCurrentUser() {
        return currentUser;
    }

    // ── Inner Classes for Custom UI Elements ──────────────────────────────────

    /** Helper class to hold contact data for the UI */
    public static class ContactItem {
        public String username;
        public boolean isOnline;
        public String statusText;

        public ContactItem(String username, boolean isOnline, String statusText) {
            this.username = username;
            this.isOnline = isOnline;
            this.statusText = statusText;
        }
    }

    private class ContactListRenderer extends JPanel implements ListCellRenderer<ContactItem> {
        private JLabel avatarLabel;
        private JLabel nameLabel;
        private JLabel statusLabel;
        private JPanel textPanel;

        public ContactListRenderer() {
            setLayout(new BorderLayout(15, 0));
            setBorder(new EmptyBorder(10, 20, 10, 20));
            setOpaque(true);

            avatarLabel = new JLabel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    // Generate a color based on the username string hash
                    int hash = Math.abs(getText().hashCode());
                    Color[] colors = {new Color(0xE38968), new Color(0x86BB71), new Color(0x94C2ED), new Color(0xF3CA73)};
                    g2.setColor(colors[hash % colors.length]);
                    g2.fill(new Ellipse2D.Double(0, 0, 45, 45));

                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("Inter", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    String initial = getText().substring(0, 1).toUpperCase();
                    int x = (45 - fm.stringWidth(initial)) / 2;
                    int y = ((45 - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(initial, x, y);
                    g2.dispose();
                }
            };
            avatarLabel.setPreferredSize(new Dimension(45, 45));

            textPanel = new JPanel(new GridLayout(2, 1));
            textPanel.setOpaque(false);

            nameLabel = new JLabel();
            nameLabel.setFont(new Font("Inter", Font.BOLD, 15));
            nameLabel.setForeground(TEXT_LIGHT);

            statusLabel = new JLabel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getForeground() == STATUS_ONLINE ? STATUS_ONLINE : STATUS_OFFLINE);
                    g2.fillOval(0, getHeight()/2 - 4, 8, 8);
                    g2.dispose();
                }
            };
            statusLabel.setFont(new Font("Inter", Font.PLAIN, 12));
            statusLabel.setForeground(TEXT_MUTED);
            statusLabel.setBorder(new EmptyBorder(0, 15, 0, 0)); // Space for the dot

            textPanel.add(nameLabel);
            textPanel.add(statusLabel);

            add(avatarLabel, BorderLayout.WEST);
            add(textPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends ContactItem> list, ContactItem value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) return this;

            avatarLabel.setText(value.username);
            nameLabel.setText(value.username);
            statusLabel.setText(value.statusText);
            statusLabel.setForeground(value.isOnline ? STATUS_ONLINE : TEXT_MUTED);

            setBackground(isSelected ? SIDEBAR_HOVER : SIDEBAR_BG);
            return this;
        }
    }

    /** Custom component for drawing the chat bubbles */
    private class ChatBubble extends JPanel {
        private String text;
        private boolean isMine;

        public ChatBubble(String text, boolean isMine) {
            this.text = text;
            this.isMine = isMine;
            setOpaque(false);

            // JTextArea allows for text wrapping inside the bubble
            JTextArea textArea = new JTextArea(text);
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setEditable(false);
            textArea.setFocusable(false);
            textArea.setOpaque(false);
            textArea.setForeground(TEXT_LIGHT);
            textArea.setFont(new Font("Inter", Font.PLAIN, 14));

            // Calculate width dynamically (max width 400px)
            FontMetrics fm = getFontMetrics(textArea.getFont());
            int width = Math.min(fm.stringWidth(text) + 40, 400);

            setLayout(new BorderLayout());
            setBorder(new EmptyBorder(12, 16, 12, 16));
            setPreferredSize(new Dimension(width, textArea.getPreferredSize().height + 24));
            setMaximumSize(new Dimension(width, textArea.getPreferredSize().height + 24));

            add(textArea, BorderLayout.CENTER);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isMine ? BUBBLE_ME : BUBBLE_THEM);
            // Draw rounded rectangle
            g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 15, 15));

            // Draw the little tail pointer
            if (isMine) {
                Polygon tail = new Polygon(new int[]{getWidth(), getWidth(), getWidth()-10}, new int[]{getHeight()-15, getHeight(), getHeight()-15}, 3);
                g2.fillPolygon(tail);
            } else {
                Polygon tail = new Polygon(new int[]{0, 0, 10}, new int[]{getHeight()-15, getHeight(), getHeight()-15}, 3);
                g2.fillPolygon(tail);
            }
            g2.dispose();
        }
    }

    /** Simple utility for placeholders in JTextFields */
    private class TextPrompt extends JLabel {
        public TextPrompt(String text, JTextField component) {
            super(text);
            component.setLayout(new BorderLayout());
            component.add(this);
            component.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                public void insertUpdate(javax.swing.event.DocumentEvent e) { setVisible(false); }
                public void removeUpdate(javax.swing.event.DocumentEvent e) { setVisible(component.getText().isEmpty()); }
                public void changedUpdate(javax.swing.event.DocumentEvent e) {}
            });
        }
    }
}