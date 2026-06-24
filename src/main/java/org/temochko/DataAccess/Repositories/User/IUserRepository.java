package org.temochko.DataAccess.Repositories.User;

import org.temochko.DataAccess.Models.User;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface IUserRepository {
    Optional<User> findByUsername(String username) throws SQLException;
    Optional<User> findById(int id) throws SQLException;
    Optional<String> findPasswordHash(String username) throws SQLException;
    User save(String username, String email, String passwordHash) throws SQLException;
    void setOnline(int userId, boolean online) throws SQLException;
    void setOnline(String username, boolean online) throws SQLException;
    List<User> findAllExcept(int excludeId) throws SQLException;
    List<User> searchByUsername(String query) throws SQLException;
}
