
package com.spendwise.gui;

import com.spendwise.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

public class LoginFrame extends JFrame {

    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField =
            new JPasswordField(20);

    private final AuthService authService = new AuthService();

    public LoginFrame() {
        setTitle("SpendWise - Login");
        setSize(400, 280);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        createUI();
    }

    private void createUI() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(
                20, 30, 20, 30));

        JLabel heading = new JLabel(
                "Welcome to SpendWise", SwingConstants.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 22));

        JButton loginButton = new JButton("Login");
        JButton registerButton = new JButton("Create Account");

        panel.add(heading);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(loginButton);
        panel.add(registerButton);

        add(panel);

        loginButton.addActionListener(e -> login());
        registerButton.addActionListener(e ->
                new RegisterFrame().setVisible(true));
        getRootPane().setDefaultButton(loginButton);
    }

    private void login() {
        char[] password = passwordField.getPassword();

        try {
            int userId = authService.login(
                    emailField.getText(), password);

            if (userId == -1) {
                JOptionPane.showMessageDialog(
                        this,
                        "Invalid email or password.",
                        "Login Failed",
                        JOptionPane.ERROR_MESSAGE);
                passwordField.setText("");
                return;
            }

            JOptionPane.showMessageDialog(
                    this, "Login successful!");

            dispose();
            new DashboardFrame(userId).setVisible(true);

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Database error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
        }
    }
}
