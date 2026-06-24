package org.temochko.DataAccess.Repositories;

import org.temochko.DataAccess.DatabaseManager;
import org.temochko.DataAccess.Models.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository layer — all SQL for messages lives here.
 */
public class MessageRepository {
    private final DatabaseManager db;

    public MessageRepository(DatabaseManager db) {
        this.db = db;
    }

    public Message save(int senderId, int receiverId, String content) throws SQLException {
        String sql = """
            INSERT INTO messages (sender_id, receiver_id, content, sent_at)
            VALUES (?, ?, ?, NOW())
            RETURNING id, sent_at
            """;
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, content);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Message m = new Message();
                    m.setId(rs.getInt("id"));
                    m.setSenderId(senderId);
                    m.setReceiverId(receiverId);
                    m.setContent(content);
                    m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                    return m;
                }
            }
        }
        throw new SQLException("Message insert returned no id");
    }

    /**
     * Retrieve conversation between two users, ordered oldest-first, last 100.
     */
    public List<Message> findConversation(int userA, int userB) throws SQLException {
        String sql = """
            SELECT m.id, m.sender_id, u.username AS sender_username,
                   m.receiver_id, m.content, m.sent_at
            FROM messages m
            JOIN users u ON u.id = m.sender_id
            WHERE (m.sender_id = ? AND m.receiver_id = ?)
               OR (m.sender_id = ? AND m.receiver_id = ?)
            ORDER BY m.sent_at ASC
            LIMIT 100
            """;
        List<Message> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userA); ps.setInt(2, userB);
            ps.setInt(3, userB); ps.setInt(4, userA);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Message m = new Message();
                    m.setId(rs.getInt("id"));
                    m.setSenderId(rs.getInt("sender_id"));
                    m.setSenderUsername(rs.getString("sender_username"));
                    m.setReceiverId(rs.getInt("receiver_id"));
                    m.setContent(rs.getString("content"));
                    m.setSentAt(rs.getTimestamp("sent_at").toLocalDateTime());
                    list.add(m);
                }
            }
        }
        return list;
    }
}