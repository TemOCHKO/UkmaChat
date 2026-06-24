package org.temochko.Presentation;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;

/**
 * LoginFrame — the entry screen for the chat application.
 * Follows a clean two-panel layout: left accent panel + right form panel.
 * All user actions are wired via LoginController.
 */
public class LoginFrame extends JFrame {

    // ── palette ───────────────────────────────────────────────────────────────
    static final Color BG_MAIN      = new Color(0xF7F8FA);
    static final Color BG_ACCENT    = new Color(0x1E1F2E);   // deep navy
    static final Color ACCENT_BLUE  = new Color(0x4F80FF);   // brand blue
    static final Color TEXT_PRIMARY = new Color(0x1A1B2E);
    static final Color TEXT_MUTED   = new Color(0x8A8FA3);
    static final Color BORDER_COLOR = new Color(0xE2E5EC);
    static final Color INPUT_BG     = new Color(0xFFFFFF);
    static final Color ERROR_COLOR  = new Color(0xE05260);

    // ── fonts ─────────────────────────────────────────────────────────────────
    static final Font FONT_TITLE  = new Font("Inter",         Font.BOLD,   26);
    static final Font FONT_LABEL  = new Font("Inter",         Font.PLAIN,  13);
    static final Font FONT_INPUT  = new Font("Monospaced",    Font.PLAIN,  14);
    static final Font FONT_BTN    = new Font("Inter",         Font.BOLD,   14);
    static final Font FONT_LINK   = new Font("Inter",         Font.PLAIN,  13);

    // ── components (package-private so controller can access) ─────────────────
    public JTextField     usernameField;
    public JPasswordField passwordField;
    public JButton        loginButton;
    public JButton        registerButton;
    JCheckBox      rememberMeBox;
    JLabel         errorLabel;
    public JLabel         forgotPasswordLink;

    public LoginFrame() {
        setTitle("UkmaChat — Sign in");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(860, 540);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new GridLayout(1, 2));
        root.add(buildAccentPanel());
        root.add(buildFormPanel());
        setContentPane(root);
    }

    // ── left decorative panel ─────────────────────────────────────────────────
    private JPanel buildAccentPanel() {
        JPanel panel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // gradient backdrop
                GradientPaint gp = new GradientPaint(
                        0, 0,               new Color(0x1E1F2E),
                        getWidth(), getHeight(), new Color(0x2A2D50));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // decorative circles
                g2.setColor(new Color(0x4F80FF, false));
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
                g2.fillOval(-60, -60, 280, 280);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
                g2.fillOval(getWidth() - 140, getHeight() - 160, 260, 260);
                g2.dispose();
            }
        };
        panel.setLayout(new GridBagLayout());
        panel.setPreferredSize(new Dimension(360, 540));

        JPanel inner = new JPanel();
        inner.setOpaque(false);
        inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));

        // logo bubble
        JPanel logoBubble = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(ACCENT_BLUE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Inter", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String ch = "C";
                g2.drawString(ch,
                        (getWidth() - fm.stringWidth(ch)) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        logoBubble.setOpaque(false);
        logoBubble.setPreferredSize(new Dimension(52, 52));
        logoBubble.setMaximumSize(new Dimension(52, 52));
        logoBubble.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel appName = new JLabel("Chatter");
        appName.setFont(new Font("Inter", Font.BOLD, 28));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel tagline = new JLabel("<html>Messages that<br>feel like presence.</html>");
        tagline.setFont(new Font("Inter", Font.PLAIN, 15));
        tagline.setForeground(new Color(0xB0B8D4));
        tagline.setAlignmentX(Component.LEFT_ALIGNMENT);

        inner.add(logoBubble);
        inner.add(Box.createVerticalStrut(18));
        inner.add(appName);
        inner.add(Box.createVerticalStrut(14));
        inner.add(tagline);

        panel.add(inner);
        return panel;
    }

    // ── right form panel ──────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_MAIN);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(320, 420));

        JLabel title = new JLabel("Welcome back");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign in to your account");
        subtitle.setFont(FONT_LABEL);
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // error label (hidden by default)
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // username
        JLabel userLabel = fieldLabel("Username");
        usernameField = styledTextField("your_username");

        // password
        JLabel passLabel = fieldLabel("Password");
        passwordField  = styledPasswordField();

        // remember + forgot
        JPanel rememberRow = new JPanel(new BorderLayout());
        rememberRow.setOpaque(false);
        rememberRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        rememberMeBox = new JCheckBox("Remember me");
        rememberMeBox.setFont(FONT_LINK);
        rememberMeBox.setForeground(TEXT_MUTED);
        rememberMeBox.setOpaque(false);
        rememberMeBox.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        forgotPasswordLink = new JLabel("Forgot password?");
        forgotPasswordLink.setFont(FONT_LINK);
        forgotPasswordLink.setForeground(ACCENT_BLUE);
        forgotPasswordLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        rememberRow.add(rememberMeBox,      BorderLayout.WEST);
        rememberRow.add(forgotPasswordLink, BorderLayout.EAST);

        // primary button
        loginButton = new JButton("Sign in") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isPressed()
                        ? ACCENT_BLUE.darker()
                        : (getModel().isRollover() ? ACCENT_BLUE.brighter() : ACCENT_BLUE));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(FONT_BTN);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        styleButton(loginButton, true);

        // divider
        JPanel divider = buildDivider("or");

        // register button
        registerButton = new JButton("Create an account") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(INPUT_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(BORDER_COLOR);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(TEXT_PRIMARY);
                g2.setFont(FONT_BTN);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(getText(),
                        (getWidth() - fm.stringWidth(getText())) / 2,
                        (getHeight() + fm.getAscent() - fm.getDescent()) / 2);
                g2.dispose();
            }
        };
        styleButton(registerButton, false);

        // assemble
        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(20));
        form.add(errorLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(userLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(14));
        form.add(passLabel);
        form.add(Box.createVerticalStrut(6));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(10));
        form.add(rememberRow);
        form.add(Box.createVerticalStrut(20));
        form.add(loginButton);
        form.add(Box.createVerticalStrut(16));
        form.add(divider);
        form.add(Box.createVerticalStrut(16));
        form.add(registerButton);

        panel.add(form);
        return panel;
    }

    // ── helpers ───────────────────────────────────────────────────────────────
    private JLabel fieldLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Inter", Font.BOLD, 12));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JTextField styledTextField(String placeholder) {
        JTextField tf = new JTextField() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (getText().isEmpty() && !isFocusOwner()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setColor(TEXT_MUTED);
                    g2.setFont(getFont().deriveFont(Font.ITALIC));
                    Insets ins = getInsets();
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString(placeholder, ins.left + 2,
                            ins.top + fm.getAscent());
                    g2.dispose();
                }
            }
        };
        tf.setFont(FONT_INPUT);
        tf.setBackground(INPUT_BG);
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(ACCENT_BLUE);
        tf.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER_COLOR, 1, 9),
                new EmptyBorder(9, 12, 9, 12)));
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        tf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return tf;
    }

    private JPasswordField styledPasswordField() {
        JPasswordField pf = new JPasswordField();
        pf.setFont(FONT_INPUT);
        pf.setBackground(INPUT_BG);
        pf.setForeground(TEXT_PRIMARY);
        pf.setCaretColor(ACCENT_BLUE);
        pf.setBorder(new CompoundBorder(
                new RoundedBorder(BORDER_COLOR, 1, 9),
                new EmptyBorder(9, 12, 9, 12)));
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }

    private void styleButton(JButton btn, boolean primary) {
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private JPanel buildDivider(String label) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        p.setAlignmentX(Component.LEFT_ALIGNMENT);

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1; p.add(new JSeparator(), c);
        c.fill = GridBagConstraints.NONE; c.weightx = 0;
        JLabel lbl = new JLabel("  " + label + "  ");
        lbl.setFont(FONT_LINK);
        lbl.setForeground(TEXT_MUTED);
        p.add(lbl, c);
        c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        p.add(new JSeparator(), c);
        return p;
    }

    /** Sets an error message below the subtitle. Empty string clears it. */
    public void setError(String message) {
        errorLabel.setText(message == null || message.isEmpty() ? " " : message);
    }

    /** Enables or disables the login button (use during async auth). */
    public void setLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        loginButton.setText(loading ? "Signing in…" : "Sign in");
    }

    // ── inner: rounded border used by inputs ──────────────────────────────────
    static class RoundedBorder extends AbstractBorder {
        private final Color color;
        private final int   thickness;
        private final int   arc;
        RoundedBorder(Color color, int thickness, int arc) {
            this.color = color; this.thickness = thickness; this.arc = arc;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(thickness));
            g2.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(thickness, thickness, thickness, thickness); }
        @Override public boolean isBorderOpaque() { return false; }
    }
}