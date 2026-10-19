package com.spendwise.model;
import java.time.LocalDate;
public class Income extends Transaction {
    public Income(double amount, String category,
                  LocalDate transactionDate,
                  String description) {
        super(amount, category, transactionDate, description);
    }
    @Override
    public String getTransactionType() {
        return "INCOME";
    }
}