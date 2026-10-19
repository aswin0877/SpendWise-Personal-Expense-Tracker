
package com.spendwise.gui;

import com.spendwise.model.Transaction;
import com.spendwise.model.Income;
import com.spendwise.model.Expense;
import com.spendwise.service.TransactionService;
import com.spendwise.model.Budget;
import com.spendwise.service.BudgetService;
import java.time.YearMonth;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.TableModel;
import javax.swing.RowFilter;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class DashboardFrame extends JFrame {
    private final TransactionService service;
    private final int userId;
    private TableRowSorter<TableModel> sorter;
    private final BudgetService budgetService = new BudgetService();
    private JTextField searchField;
    private JComboBox<String> typeFilter;
    private JButton searchButton;
    private JButton resetFilterButton;
    private JTextField fromDateField;
    private JTextField toDateField;
    private JButton dateFilterButton;
    
    private JLabel budgetLabel;
    private JLabel spentLabel;
    private JLabel remainingLabel;

    private JProgressBar budgetProgressBar;

    private JLabel balanceLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;

    private JTable transactionTable;
    private DefaultTableModel tableModel;
    public DashboardFrame(int userId){
        this.userId=userId;
        this.service=new TransactionService(userId);
        setTitle("SpendWise - Personal Expense Tracker");
        setSize(1100, 750);
        setMinimumSize(new Dimension(850, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        createUI();
        loadTransactions();
        loadBudgetSummary();
    }
    
    
    private JPanel createBudgetPanel() {

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));

        budgetLabel = new JLabel("Monthly Budget: ₹0.00");
        spentLabel = new JLabel("Spent: ₹0.00");
        remainingLabel = new JLabel("Remaining: ₹0.00");

        budgetProgressBar = new JProgressBar(0, 100);
        budgetProgressBar.setStringPainted(true);
        budgetProgressBar.setString("0% used");

        JButton setBudgetButton = new JButton("Set Monthly Budget");
        setBudgetButton.addActionListener(e -> setMonthlyBudget());

        panel.setBorder(BorderFactory.createTitledBorder(
                "Monthly Budget"));

        panel.add(budgetLabel);
        panel.add(spentLabel);
        panel.add(remainingLabel);
        panel.add(budgetProgressBar);
        panel.add(setBudgetButton);

        return panel;
    }
    private JPanel createFilterPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));

        // First row: text search and transaction type
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        searchField = new JTextField(12);
        typeFilter = new JComboBox<>(
            new String[]{"ALL", "INCOME", "EXPENSE"}
        );

        searchButton = new JButton("Search");
        resetFilterButton = new JButton("Reset");

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("Type:"));
        searchPanel.add(typeFilter);
        searchPanel.add(searchButton);
        searchPanel.add(resetFilterButton);
        
  

        // Second row: date range
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        fromDateField = new JTextField(9);
        toDateField = new JTextField(9);
        dateFilterButton = new JButton("Apply Dates");

        datePanel.add(new JLabel("From:"));
        datePanel.add(fromDateField);
        datePanel.add(new JLabel("To:"));
        datePanel.add(toDateField);
        datePanel.add(dateFilterButton);

        panel.add(searchPanel);
        panel.add(datePanel);

        searchButton.addActionListener(e -> applyTransactionFilter());

        resetFilterButton.addActionListener(e -> {
            searchField.setText("");
            typeFilter.setSelectedItem("ALL");
            fromDateField.setText("");
            toDateField.setText("");
            updateAllFilters();
        });

        dateFilterButton.addActionListener(e -> applyDateFilter());

        return panel;
    }
    private void applyDateFilter() {
        updateAllFilters();
    }

    private void loadBudgetSummary() {

        YearMonth currentMonth = YearMonth.now();

        try {
            int month = currentMonth.getMonthValue();
            int year = currentMonth.getYear();

            double budget = budgetService.getBudget(
                    userId, month, year);

            double spent = budgetService.getMonthlyExpenses(
                    userId, month, year);

            double remaining = budget - spent;

            budgetLabel.setText(String.format(
                    "Monthly Budget: ₹%.2f", budget));

            spentLabel.setText(String.format(
                    "Spent: ₹%.2f", spent));

            remainingLabel.setText(String.format(
                    "Remaining: ₹%.2f", remaining));

            if (budget <= 0) {
                budgetProgressBar.setValue(0);
                budgetProgressBar.setString("Set a budget");

            } else {
                int percentage = (int) Math.min(
                        100, Math.round(spent / budget * 100));

                budgetProgressBar.setValue(percentage);
                budgetProgressBar.setString(
                        percentage + "% used");

                if (spent > budget) {
                    remainingLabel.setText(String.format(
                            "Over budget by: ₹%.2f",
                            spent - budget));

                    JOptionPane.showMessageDialog(
                            this,
                            "You have exceeded your monthly budget!",
                            "Budget Warning",
                            JOptionPane.WARNING_MESSAGE);

                } else if (spent >= budget * 0.8) {
                    remainingLabel.setText(String.format(
                            "Remaining: ₹%.2f — Budget nearly reached",
                            remaining));
                }
            }

        } catch (SQLException ex) {
            showError("Could not load budget summary", ex);
        }
    }

    private void setMonthlyBudget() {

        String input = JOptionPane.showInputDialog(
                this,
                "Enter your budget for this month (₹):",
                "Set Monthly Budget",
                JOptionPane.QUESTION_MESSAGE);

        if (input == null) {
            return;
        }

        try {
            double amount = Double.parseDouble(input.trim());

            if (!Double.isFinite(amount) || amount <= 0) {
                throw new IllegalArgumentException(
                        "Enter a positive budget amount.");
            }

            YearMonth currentMonth = YearMonth.now();

            Budget budget = new Budget(
                    userId,
                    currentMonth.getMonthValue(),
                    currentMonth.getYear(),
                    amount);

            budgetService.saveBudget(budget);

            loadBudgetSummary();

            JOptionPane.showMessageDialog(
                    this,
                    "Monthly budget saved successfully!");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please enter a valid number.",
                    "Invalid Budget",
                    JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Budget",
                    JOptionPane.ERROR_MESSAGE);

        } catch (SQLException ex) {
            showError("Could not save budget", ex);
        }
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBackground(new Color(37, 99, 235));
        button.setForeground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(
            9, 15, 9, 15
        ));
        button.setOpaque(true);
    }

    private void updateAllFilters() {
        java.util.List<RowFilter<Object, Object>> filters =
                new java.util.ArrayList<>();

        // 1. Search text
        String searchText = searchField.getText().trim();

        if (!searchText.isEmpty()) {
            filters.add(RowFilter.regexFilter(
                    "(?i)" + java.util.regex.Pattern.quote(searchText)
            ));
        }

        // 2. Transaction type
        String selectedType = (String) typeFilter.getSelectedItem();

        if (selectedType != null && !selectedType.equals("ALL")) {
            // Type column index: change if your table differs.
            filters.add(RowFilter.regexFilter(
                    "^" + java.util.regex.Pattern.quote(selectedType) + "$",
                    1
            ));
        }

        // 3. Date range
        String fromText = fromDateField.getText().trim();
        String toText = toDateField.getText().trim();

        try {
            java.time.LocalDate fromDate = fromText.isEmpty()
                    ? null : java.time.LocalDate.parse(fromText);

            java.time.LocalDate toDate = toText.isEmpty()
                    ? null : java.time.LocalDate.parse(toText);

            if (fromDate != null && toDate != null
                    && fromDate.isAfter(toDate)) {
                JOptionPane.showMessageDialog(
                        this,
                        "The start date cannot be after the end date.",
                        "Invalid Date Range",
                        JOptionPane.WARNING_MESSAGE
                );
                return;
            }

            if (fromDate != null || toDate != null) {
                filters.add(new RowFilter<Object, Object>() {
                    @Override
                    public boolean include(
                            Entry<? extends Object, ? extends Object> entry) {

                        // Date column index: change if your table differs.
                        String dateText = entry.getStringValue(4);

                        try {
                            java.time.LocalDate date =
                                    java.time.LocalDate.parse(dateText);

                            return (fromDate == null
                                        || !date.isBefore(fromDate))
                                    && (toDate == null
                                        || !date.isAfter(toDate));

                        } catch (java.time.format.DateTimeParseException ex) {
                            return false;
                        }
                    }
                });
            }

        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Enter valid dates in yyyy-MM-dd format.",
                    "Invalid Date",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        sorter.setRowFilter(
                filters.isEmpty() ? null : RowFilter.andFilter(filters)
        );
    }
    private void createUI() {
        setLayout(new BorderLayout(10, 10));

        JLabel heading = new JLabel(
                "SpendWise - Personal Expense Tracker",
                SwingConstants.CENTER);

        heading.setFont(new Font("Arial", Font.BOLD, 24));
        heading.setBorder(
                BorderFactory.createEmptyBorder(15, 10, 15, 10));

        add(heading, BorderLayout.NORTH);
        balanceLabel = new JLabel("₹0.00");
        incomeLabel = new JLabel("₹0.00");
        expenseLabel = new JLabel("₹0.00");

        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 12, 0));
        summaryPanel.setBackground(new Color(248, 250, 252));
        summaryPanel.setBorder(
            BorderFactory.createEmptyBorder(12, 12, 12, 12)
        );

        summaryPanel.add(
            createSummaryCard("Total Balance", balanceLabel)
        );

        summaryPanel.add(
            createSummaryCard("Total Income", incomeLabel)
        );

        summaryPanel.add(
            createSummaryCard("Total Expenses", expenseLabel)
        );
        
        // Transaction table
        String[] columns = {
            "ID", "Type", "Amount", "Category", "Date", "Description"
        };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(30);
        transactionTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        transactionTable.setForeground(new Color(51, 65, 85));
        transactionTable.setBackground(Color.WHITE);
        transactionTable.setGridColor(new Color(226, 232, 240));
        transactionTable.setShowVerticalLines(false);
        transactionTable.setShowHorizontalLines(true);
        transactionTable.setSelectionBackground(new Color(219, 234, 254));
        transactionTable.setSelectionForeground(new Color(30, 41, 59));
        
        JTableHeader header = transactionTable.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(new Color(241, 245, 249));
        header.setForeground(new Color(30, 41, 59));
        header.setPreferredSize(new Dimension(0, 36));
        header.setReorderingAllowed(false);
        
        
        
        transactionTable.setDefaultRenderer(Object.class,
        new javax.swing.table.DefaultTableCellRenderer() {

        @Override
        public Component getTableCellRendererComponent(
                JTable table,
                Object value,
                boolean isSelected,
                boolean hasFocus,
                int row,
                int column) {

            Component component = super.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column
            );

            if (!isSelected) {
                component.setBackground(
                    row % 2 == 0
                        ? Color.WHITE
                        : new Color(248, 250, 252)
                );
                component.setForeground(new Color(51, 65, 85));
            }

            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

            return component;
        }
    }
);
        
        sorter = new TableRowSorter<>(transactionTable.getModel());
        transactionTable.setRowSorter(sorter);
        transactionTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(transactionTable);

        JPanel centerPanel = new JPanel(new BorderLayout(10,10));
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(summaryPanel, BorderLayout.NORTH);
        topPanel.add(createFilterPanel(), BorderLayout.SOUTH);
        
        centerPanel.add(topPanel, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(createBudgetPanel(), BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Buttons
        JButton addButton = new JButton("Add Transaction");
        JButton deleteButton = new JButton("Delete Selected");
        JButton editButton = new JButton("Edit Selected");
        JButton refreshButton = new JButton("Refresh");
        JButton logoutButton = new JButton("Logout");
        
        styleButton(addButton);
        styleButton(editButton);
        styleButton(deleteButton);
        styleButton(refreshButton);
        styleButton(logoutButton);

        addButton.addActionListener(e -> addTransaction());
        deleteButton.addActionListener(e -> deleteTransaction());
        editButton.addActionListener(e -> editTransaction());
        refreshButton.addActionListener(e -> loadTransactions());
        logoutButton.addActionListener(e -> logout());

        JPanel buttonPanel = new JPanel(new FlowLayout());

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
    private void logout() {
    int choice = JOptionPane.showConfirmDialog(
        this,
        "Are you sure you want to log out?",
        "Confirm Logout",
        JOptionPane.YES_NO_OPTION
    );

    if (choice == JOptionPane.YES_OPTION) {
        dispose();

        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setLocationRelativeTo(null);
        loginFrame.setVisible(true);
    }
    }
    private void applyTransactionFilter() {
        updateAllFilters();
    }
    private JPanel createSummaryCard(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        valueLabel.setFont(new Font("Arial", Font.BOLD, 20));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);
       

        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)));

        panel.add(titleLabel);
        panel.add(valueLabel);

        return panel;
    }

    private void loadTransactions() {
        try {
            List<Transaction> transactions =
                    service.viewTransactions();

            tableModel.setRowCount(0);

            double totalIncome = 0;
            double totalExpense = 0;

            for (Transaction t : transactions) {
                tableModel.addRow(new Object[]{
                    t.getTransactionId(),
                    t.getTransactionType(),
                    String.format("%.2f", t.getAmount()),
                    t.getCategory(),
                    t.getTransactionDate(),
                    t.getDescription()
                });

                if ("INCOME".equals(t.getTransactionType())) {
                    totalIncome += t.getAmount();
                } else {
                    totalExpense += t.getAmount();
                }
            }
            loadBudgetSummary();

            incomeLabel.setText(
                    String.format("₹%.2f", totalIncome));

            expenseLabel.setText(
                    String.format("₹%.2f", totalExpense));

            balanceLabel.setText(
                    String.format("₹%.2f",
                            totalIncome - totalExpense));

        } catch (SQLException ex) {
            showError("Could not load transactions", ex);
        }
    }
    
    
    private void editTransaction() {

        int selectedRow = transactionTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select a transaction to edit."
            );
            return;
        }

        int row = transactionTable.convertRowIndexToModel(selectedRow);

        int id = Integer.parseInt(
                tableModel.getValueAt(row, 0).toString());

        String oldType =
                tableModel.getValueAt(row, 1).toString();

        String oldAmount =
                tableModel.getValueAt(row, 2).toString();

        String oldCategory =
                tableModel.getValueAt(row, 3).toString();

        String oldDate =
                tableModel.getValueAt(row, 4).toString();

        Object oldDescription =
                tableModel.getValueAt(row, 5);

        JTextField amountField = new JTextField(oldAmount);
        JTextField dateField = new JTextField(oldDate);

        JTextField descriptionField = new JTextField(
                oldDescription == null
                        ? ""
                        : oldDescription.toString());

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{"EXPENSE", "INCOME"});

        typeBox.setSelectedItem(oldType);

        JComboBox<String> categoryBox = new JComboBox<>(
                new String[]{
                    "Food", "Travel", "Education",
                    "Shopping", "Bills", "Pocket Money",
                    "Salary", "Other", oldCategory
                });

        categoryBox.setSelectedItem(oldCategory);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        form.add(new JLabel("Type:"));
        form.add(typeBox);

        form.add(new JLabel("Amount (₹):"));
        form.add(amountField);

        form.add(new JLabel("Category:"));
        form.add(categoryBox);

        form.add(new JLabel("Date (YYYY-MM-DD):"));
        form.add(dateField);

        form.add(new JLabel("Description:"));
        form.add(descriptionField);

        int result = JOptionPane.showConfirmDialog(
                this,
                form,
                "Edit Transaction",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double amount = Double.parseDouble(
                    amountField.getText().trim());

            String type = (String) typeBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();

            LocalDate date = LocalDate.parse(
                    dateField.getText().trim());

            String description =
                    descriptionField.getText().trim();

            Transaction transaction;

            if ("INCOME".equals(type)) {
                transaction = new Income(
                        amount, category, date, description);
            } else {
                transaction = new Expense(
                        amount, category, date, description);
            }

            transaction.setTransactionId(id);

            service.updateTransaction(transaction);

            JOptionPane.showMessageDialog(
                    this, "Transaction updated successfully!");

            loadTransactions();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter a valid amount.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);

        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter the date as YYYY-MM-DD.",
                    "Invalid Date", JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);

        } catch (SQLException ex) {
            showError("Could not update transaction", ex);
        }
    }


    private void addTransaction() {
        JTextField amountField = new JTextField();
        JTextField dateField =
                new JTextField(LocalDate.now().toString());
        JTextField descriptionField = new JTextField();

        JComboBox<String> typeBox = new JComboBox<>(
                new String[]{"EXPENSE", "INCOME"});

        JComboBox<String> categoryBox = new JComboBox<>(
                new String[]{
                    "Food", "Travel", "Education",
                    "Shopping", "Bills", "Pocket Money",
                    "Salary", "Other"
                });

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));

        form.add(new JLabel("Type:"));
        form.add(typeBox);

        form.add(new JLabel("Amount (₹):"));
        form.add(amountField);

        form.add(new JLabel("Category:"));
        form.add(categoryBox);

        form.add(new JLabel("Date (YYYY-MM-DD):"));
        form.add(dateField);

        form.add(new JLabel("Description:"));
        form.add(descriptionField);

        int result = JOptionPane.showConfirmDialog(
                this, form, "Add Transaction",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        try {
            double amount = Double.parseDouble(
                    amountField.getText().trim());

            String type = (String) typeBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();

            LocalDate date = LocalDate.parse(
                    dateField.getText().trim());

            String description =
                    descriptionField.getText().trim();

            Transaction transaction;

            if ("INCOME".equals(type)) {
                transaction = new Income(
                        amount, category, date, description);
            } else {
                transaction = new Expense(
                        amount, category, date, description);
            }

            service.addTransaction(transaction);

            JOptionPane.showMessageDialog(
                    this, "Transaction saved successfully!");

            loadTransactions();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter a valid amount.",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);

        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(
                    this, "Enter the date as YYYY-MM-DD.",
                    "Invalid Date", JOptionPane.ERROR_MESSAGE);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this, ex.getMessage(),
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);

        } catch (SQLException ex) {
            showError("Could not save transaction", ex);
        }
    }

    private void deleteTransaction() {
        int selectedRow = transactionTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this, "Please select a transaction first.");
            return;
        }

        int transactionId = Integer.parseInt(
                tableModel.getValueAt(selectedRow, 0).toString());

        int confirm = JOptionPane.showConfirmDialog(
                this, "Delete this transaction?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            service.deleteTransaction(transactionId);

            JOptionPane.showMessageDialog(
                    this, "Transaction deleted successfully.");

            loadTransactions();

        } catch (SQLException ex) {
            showError("Could not delete transaction", ex);
        }
    }

    private void showError(String message, Exception ex) {
        JOptionPane.showMessageDialog(
                this,
                message + ":\n" + ex.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}