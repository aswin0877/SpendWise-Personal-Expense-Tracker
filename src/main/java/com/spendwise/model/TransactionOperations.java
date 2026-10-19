package com.spendwise.model;
import java.sql.SQLException;
import java.util.List;
public interface TransactionOperations {
    void addTransaction(Transaction transaction)
                throws SQLException;
    List<Transaction> viewTransactions()
            throws SQLException;
    void updateTransaction(Transaction transaction)
            throws SQLException;
    void deleteTransaction(int transactionId)
            throws SQLException;
}
