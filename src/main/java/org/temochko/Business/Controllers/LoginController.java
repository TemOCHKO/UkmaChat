package org.temochko.Business.Controllers;

import org.temochko.Presentation.LoginFrame;
import org.temochko.Presentation.MainFrame;

import javax.swing.*;
import java.awt.event.*;

/**
 * LoginController — handles all interaction on LoginFrame.
 */
public class LoginController {

    private final LoginFrame view;
    // private final AuthService authService;

    public LoginController(LoginFrame view /*, AuthService authService */) {
        this.view = view;
        // this.authService = authService;
        wireEvents();
    }

    private void wireEvents() {
        view.loginButton.addActionListener(e -> onLogin());
        view.registerButton.addActionListener(e -> onRegister());

        // Enter key from either field triggers login
        KeyAdapter enterLogin = new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) onLogin();
            }
        };
        view.usernameField.addKeyListener(enterLogin);
        view.passwordField.addKeyListener(enterLogin);

        view.forgotPasswordLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { onForgotPassword(); }
        });
    }

    private void onLogin() {
        String username = view.usernameField.getText().trim();
        String password = new String(view.passwordField.getPassword());

        view.setError("");

        if (username.isEmpty()) { view.setError("Please enter your username."); return; }
        if (password.isEmpty()) { view.setError("Please enter your password.");  return; }

        view.setLoading(true);

        // Replace this Timer with a real async auth call:
        // authService.login(username, password, this::handleAuthResult);
        new Timer(600, e -> {
            ((Timer) e.getSource()).stop();
            handleAuthResult(username, password);
        }).start();
    }

    private void handleAuthResult(String username, String password) {
        view.setLoading(false);

        // Stub: accept anything non-empty — replace with real check
        boolean success = !username.isEmpty() && !password.isEmpty();

        if (success) {
            view.dispose();
            SwingUtilities.invokeLater(() -> {
                MainFrame main = new MainFrame(username);
                new MainController(main);
                main.setVisible(true);
            });
        } else {
            view.setError("Incorrect username or password.");
        }
    }

    private void onRegister() {
        JOptionPane.showMessageDialog(view,
                "Registration flow — wire to your registration screen here.",
                "Create account", JOptionPane.INFORMATION_MESSAGE);
    }

    private void onForgotPassword() {
        JOptionPane.showMessageDialog(view,
                "Password reset flow — wire to your reset screen here.",
                "Forgot password", JOptionPane.INFORMATION_MESSAGE);
    }
}