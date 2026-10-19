
package com.spendwise.service;

import com.spendwise.database.DatabaseManager;
import com.spendwise.util.PasswordUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public class AuthService {

    public int register(
            String name, String email, char[] password)
            throws SQLException {

        name = name.trim();
        email = email.trim().toLowerCase(Locale.ROOT);

        if (name.isEmpty() || email.isEmpty()) {
            throw new IllegalArgumentException(
                    "Name and email are required.");
        }

        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException(
                    "Enter a valid email address.");
        }

        if (password == null || password.length < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters.");
        }

        String passwordHash = PasswordUtil.hashPassword(password);

        String sql = "INSERT INTO users (name, email, password_hash) "
                + "VALUES (?, ?, ?)";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, passwordHash);

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }

        throw new SQLException(
                "Registration failed: no user ID was returned.");
    }

    public int login(String email, char[] password)
            throws SQLException {

        if (email == null || email.isBlank() || password == null) {
            return -1;
        }

        String sql = "SELECT user_id, password_hash "
                + "FROM users WHERE email = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(
                    1, email.trim().toLowerCase(Locale.ROOT));

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    String storedHash =
                            rs.getString("password_hash");

                    if (PasswordUtil.verifyPassword(
                            password, storedHash)) {
                        return rs.getInt("user_id");
                    }
                }
            }
        }

        // Same result for an unknown email or wrong password.
        return -1;
    }
}