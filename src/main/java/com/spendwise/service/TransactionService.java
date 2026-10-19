package com.spendwise.service;
import com.spendwise.database.DatabaseManager;
import com.spendwise.model.Transaction;
import com.spendwise.model.Income;
import com.spendwise.model.Expense;
import com.spendwise.model.TransactionOperations;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class TransactionService
        implements TransactionOperations {

    private final int userId;

    public TransactionService(int userId) {
        this.userId = userId;
    }

    @Override
    public void addTransaction(Transaction transaction)
            throws SQLException {

        String sql = "INSERT INTO transactions "
                + "(user_id, amount, type, category, "
                + "transaction_date, description) "
                + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, userId);
            ps.setDouble(2, transaction.getAmount());
            ps.setString(3, transaction.getTransactionType());
            ps.setString(4, transaction.getCategory());
            ps.setDate(5, Date.valueOf(
                    transaction.getTransactionDate()));
            ps.setString(6, transaction.getDescription());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    transaction.setTransactionId(
                            keys.getInt(1));
                }
            }
        }
    }

    @Override
    public List<Transaction> viewTransactions()
            throws SQLException {

        List<Transaction> transactions = new ArrayList<>();

        String sql = "SELECT * FROM transactions "
                + "WHERE user_id = ? "
                + "ORDER BY transaction_date DESC";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    String type = rs.getString("type");

                    double amount = rs.getDouble("amount");
                    String category = rs.getString("category");
                    java.time.LocalDate date =
                            rs.getDate("transaction_date")
                                    .toLocalDate();
                    String description =
                            rs.getString("description");

                    Transaction transaction;

                    if ("INCOME".equalsIgnoreCase(type)) {
                        transaction = new Income(
                                amount, category, date, description);
                    } else if ("EXPENSE".equalsIgnoreCase(type)) {
                        transaction = new Expense(
                                amount, category, date, description);
                    } else {
                        throw new SQLException(
                                "Unknown transaction type: " + type);
                    }

                    transaction.setTransactionId(
                            rs.getInt("transaction_id"));

                    transactions.add(transaction);
                }
            }
        }

        return transactions;
    }

    @Override
    public void updateTransaction(Transaction transaction)
            throws SQLException {

        String sql = "UPDATE transactions SET "
                + "amount = ?, type = ?, category = ?, "
                + "transaction_date = ?, description = ? "
                + "WHERE transaction_id = ? AND user_id = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, transaction.getAmount());
            ps.setString(2, transaction.getTransactionType());
            ps.setString(3, transaction.getCategory());
            ps.setDate(4, Date.valueOf(
                    transaction.getTransactionDate()));
            ps.setString(5, transaction.getDescription());
            ps.setInt(6, transaction.getTransactionId());
            ps.setInt(7, userId);

            ps.executeUpdate();
        }
    }

    @Override
    public void deleteTransaction(int transactionId)
            throws SQLException {

        String sql = "DELETE FROM transactions "
                + "WHERE transaction_id = ? AND user_id = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, transactionId);
            ps.setInt(2, userId);

            ps.executeUpdate();
        }
    }
}
