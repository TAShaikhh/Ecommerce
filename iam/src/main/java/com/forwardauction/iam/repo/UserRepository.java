package com.forwardauction.iam.repo;

import java.sql.SQLException;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long createUser(String username, String passwordHash, String firstName, String lastName, String address) {
        try {
            jdbc.update(
                    "INSERT INTO users(username, password_hash, first_name, last_name, address) VALUES(?,?,?,?,?)",
                    username, passwordHash, firstName, lastName, address
            );
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Username already exists");
        } catch (UncategorizedSQLException e) {
            if (isSqliteUniqueConstraint(e)) {
                throw new IllegalArgumentException("Username already exists");
            }
            throw e;
        }

        Long id = jdbc.queryForObject("SELECT id FROM users WHERE username = ?", Long.class, username);
        return id == null ? -1 : id;
    }

    public void updatePassword(String username, String newPasswordHash) {
        int rows = jdbc.update("UPDATE users SET password_hash = ? WHERE username = ?", newPasswordHash, username);
        if (rows == 0) {
            throw new IllegalArgumentException("User not found");
        }
    }

    public Optional<UserRecord> findByUsername(String username) {
        return jdbc.query(
                        "SELECT id, username, password_hash FROM users WHERE username = ?",
                        (rs, rowNum) -> new UserRecord(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("password_hash")
                        ),
                        username
                )
                .stream()
                .findFirst();
    }

    public Optional<UserProfile> findProfileById(long userId) {
        return jdbc.query(
                        "SELECT id, username, first_name, last_name, address FROM users WHERE id = ?",
                        (rs, rowNum) -> new UserProfile(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("address")
                        ),
                        userId
                )
                .stream()
                .findFirst();
    }

    public Optional<UserProfile> findProfileByUsername(String username) {
        return jdbc.query(
                        "SELECT id, username, first_name, last_name, address FROM users WHERE username = ?",
                        (rs, rowNum) -> new UserProfile(
                                rs.getLong("id"),
                                rs.getString("username"),
                                rs.getString("first_name"),
                                rs.getString("last_name"),
                                rs.getString("address")
                        ),
                        username
                )
                .stream()
                .findFirst();
    }

    private boolean isSqliteUniqueConstraint(UncategorizedSQLException e) {
        Throwable cause = e.getCause();
        if (cause instanceof SQLException sqlEx) {
            String msg = sqlEx.getMessage();
            return msg != null && msg.toLowerCase().contains("unique constraint failed");
        }
        String msg = e.getMessage();
        return msg != null && msg.toLowerCase().contains("unique constraint failed");
    }

    public record UserRecord(long id, String username, String passwordHash) {}

    public record UserProfile(long id, String username, String firstName, String lastName, String address) {}
}
