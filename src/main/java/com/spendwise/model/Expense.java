package com.spendwise.model;
import java.time.LocalDate;
public class Expense extends Transaction {
    public Expense(double amount, String category,
                   LocalDate transactionDate,
                   String description) {

        super(amount, category, transactionDate, description);
    }
    @Override
    public String getTransactionType() {
        return "EXPENSE";
    }
}