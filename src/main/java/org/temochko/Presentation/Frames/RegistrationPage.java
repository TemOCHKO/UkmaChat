package org.temochko.Presentation.Frames;

import javax.swing.*;
import java.awt.*;

import javax.swing.border.*;

/**
 * RegistrationFrame — the sign-up screen for the chat application.
 * Styled to match LoginFrame perfectly.
 * Exposes fields so a RegistrationController can wire up the logic.
 */
public class RegistrationPage extends JFrame {

    // ── palette ───────────────────────────────────────────────────────────────
    static final Color BG_MAIN      = new Color(0xF7F8FA);
    static final Color BG_ACCENT    = new Color(0x1E1F2E);
    static final Color ACCENT_BLUE  = new Color(0x4F80FF);
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

    // ── components (public so controller can access) ──────────────────────────
    public JTextField     usernameField;
    public JTextField     emailField;
    public JPasswordField passwordField;
    public JPasswordField confirmPasswordField;

    public JButton        registerButton;
    public JButton        loginButton; // Button to go back to login

    private JLabel        errorLabel;

    public RegistrationPage() {
        setTitle("Chat — Create Account");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        setSize(450, 560);
        setLocationRelativeTo(null);

        setContentPane(buildFormPanel());
    }


    // ── right form panel ──────────────────────────────────────────────────────
    private JPanel buildFormPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_MAIN);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(320, 480)); // Slightly taller for more fields

        JLabel title = new JLabel("Create Account");
        title.setFont(FONT_TITLE);
        title.setForeground(TEXT_PRIMARY);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitle = new JLabel("Sign up for a new account");
        subtitle.setFont(FONT_LABEL);
        subtitle.setForeground(TEXT_MUTED);
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // error label
        errorLabel = new JLabel(" ");
        errorLabel.setFont(new Font("Inter", Font.PLAIN, 12));
        errorLabel.setForeground(ERROR_COLOR);
        errorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Inputs
        usernameField = styledTextField("your_username");
        emailField = styledTextField("student@ukma.edu.ua");
        passwordField  = styledPasswordField();
        confirmPasswordField = styledPasswordField();

        // primary button
        registerButton = new JButton("Sign up") {
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
        styleButton(registerButton, true);

        // divider
        JPanel divider = buildDivider("or");

        // secondary button
        loginButton = new JButton("Sign in instead") {
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
        styleButton(loginButton, false);

        // assemble (tightened struts for 4 fields)
        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(subtitle);
        form.add(Box.createVerticalStrut(10));
        form.add(errorLabel);

        form.add(fieldLabel("Username"));
        form.add(Box.createVerticalStrut(4));
        form.add(usernameField);
        form.add(Box.createVerticalStrut(10));

        form.add(fieldLabel("Email"));
        form.add(Box.createVerticalStrut(4));
        form.add(emailField);
        form.add(Box.createVerticalStrut(10));

        form.add(fieldLabel("Password"));
        form.add(Box.createVerticalStrut(4));
        form.add(passwordField);
        form.add(Box.createVerticalStrut(10));

        form.add(fieldLabel("Confirm Password"));
        form.add(Box.createVerticalStrut(4));
        form.add(confirmPasswordField);

        form.add(Box.createVerticalStrut(18));
        form.add(registerButton);
        form.add(Box.createVerticalStrut(12));
        form.add(divider);
        form.add(Box.createVerticalStrut(12));
        form.add(loginButton);

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
                new EmptyBorder(7, 12, 7, 12))); // slightly smaller padding
        tf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
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
                new EmptyBorder(7, 12, 7, 12))); // slightly smaller padding
        pf.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pf.setAlignmentX(Component.LEFT_ALIGNMENT);
        return pf;
    }

    private void styleButton(JButton btn, boolean primary) {
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setPreferredSize(new Dimension(Integer.MAX_VALUE, 42));
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

    public void setError(String message) {
        errorLabel.setText(message == null || message.isEmpty() ? " " : message);
    }

    public void setLoading(boolean loading) {
        registerButton.setEnabled(!loading);
        registerButton.setText(loading ? "Creating account…" : "Sign up");
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