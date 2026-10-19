
package com.spendwise.gui;

import com.spendwise.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.Arrays;

public class RegisterFrame extends JFrame {

    private final JTextField nameField = new JTextField(20);
    private final JTextField emailField = new JTextField(20);
    private final JPasswordField passwordField =
            new JPasswordField(20);
    private final JPasswordField confirmField =
            new JPasswordField(20);

    private final AuthService authService = new AuthService();

    public RegisterFrame() {
        setTitle("SpendWise - Register");
        setSize(420, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        createUI();
    }

    private void createUI() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(
                20, 20, 20, 20));

        JButton registerButton = new JButton("Register");

        panel.add(new JLabel("Full Name:"));
        panel.add(nameField);

        panel.add(new JLabel("Email:"));
        panel.add(emailField);

        panel.add(new JLabel("Password:"));
        panel.add(passwordField);

        panel.add(new JLabel("Confirm Password:"));
        panel.add(confirmField);

        panel.add(new JLabel(""));
        panel.add(registerButton);

        add(panel);

        registerButton.addActionListener(e -> register());
        getRootPane().setDefaultButton(registerButton);
    }

    private void register() {
        char[] password = passwordField.getPassword();
        char[] confirm = confirmField.getPassword();

        try {
            if (!Arrays.equals(password, confirm)) {
                JOptionPane.showMessageDialog(
                        this,
                        "Passwords do not match.",
                        "Registration Failed",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int userId = authService.register(
                    nameField.getText(),
                    emailField.getText(),
                    password);

            JOptionPane.showMessageDialog(
                    this,
                    "Account created successfully!\n"
                    + "Your user ID is " + userId
                    + ". Please log in.");

            dispose();

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE);

        } catch (SQLException ex) {
            String message = ex.getMessage();

            if (message != null
                    && message.toLowerCase().contains("duplicate")) {
                message = "This email is already registered.";
            } else {
                message = "Database error: " + message;
            }

            JOptionPane.showMessageDialog(
                    this,
                    message,
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);

        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
        }
    }
}
