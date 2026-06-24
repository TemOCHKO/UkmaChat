package org.temochko.Business.Controllers;

import org.temochko.Business.DTOs.LoginResponseDto;
import org.temochko.Business.DTOs.RegisterResponseDto;
import org.temochko.NetworkLayer.NetworkClient;
import org.temochko.Presentation.LoginFrame;
import org.temochko.Presentation.MainFrame;
import org.temochko.Presentation.RegistrationPage;

import javax.swing.*;
import java.awt.event.*;

/**
 * LoginController — handles all interaction on LoginFrame.
 */
public class LoginController {

    private final LoginFrame view;
    private final RegistrationPage registrationPage;
    private final NetworkClient networkClient; // Тепер контролер працює з мережею, а не напряму з БД

    public LoginController(LoginFrame view, RegistrationPage registrationPage, NetworkClient networkClient) {
        this.view = view;
        this.registrationPage = registrationPage;
        this.networkClient = networkClient;
        wireEvents();
    }


    private void onLogin() {
        registrationPage.dispose();
        view.setVisible(true);
        String username = view.usernameField.getText().trim();
        String password = new String(view.passwordField.getPassword());

        view.setError("");

        if (username.isEmpty()) { view.setError("Please enter your username."); return; }
        if (password.isEmpty()) { view.setError("Please enter your password.");  return; }

        view.setLoading(true);

        SwingWorker<LoginResponseDto, Void> worker = new SwingWorker<>() {
            @Override
            protected LoginResponseDto doInBackground() throws Exception {
                return networkClient.sendLoginRequest(username, password);
            }

            @Override
            protected void done() {
                view.setLoading(false);
                try {
                    LoginResponseDto response = get();
                    handleAuthResult(response, username);
                } catch (Exception ex) {
                    view.setError("Не вдалося з'єднатися з сервером.");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void handleAuthResult(LoginResponseDto response, String username) {
        if (response.success) {
            view.dispose();
            registrationPage.dispose();
            MainFrame main = new MainFrame(username);
            new MainController(main);
            main.setVisible(true);
        } else {
            view.setError(response.message);
        }
    }

    private void wireEvents() {
        view.loginButton.addActionListener(e -> onLogin());
        view.registerButton.addActionListener(e -> onRegister());

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

        registrationPage.loginButton.addActionListener(e -> onLogin());
        registrationPage.registerButton.addActionListener(e -> onRegister());
    }

    private void onRegister() {
        view.dispose();
        registrationPage.setVisible(true);

        String username = registrationPage.usernameField.getText().trim();
        String password = new String(registrationPage.passwordField.getPassword());
        String confirmPassword = new String(registrationPage.confirmPasswordField.getPassword());
        String email = new String(registrationPage.emailField.getText());

        registrationPage.setError("");

        if (username.isEmpty()) {
            registrationPage.setError("Please enter your username.");
            return;
        }
        if (password.isEmpty()) { registrationPage.setError("Please enter your password.");  return; }
        if (confirmPassword.isEmpty()) { registrationPage.setError("Please enter your confirmed password.");  return; }
        if (email.isEmpty()) { registrationPage.setError("Please enter your email.");  return; }

        if (!password.equals(confirmPassword)) {
            registrationPage.setError("Passwords do not match.");
            return;
        }

        registrationPage.setLoading(true);

        SwingWorker<RegisterResponseDto, Void> worker = new SwingWorker<>() {
            @Override
            protected RegisterResponseDto doInBackground() throws Exception {
                return networkClient.sendRegisterRequest(username, password, email);
            }

            @Override
            protected void done() {
                view.setLoading(false);
                try {
                    RegisterResponseDto response = get();
                    handleAuthResult(new LoginResponseDto(response.success, response.message, response.sessionToken), username);
                } catch (Exception ex) {
                    view.setError("Couldnt connect to server.");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();


    }

    private void onForgotPassword() {
        JOptionPane.showMessageDialog(view,
                "Password reset flow — wire to your reset screen here.",
                "Forgot password", JOptionPane.INFORMATION_MESSAGE);
    }
}