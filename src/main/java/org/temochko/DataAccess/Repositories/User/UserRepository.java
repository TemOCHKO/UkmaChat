package org.temochko.DataAccess.Repositories.User;

import org.temochko.DataAccess.DatabaseManager;
import org.temochko.DataAccess.Models.User;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class UserRepository implements IUserRepository {
    private final DatabaseManager db;

    public UserRepository(DatabaseManager db) {
        this.db = db;
    }

    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, email, online, last_seen FROM users WHERE username = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                //if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT id, username, email, online, last_seen FROM users WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                //if (rs.next()) return Optional.of(map(rs));
            }
        }
        return Optional.empty();
    }

    public Optional<String> findPasswordHash(String username) throws SQLException {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getString("password"));
            }
        }
        return Optional.empty();
    }

    public User save(String username, String passwordHash, String email) throws SQLException {
        String sql = "INSERT INTO users (username, email, password) VALUES (?,?,?) RETURNING id";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, email);
            ps.setString(3, passwordHash);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), username, email);
                }
            }
        }
        throw new SQLException("User insert returned no id");
    }

    public void setOnline(int userId, boolean online) throws SQLException {
        String sql = "UPDATE users SET online = ?, last_seen = ? WHERE id = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, online);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void setOnline(String username, boolean online) throws SQLException {
        String sql = "UPDATE users SET online = ?, last_seen = ? WHERE username = ?";
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, online);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(3, username);
            ps.executeUpdate();
        }
    }

    public List<User> findAllExcept(int excludeId) throws SQLException {
        String sql = "SELECT id, username, email, online, last_seen FROM users WHERE id <> ?";
        List<User> list = new ArrayList<>();
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, excludeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

   private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setEmail(rs.getString("email"));
        u.setOnline(rs.getBoolean("online"));
        Timestamp ts = rs.getTimestamp("last_seen");
        if (ts != null) u.setLastSeen(ts.toLocalDateTime());
        return u;
    }

    public List<User> searchByUsername(String query) throws SQLException {
        String sql = "SELECT id, username, email, online, last_seen FROM users WHERE username ILIKE ?";
        List<User> list = new ArrayList<>();

        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, "%" + query + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(map(rs));
                }
            }
        }
        return list;
    }

    public boolean isUserOnline(String username) throws SQLException {
        String sql = "SELECT online FROM users WHERE username = ?";

        // Using try-with-resources to automatically close the connection and statement
        try (Connection c = db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Returns the boolean value from the 'online' column
                    return rs.getBoolean("online");
                }
            }
        }
        // Default to false if the user is not found in the database
        return false;
    }
}