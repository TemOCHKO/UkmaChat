package org.temochko.Business.Controllers;

import org.temochko.Business.DTOs.Login.LoginResponseDto;
import org.temochko.Business.DTOs.Register.RegisterResponseDto;
import org.temochko.NetworkLayer.ClientSide.NetworkClient;
import org.temochko.Presentation.ChatFrame;
import org.temochko.Presentation.LoginFrame;
import org.temochko.Presentation.RegistrationPage;

import javax.swing.*;
import java.awt.event.*;

public class LoginController {

    private final LoginFrame view;
    private final RegistrationPage registrationPage;
    private final NetworkClient networkClient;

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
                    view.setError("Couldnt connect to the server.");
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }

    private void handleAuthResult(LoginResponseDto response, String username) throws Exception {
        if (response.success) {
            view.dispose();
            registrationPage.dispose();

            ChatFrame chatFrame = new ChatFrame(username);
            new MainController(chatFrame, networkClient);
            chatFrame.setVisible(true);
            networkClient.sendSetOnlineRequest(username, true);
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
}