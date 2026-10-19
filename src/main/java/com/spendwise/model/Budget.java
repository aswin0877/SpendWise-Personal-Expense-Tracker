
package com.spendwise.model;

public class Budget {

    private int budgetId;
    private int userId;
    private int month;
    private int year;
    private double limitAmount;

    public Budget(int userId, int month, int year,
                  double limitAmount) {

        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Month must be between 1 and 12.");
        }

        if (limitAmount <= 0) {
            throw new IllegalArgumentException(
                    "Budget must be greater than zero.");
        }

        this.userId = userId;
        this.month = month;
        this.year = year;
        this.limitAmount = limitAmount;
    }

    public int getBudgetId() {
        return budgetId;
    }

    public void setBudgetId(int budgetId) {
        this.budgetId = budgetId;
    }

    public int getUserId() {
        return userId;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public double getLimitAmount() {
        return limitAmount;
    }

    public void setLimitAmount(double limitAmount) {
        if (limitAmount <= 0) {
            throw new IllegalArgumentException(
                    "Budget must be greater than zero.");
        }

        this.limitAmount = limitAmount;
    }
}
