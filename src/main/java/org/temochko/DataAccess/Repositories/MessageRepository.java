package org.temochko.DataAccess.Repositories;

import org.temochko.Business.DTOs.Message.ChatMessage;
import org.temochko.DataAccess.DatabaseManager;
import org.temochko.DataAccess.Models.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private final DatabaseManager db = DatabaseManager.getInstance();

    public void saveMessage(ChatMessage msg) throws SQLException {
        String sql = "INSERT INTO messages (sender_name, receiver_name, content, sent_at) VALUES (?, ?, ?, ?)";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, msg.from);
            ps.setString(2, msg.username);
            ps.setString(3, msg.message);
            ps.setTimestamp(4, java.sql.Timestamp.valueOf(java.time.LocalDateTime.now()));

            ps.executeUpdate();
        }
    }

    public List<ChatMessage> getChatHistory(String user1, String user2) throws SQLException {
        List<ChatMessage> history = new ArrayList<>();

        String sql = "SELECT * FROM messages WHERE " +
                "(sender_name = ? AND receiver_name = ?) OR " +
                "(sender_name = ? AND receiver_name = ?) " +
                "ORDER BY sent_at ASC";

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, user1);
            ps.setString(2, user2);
            ps.setString(3, user2);
            ps.setString(4, user1);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String msgFrom = rs.getString("sender_name");
                    String msgUsername = rs.getString("receiver_name");
                    String msgMessage = rs.getString("content");
                    String msgTimestamp = String.valueOf(rs.getTimestamp("sent_at").toLocalDateTime());

                    ChatMessage msg = new ChatMessage(msgUsername, msgFrom, msgMessage, msgTimestamp);

                    history.add(msg);
                }
            }
        }
        return history;
    }
}