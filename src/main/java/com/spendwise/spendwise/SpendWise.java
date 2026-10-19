package com.spendwise.spendwise;

import com.spendwise.gui.LoginFrame;
import javax.swing.SwingUtilities;

public class SpendWise {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new LoginFrame().setVisible(true)
        );
    }
}