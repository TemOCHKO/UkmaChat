package org.temochko.Presentation;


import org.temochko.NetworkLayer.NetworkClient;
import org.temochko.Presentation.LoginFrame;
import org.temochko.Business.Controllers.LoginController;

import javax.swing.*;

public class ClientMain {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

                System.out.println("Client Started");

                String serverIp = "127.0.0.1";
                int serverPort = 8080;
                NetworkClient networkClient = new NetworkClient("127.0.0.1", 8080);

                // Встановлюємо з'єднання та робимо Handshake
                try {
                    networkClient.connect();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null,
                            "Не вдалося підключитися до сервера. Переконайтеся, що сервер запущено.",
                            "Помилка мережі", JOptionPane.ERROR_MESSAGE);
                    return; // Виходимо, якщо немає зв'язку
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
                        "error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}