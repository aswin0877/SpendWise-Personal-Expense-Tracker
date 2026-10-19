package com.spendwise.database;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class DatabaseManager {
    private static final String URL =
        "jdbc:mysql://localhost:3306/spendwise";
    private static final String USER = System.getenv("SPENDWISE_DB_USER");
    private static final String PASSWORD = System.getenv("SPENDWISE_DB_PASSWORD");

    public static Connection getConnection()
            throws SQLException {
        return DriverManager.getConnection(
            URL, USER, PASSWORD
        );
    }
    public static void main(String[] args) {
        try (Connection con = getConnection()) {
            if (con != null) {
                System.out.println(
                    "SpendWise database connected successfully!"
                );
            }
        } catch (SQLException e) {
            System.out.println(
                "Database connection failed!"
            );
            System.out.println(e.getMessage());
        }
    }
}
