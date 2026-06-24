package org.temochko.Presentation;


import org.temochko.NetworkLayer.ClientSide.NetworkClient;
import org.temochko.Business.Controllers.LoginController;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                String serverIp = "127.0.0.1";
                int serverPort = 8080;
                NetworkClient networkClient = new NetworkClient("127.0.0.1", 8080);

                try {
                    networkClient.connect();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Couldnt connect to server. Please try again.",
                            "Network Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                LoginFrame loginFrame = new LoginFrame();
                RegistrationPage registrationPage = new RegistrationPage();
                LoginController loginController = new LoginController(loginFrame, registrationPage, networkClient);

                loginFrame.setVisible(true);
            } catch (Exception e) {
                System.err.println("Client Error: " + e.getMessage());
                e.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        "App couldnt start" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}