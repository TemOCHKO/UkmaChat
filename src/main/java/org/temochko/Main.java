package org.temochko;

import org.temochko.Business.Controllers.LoginController;
import org.temochko.DataAccess.DatabaseManager;
import org.temochko.Presentation.LoginFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting Chat Server...");

        // 1. Initialize the database schema
        DatabaseManager dbManager = DatabaseManager.getInstance();
        dbManager.initSchema();

        System.out.println("init schema.");

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            LoginFrame login = new LoginFrame();
            new LoginController(login);
            login.setVisible(true);
        });
    }
}