package org.temochko.DataAccess;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Data Access Layer — lowest tier.
 * Manages JDBC connections to PostgreSQL.
 */
public class DatabaseManager {
    private static final Logger LOG = Logger.getLogger(DatabaseManager.class.getName());

    private static DatabaseManager instance;
    private final String url;
    private final String user;
    private final String password;

    private DatabaseManager(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("PostgreSQL driver not found", e);
        }
    }

    /** Initialize from .env or system environment variables */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

            String url  = dotenv.get("DB_URL");
            String user = dotenv.get("DB_USER");
            String pass = dotenv.get("DB_PASSWORD");

            if (url == null || user == null || pass == null) {
                throw new IllegalStateException("Database credentials are not fully set in the environment.");
            }

            instance = new DatabaseManager(url, user, pass);
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

     /**
     * Run once at startup to create tables if they don't exist.
     */
    public void initSchema() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id       SERIAL PRIMARY KEY,
                username VARCHAR(50)  NOT NULL UNIQUE,
                email    VARCHAR(100) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                online   BOOLEAN      NOT NULL DEFAULT FALSE,
                last_seen TIMESTAMP
            );

            CREATE TABLE IF NOT EXISTS messages (
                id          SERIAL PRIMARY KEY,
                sender_id   INT NOT NULL REFERENCES users(id),
                receiver_id INT NOT NULL REFERENCES users(id),
                content     TEXT NOT NULL,
                sent_at     TIMESTAMP NOT NULL DEFAULT NOW()
            );
            """;

        try (Connection conn = getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
            LOG.info("Database schema ready.");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialise schema: " + e.getMessage(), e);
        }
    }
}