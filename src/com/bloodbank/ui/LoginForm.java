package com.bloodbank.view;

import com.bloodbank.dao.UserDAO;
import javax.swing.*;
import java.awt.event.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private UserDAO userDAO = new UserDAO();

    public LoginForm() {
        setTitle("Blood Bank Login");
        setSize(400, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // --- Labels & Fields ---
        addLabel("Username:", 50, 50);
        usernameField = addTextField(140, 50);

        addLabel("Password:", 50, 90);
        passwordField = new JPasswordField();
        passwordField.setBounds(140, 90, 180, 25);
        add(passwordField);

        // --- Buttons ---
        JButton loginBtn = addButton("Login", 50, 140);
        JButton registerBtn = addButton("Register", 190, 140);

        // --- Actions ---
        loginBtn.addActionListener(e -> handleLogin());
        registerBtn.addActionListener(e -> {
            new RegistrationForm().setVisible(true);
            dispose();
        });
    }

    private void handleLogin() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            showMsg("Please enter username and password.", "Error");
            return;
        }

        if (userDAO.validateLogin(user, pass)) {
            showMsg("Login Successful!", "Welcome");
            new MainDashboard(user).setVisible(true);
            dispose();
        } else {
            showMsg("Invalid Username or Password.", "Login Failed");
        }
    }

    // --- Small helper methods to keep it clean ---
    private void addLabel(String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, 80, 25);
        add(label);
    }

    private JTextField addTextField(int x, int y) {
        JTextField field = new JTextField();
        field.setBounds(x, y, 180, 25);
        add(field);
        return field;
    }

    private JButton addButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 130, 30);
        add(btn);
        return btn;
    }

    private void showMsg(String msg, String title) {
        JOptionPane.showMessageDialog(this, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        new LoginForm().setVisible(true);
    }
}
