
package com.spendwise.service;

import com.spendwise.database.DatabaseManager;
import com.spendwise.model.Budget;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;

public class BudgetService {

    public void saveBudget(Budget budget)
            throws SQLException {

        String sql =
                "INSERT INTO budgets "
                + "(user_id, budget_month, budget_year, limit_amount) "
                + "VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE "
                + "limit_amount = VALUES(limit_amount)";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, budget.getUserId());
            ps.setInt(2, budget.getMonth());
            ps.setInt(3, budget.getYear());
            ps.setDouble(4, budget.getLimitAmount());

            ps.executeUpdate();
        }
    }

    public double getBudget(int userId, int month, int year)
            throws SQLException {

        String sql =
                "SELECT limit_amount FROM budgets "
                + "WHERE user_id = ? "
                + "AND budget_month = ? AND budget_year = ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setInt(2, month);
            ps.setInt(3, year);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("limit_amount");
                }
            }
        }

        return 0;
    }

    public double getMonthlyExpenses(
            int userId, int month, int year)
            throws SQLException {

        YearMonth period = YearMonth.of(year, month);

        String sql =
                "SELECT COALESCE(SUM(amount), 0) AS total "
                + "FROM transactions "
                + "WHERE user_id = ? AND type = 'EXPENSE' "
                + "AND transaction_date >= ? "
                + "AND transaction_date < ?";

        try (Connection con = DatabaseManager.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ps.setDate(2, java.sql.Date.valueOf(
                    period.atDay(1)));
            ps.setDate(3, java.sql.Date.valueOf(
                    period.plusMonths(1).atDay(1)));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        }

        return 0;
    }

    public double getRemainingBudget(
            int userId, int month, int year)
            throws SQLException {

        double budget = getBudget(userId, month, year);
        double expenses =
                getMonthlyExpenses(userId, month, year);

        return budget - expenses;
    }
}
