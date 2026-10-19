package com.spendwise.model;
import java.time.LocalDate;
public abstract class Transaction {
    private int transactionId;
    private double amount;
    private String category;
    private LocalDate transactionDate;
    private String description;
    public Transaction(double amount, String category,
                       LocalDate transactionDate,
                       String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Amount must be greater than zero"
            );
        }
        this.amount = amount;
        this.category = category;
        this.transactionDate = transactionDate;
        this.description = description;
    }
    public int getTransactionId() {
        return transactionId;
    }
    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }
    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException(
                "Amount must be greater than zero"
            );
        }

        this.amount = amount;
    }
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }
    public LocalDate getTransactionDate() {
        return transactionDate;
    }
    public void setTransactionDate(LocalDate transactionDate) {
        this.transactionDate = transactionDate;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public abstract String getTransactionType();
}
