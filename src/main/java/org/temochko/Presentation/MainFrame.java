package org.temochko.Presentation;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * MainFrame — primary chat window.
 *
 * Layout:  [NavRail 60px] | [ChatList 260px] | [ChatArea flex]
 *
 * Construction order guarantees all fields are assigned before
 * any listener can fire: buildXxx() methods are called top-to-bottom
 * inside the constructor, messagesScroll is created before it is used.
 */
public class MainFrame extends JFrame {

    // ── palette ───────────────────────────────────────────────────────────────
    static final Color C_NAV_BG    = new Color(0x1E1F2E);
    static final Color C_SIDEBAR   = new Color(0xFFFFFF);
    static final Color C_CHAT_BG   = new Color(0xF4F6FB);
    static final Color C_WHITE     = Color.WHITE;
    static final Color C_ACCENT    = new Color(0x4F80FF);
    static final Color C_TEXT      = new Color(0x1A1B2E);
    static final Color C_MUTED     = new Color(0x8A8FA3);
    static final Color C_BORDER    = new Color(0xE2E5EC);
    static final Color C_HOVER     = new Color(0xF0F3FF);
    static final Color C_SELECTED  = new Color(0xE6EBFF);
    static final Color C_BUBBLE_IN = new Color(0xFFFFFF);

    // ── fonts ─────────────────────────────────────────────────────────────────
    static final Font F_BODY   = new Font("Inter", Font.PLAIN, 13);
    static final Font F_BOLD   = new Font("Inter", Font.BOLD,  13);
    static final Font F_SMALL  = new Font("Inter", Font.PLAIN, 11);
    static final Font F_HEADER = new Font("Inter", Font.BOLD,  15);

    // ── wired by controller ───────────────────────────────────────────────────
    // nav
    public JButton btnNavChats;
    public JButton btnNavSettings;
    public JButton btnNavLogout;

    // chat list
    public JTextField searchField;
    public JPanel     conversationList;   // BoxLayout column of ConversationRow
    public JButton    btnNewChat;

    // chat header
    JLabel lblChatName;
    JLabel lblChatStatus;

    // messages
    JPanel     messagesPanel;      // inner container inside scroll
    JScrollPane messagesScroll;    // assigned before any listener fires

    // input
    public JTextArea  messageInput;
    public JButton    btnSend;
    public JButton    btnAttach;

    // currently selected row
    public ConversationRow selectedRow;

    // ── constructor ───────────────────────────────────────────────────────────
    public MainFrame(String username) {
        setTitle("Chatter");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 520));
        setSize(1060, 660);
        setLocationRelativeTo(null);

        // root: nav rail | content
        JPanel root = new JPanel(new BorderLayout());
        root.add(buildNavRail(username), BorderLayout.WEST);
        root.add(buildContent(),         BorderLayout.CENTER);
        setContentPane(root);

        // seed sample data AFTER all fields are assigned
        seedSampleData();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // NAV RAIL
    // ═══════════════════════════════════════════════════════════════════════════
    private JPanel buildNavRail(String username) {
        JPanel rail = new JPanel();
        rail.setPreferredSize(new Dimension(60, 0));
        rail.setBackground(C_NAV_BG);
        rail.setLayout(new BoxLayout(rail, BoxLayout.Y_AXIS));
        rail.setBorder(new EmptyBorder(14, 0, 14, 0));

        // logo
        JLabel logo = new JLabel("C") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(C_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(C_WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 17));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("C", (getWidth() - fm.stringWidth("C")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logo.setPreferredSize(new Dimension(38, 38));
        logo.setMaximumSize(new Dimension(38, 38));
        logo.setAlignmentX(CENTER_ALIGNMENT);

        btnNavChats    = navBtn("💬", "Chats");
        btnNavSettings = navBtn("⚙",  "Settings");
        btnNavLogout   = navBtn("⏏",  "Log out");
        markNavActive(btnNavChats);

        // user initials badge at bottom
        JLabel avatar = avatarLabel(initials(username), new Color(0x6C8EFF), 36);
        avatar.setAlignmentX(CENTER_ALIGNMENT);
        avatar.setToolTipText("Signed in as " + username);

        rail.add(Box.createVerticalStrut(4));
        rail.add(logo);
        rail.add(Box.createVerticalStrut(26));
        rail.add(btnNavChats);
        rail.add(Box.createVerticalStrut(6));
        rail.add(btnNavSettings);
        rail.add(Box.createVerticalGlue());
        rail.add(btnNavLogout);
        rail.add(Box.createVerticalStrut(12));
        rail.add(avatar);
        return rail;
    }

    private JButton navBtn(String icon, String tip) {
        JButton btn = new JButton(icon) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean active = Boolean.TRUE.equals(getClientProperty("active"));
                boolean hover  = getModel().isRollover();
                if (active || hover) {
                    g2.setColor(new Color(0xFF, 0xFF, 0xFF, active ? 38 : 20));
                    g2.fillRoundRect(8, 4, getWidth() - 16, getHeight() - 8, 10, 10);
                }
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 17));
                g2.setColor(active ? C_WHITE : C_MUTED);
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (getWidth()  - fm.stringWidth(t)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(60, 42)); btn.setMaximumSize(new Dimension(60, 42));
        btn.setAlignmentX(CENTER_ALIGNMENT);
        btn.setToolTipText(tip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    public void markNavActive(JButton btn) {
        for (JButton b : List.of(btnNavChats, btnNavSettings)) {
            if (b != null) { b.putClientProperty("active", false); b.repaint(); }
        }
        btn.putClientProperty("active", true); btn.repaint();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CONTENT  (chat list | chat area)
    // ═══════════════════════════════════════════════════════════════════════════
    private JSplitPane buildContent() {
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                buildChatList(), buildChatArea());
        split.setDividerSize(1);
        split.setBorder(null);
        split.setDividerLocation(260);
        split.setResizeWeight(0);
        return split;
    }

    // ── chat list ─────────────────────────────────────────────────────────────
    private JPanel buildChatList() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_SIDEBAR);
        panel.setBorder(new MatteBorder(0, 0, 0, 1, C_BORDER));
        panel.setPreferredSize(new Dimension(260, 0));

        // header
        JPanel header = new JPanel(new BorderLayout(8, 0));
        header.setBackground(C_SIDEBAR);
        header.setBorder(new EmptyBorder(14, 14, 10, 14));

        JLabel title = new JLabel("Messages");
        title.setFont(F_HEADER); title.setForeground(C_TEXT);

        btnNewChat = new JButton("+") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? C_ACCENT : new Color(0xEEF0F8));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.setColor(getModel().isRollover() ? C_WHITE : C_ACCENT);
                g2.setFont(new Font("Inter", Font.BOLD, 17));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("+", (getWidth() - fm.stringWidth("+")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnNewChat.setContentAreaFilled(false); btnNewChat.setBorderPainted(false);
        btnNewChat.setFocusPainted(false); btnNewChat.setPreferredSize(new Dimension(30, 30));
        btnNewChat.setToolTipText("New conversation");
        btnNewChat.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        header.add(title,      BorderLayout.WEST);
        header.add(btnNewChat, BorderLayout.EAST);

        // search
        searchField = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(C_MUTED);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    g2.drawString("Search…", ins.left, ins.top + g2.getFontMetrics().getAscent());
                    g2.dispose();
                }
            }
        };
        searchField.setFont(F_BODY);
        searchField.setBackground(new Color(0xF2F4F8));
        searchField.setForeground(C_TEXT);
        searchField.setCaretColor(C_ACCENT);
        searchField.setBorder(new CompoundBorder(
                new RoundBorder(C_BORDER, 1, 20),
                new EmptyBorder(7, 12, 7, 12)));

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setBackground(C_SIDEBAR);
        searchWrap.setBorder(new EmptyBorder(0, 10, 8, 10));
        searchWrap.add(searchField);

        // conversation list (scrollable)
        conversationList = new JPanel();
        conversationList.setBackground(C_SIDEBAR);
        conversationList.setLayout(new BoxLayout(conversationList, BoxLayout.Y_AXIS));

        JScrollPane listScroll = new JScrollPane(conversationList);
        listScroll.setBorder(null);
        listScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        listScroll.getVerticalScrollBar().setUnitIncrement(12);
        styleScrollBar(listScroll.getVerticalScrollBar());

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(C_SIDEBAR);
        top.add(header,     BorderLayout.NORTH);
        top.add(searchWrap, BorderLayout.SOUTH);

        panel.add(top,        BorderLayout.NORTH);
        panel.add(listScroll, BorderLayout.CENTER);
        return panel;
    }

    // ── chat area ─────────────────────────────────────────────────────────────
    private JPanel buildChatArea() {
        // Build sub-components in strict order so fields are assigned
        // before any event can reference them.
        JPanel chatHeader = buildChatHeader();   // assigns lblChatName, lblChatStatus
        buildMessagesArea();                      // assigns messagesPanel, messagesScroll
        JPanel inputBar   = buildInputBar();      // assigns messageInput, btnSend, btnAttach

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(C_CHAT_BG);
        panel.add(chatHeader,    BorderLayout.NORTH);
        panel.add(messagesScroll, BorderLayout.CENTER);
        panel.add(inputBar,      BorderLayout.SOUTH);
        return panel;
    }

    private JPanel buildChatHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 0));
        header.setBackground(C_WHITE);
        header.setBorder(new CompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDER),
                new EmptyBorder(10, 16, 10, 16)));

        JLabel avatar = avatarLabel("AN", new Color(0xE8A87C), 38);

        JPanel nameGroup = new JPanel();
        nameGroup.setOpaque(false);
        nameGroup.setLayout(new BoxLayout(nameGroup, BoxLayout.Y_AXIS));

        lblChatName = new JLabel("Select a conversation");
        lblChatName.setFont(F_BOLD);
        lblChatName.setForeground(C_TEXT);

        lblChatStatus = new JLabel(" ");
        lblChatStatus.setFont(F_SMALL);
        lblChatStatus.setForeground(new Color(0x3DAA6E));

        nameGroup.add(lblChatName);
        nameGroup.add(lblChatStatus);

        JPanel left = new JPanel(new BorderLayout(10, 0));
        left.setOpaque(false);
        left.add(avatar,    BorderLayout.WEST);
        left.add(nameGroup, BorderLayout.CENTER);

        header.add(left, BorderLayout.WEST);
        return header;
    }

    private void buildMessagesArea() {
        messagesPanel = new JPanel();
        messagesPanel.setBackground(C_CHAT_BG);
        messagesPanel.setLayout(new BoxLayout(messagesPanel, BoxLayout.Y_AXIS));
        messagesPanel.setBorder(new EmptyBorder(14, 0, 8, 0));

        // messagesScroll is assigned HERE — before constructor finishes
        messagesScroll = new JScrollPane(messagesPanel);
        messagesScroll.setBorder(null);
        messagesScroll.setBackground(C_CHAT_BG);
        messagesScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        messagesScroll.getVerticalScrollBar().setUnitIncrement(16);
        styleScrollBar(messagesScroll.getVerticalScrollBar());
    }

    private JPanel buildInputBar() {
        JPanel bar = new JPanel(new BorderLayout(8, 0));
        bar.setBackground(C_WHITE);
        bar.setBorder(new CompoundBorder(
                new MatteBorder(1, 0, 0, 0, C_BORDER),
                new EmptyBorder(10, 14, 10, 14)));

        btnAttach = smallIconBtn("📎", "Attach file");

        messageInput = new JTextArea(1, 1);
        messageInput.setFont(F_BODY);
        messageInput.setBackground(new Color(0xF2F4F8));
        messageInput.setForeground(C_TEXT);
        messageInput.setCaretColor(C_ACCENT);
        messageInput.setLineWrap(true);
        messageInput.setWrapStyleWord(true);
        messageInput.setBorder(new CompoundBorder(
                new RoundBorder(C_BORDER, 1, 18),
                new EmptyBorder(8, 12, 8, 12)));

        // Shift+Enter = newline; Enter = send (wired in controller)
        JScrollPane inputScroll = new JScrollPane(messageInput);
        inputScroll.setBorder(null);
        inputScroll.setOpaque(false);
        inputScroll.getViewport().setOpaque(false);
        inputScroll.setPreferredSize(new Dimension(0, 38));

        btnSend = new JButton("↑") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? C_ACCENT.brighter() : C_ACCENT);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.setColor(C_WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 16));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString("↑", (getWidth()  - fm.stringWidth("↑")) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        btnSend.setContentAreaFilled(false); btnSend.setBorderPainted(false);
        btnSend.setFocusPainted(false); btnSend.setPreferredSize(new Dimension(38, 38));
        btnSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSend.setToolTipText("Send (Enter)");

        bar.add(btnAttach,   BorderLayout.WEST);
        bar.add(inputScroll, BorderLayout.CENTER);
        bar.add(btnSend,     BorderLayout.EAST);
        return bar;
    }

    // ── seed sample data ──────────────────────────────────────────────────────
    private void seedSampleData() {
        addConversationRow("Anna K.",   "Sure, sounds good!",         "10:42", 2, new Color(0xE8A87C));
        addConversationRow("Dev Team",  "PR merged ✓",                "10:15", 0, new Color(0x6C8EFF));
        addConversationRow("Maria S.",  "Are you coming tonight?",    "09:30", 1, new Color(0xFF7EB3));
        addConversationRow("Bob",       "Check the attachment I sent", "Tue",  0, new Color(0x50C8A8));

        addDateDivider("Today");
        addMessage("Hey! Are you free this evening?", false, "10:38");
        addMessage("Yeah, should be. What's up?",     true,  "10:39");
        addMessage("Wanted to catch up 😊",            false, "10:40");
        addMessage("Let's do 7 pm?",                   true,  "10:41");
        addMessage("Sure, sounds good!",               false, "10:42");
        scrollToBottom();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // PUBLIC API (called by controller)
    // ═══════════════════════════════════════════════════════════════════════════

    /** Adds a message bubble to the current chat. Thread-safe. */
    public void addMessage(String text, boolean outgoing, String time) {
        Runnable r = () -> {
            messagesPanel.add(new MessageBubble(text, outgoing, time));
            messagesPanel.add(Box.createVerticalStrut(4));
            messagesPanel.revalidate();
            messagesPanel.repaint();
            scrollToBottom();
        };
        if (SwingUtilities.isEventDispatchThread()) r.run();
        else SwingUtilities.invokeLater(r);
    }

    /** Inserts a slim date-divider row. */
    public void addDateDivider(String label) {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        row.add(new JSeparator(), c);
        c.fill = GridBagConstraints.NONE; c.weightx = 0;
        JLabel lbl = new JLabel("  " + label + "  ");
        lbl.setFont(F_SMALL); lbl.setForeground(C_MUTED);
        row.add(lbl, c);
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        row.add(new JSeparator(), c);
        messagesPanel.add(row);
    }

    /** Clears all messages from the panel. */
    public void clearMessages() {
        messagesPanel.removeAll();
        messagesPanel.revalidate();
        messagesPanel.repaint();
    }

    /** Adds a conversation row to the list. Returns it so the controller can attach listeners. */
    public ConversationRow addConversationRow(
            String name, String preview, String time, int unread, Color avatarColor) {
        ConversationRow row = new ConversationRow(name, preview, time, unread, avatarColor);
        conversationList.add(row);
        conversationList.revalidate();
        return row;
    }

    /** Selects a conversation row (highlights it). */
    public void selectRow(ConversationRow row) {
        if (selectedRow != null) selectedRow.setSelected(false);
        selectedRow = row;
        if (row != null) {
            row.setSelected(true);
            row.clearUnread();
            lblChatName.setText(row.contactName);
            lblChatStatus.setText("● Online");
        }
    }

    // ── settings view (swap center panel) ─────────────────────────────────────
    public void showSettingsPanel() {
        // Simple approach: show a dialog. For full integration, controller
        // can replace content pane. Keeping it minimal here.
        JOptionPane.showMessageDialog(this,
                buildSettingsContent(),
                "Settings", JOptionPane.PLAIN_MESSAGE);
    }

    private JPanel buildSettingsContent() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setPreferredSize(new Dimension(340, 280));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        p.add(settingRow("Display name", "Change…"));
        p.add(Box.createVerticalStrut(10));
        p.add(settingRow("Notifications", "On"));
        p.add(Box.createVerticalStrut(10));
        p.add(settingRow("Sound",         "On"));
        p.add(Box.createVerticalStrut(10));
        p.add(settingRow("Theme",         "Light"));
        return p;
    }

    private JPanel settingRow(String key, String value) {
        JPanel row = new JPanel(new BorderLayout());
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel k = new JLabel(key);   k.setFont(F_BODY); k.setForeground(C_TEXT);
        JLabel v = new JLabel(value); v.setFont(F_BODY); v.setForeground(C_MUTED);
        row.add(k, BorderLayout.WEST);
        row.add(v, BorderLayout.EAST);
        return row;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INNER CLASSES
    // ═══════════════════════════════════════════════════════════════════════════

    /** A single row in the conversation sidebar. */
    public class ConversationRow extends JPanel {
        public final String contactName;
        public final Color  avatarColor;
        private final JLabel unreadBadge;
        private boolean selected;

        public ConversationRow(String name, String preview, String time, int unread, Color color) {
            this.contactName = name;
            this.avatarColor = color;
            setOpaque(true);
            setBackground(C_SIDEBAR);
            setLayout(new BorderLayout(10, 0));
            setBorder(new EmptyBorder(9, 12, 9, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel avatar = avatarLabel(initials(name), color, 38);

            // text column
            JPanel col = new JPanel();
            col.setOpaque(false);
            col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

            JLabel nameLabel = new JLabel(name);
            nameLabel.setFont(F_BOLD); nameLabel.setForeground(C_TEXT);

            JLabel previewLabel = new JLabel(clip(preview, 30));
            previewLabel.setFont(F_SMALL); previewLabel.setForeground(C_MUTED);

            col.add(nameLabel);
            col.add(Box.createVerticalStrut(2));
            col.add(previewLabel);

            // right column: time + badge
            JPanel right = new JPanel();
            right.setOpaque(false);
            right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

            JLabel timeLabel = new JLabel(time);
            timeLabel.setFont(new Font("Inter", Font.PLAIN, 10));
            timeLabel.setForeground(C_MUTED);
            timeLabel.setAlignmentX(RIGHT_ALIGNMENT);

            unreadBadge = new JLabel(unread > 0 ? String.valueOf(unread) : "") {
                @Override protected void paintComponent(Graphics g) {
                    if (getText().isEmpty()) return;
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(C_ACCENT);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.setColor(C_WHITE);
                    g2.setFont(new Font("Inter", Font.BOLD, 10));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(getText(), (getWidth() - fm.stringWidth(getText())) / 2,
                            (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                    g2.dispose();
                }
            };
            unreadBadge.setPreferredSize(new Dimension(18, 18));
            unreadBadge.setMaximumSize(new Dimension(18, 18));
            unreadBadge.setAlignmentX(RIGHT_ALIGNMENT);

            right.add(timeLabel);
            right.add(Box.createVerticalStrut(4));
            right.add(unreadBadge);

            add(avatar, BorderLayout.WEST);
            add(col,    BorderLayout.CENTER);
            add(right,  BorderLayout.EAST);

            addMouseListener(new MouseAdapter() {
                @Override public void mouseEntered(MouseEvent e) { if (!selected) setBackground(C_HOVER);    }
                @Override public void mouseExited (MouseEvent e) { if (!selected) setBackground(C_SIDEBAR);  }
            });
        }

        public void setSelected(boolean sel) {
            selected = sel;
            setBackground(sel ? C_SELECTED : C_SIDEBAR);
        }

        public void clearUnread() {
            unreadBadge.setText("");
            unreadBadge.repaint();
        }
    }

    /** A single chat bubble. */
    public static class MessageBubble extends JPanel {
        public MessageBubble(String text, boolean out, String time) {
            setOpaque(false);
            setLayout(new FlowLayout(out ? FlowLayout.RIGHT : FlowLayout.LEFT, 14, 0));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
            setAlignmentX(LEFT_ALIGNMENT);

            JPanel bubble = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(out ? C_ACCENT : C_BUBBLE_IN);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                    if (!out) {
                        g2.setColor(C_BORDER);
                        g2.setStroke(new BasicStroke(1));
                        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                    }
                    g2.dispose();
                }
            };
            bubble.setOpaque(false);
            bubble.setLayout(new BoxLayout(bubble, BoxLayout.Y_AXIS));
            bubble.setBorder(new EmptyBorder(8, 13, 8, 13));

            JLabel textLbl = new JLabel("<html><body style='width:200px'>" + text + "</body></html>");
            textLbl.setFont(F_BODY);
            textLbl.setForeground(out ? C_WHITE : C_TEXT);

            JLabel timeLbl = new JLabel(time);
            timeLbl.setFont(new Font("Inter", Font.PLAIN, 10));
            timeLbl.setForeground(out ? new Color(0xBBCCFF) : C_MUTED);
            timeLbl.setAlignmentX(out ? RIGHT_ALIGNMENT : LEFT_ALIGNMENT);

            bubble.add(textLbl);
            bubble.add(Box.createVerticalStrut(3));
            bubble.add(timeLbl);
            add(bubble);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════
    static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        return (parts.length == 1)
                ? parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase()
                : ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
    }

    static String clip(String s, int max) {
        return s.length() > max ? s.substring(0, max) + "…" : s;
    }

    static JLabel avatarLabel(String text, Color color, int size) {
        JLabel lbl = new JLabel(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(C_WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, size / 3));
                FontMetrics fm = g2.getFontMetrics();
                String t = getText();
                g2.drawString(t, (getWidth()  - fm.stringWidth(t)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        lbl.setPreferredSize(new Dimension(size, size));
        lbl.setMinimumSize (new Dimension(size, size));
        lbl.setMaximumSize (new Dimension(size, size));
        return lbl;
    }

    private JButton smallIconBtn(String icon, String tip) {
        JButton btn = new JButton(icon);
        btn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        btn.setForeground(C_MUTED);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(32, 32));
        btn.setToolTipText(tip);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setForeground(C_ACCENT); }
            @Override public void mouseExited (MouseEvent e) { btn.setForeground(C_MUTED);  }
        });
        return btn;
    }

    private void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = messagesScroll.getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    static void styleScrollBar(JScrollBar bar) {
        bar.setPreferredSize(new Dimension(5, 0));
        bar.setUI(new BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                thumbColor = new Color(0xCDD0D9);
                trackColor = new Color(0, 0, 0, 0);
            }
            @Override protected JButton createDecreaseButton(int o) { return zero(); }
            @Override protected JButton createIncreaseButton(int o) { return zero(); }
            JButton zero() { JButton b = new JButton(); b.setPreferredSize(new Dimension(0,0)); return b; }
        });
    }

    /** Simple rounded-rectangle border used for inputs. */
    static class RoundBorder extends AbstractBorder {
        private final Color color; private final int w, arc;
        RoundBorder(Color color, int w, int arc) { this.color = color; this.w = w; this.arc = arc; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color); g2.setStroke(new BasicStroke(w));
            g2.drawRoundRect(x, y, width - 1, height - 1, arc, arc);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(w,w,w,w); }
        @Override public boolean isBorderOpaque() { return false; }
    }
}