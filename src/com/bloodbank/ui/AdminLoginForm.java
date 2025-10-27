package com.bloodbank.view;
import javax.swing.*;
public class AdminLoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public AdminLoginForm() {
        setTitle("Admin Login");
        setSize(350, 200);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Close only this window
        setLocationRelativeTo(null); // Center
        setLayout(null);

        // --- Labels & Fields ---
        addLabel("Username:", 30, 30);
        usernameField = addTextField(120, 30);
        usernameField.setText("admin"); // Pre-fill for convenience

        addLabel("Password:", 30, 70);
        passwordField = new JPasswordField();
        passwordField.setBounds(120, 70, 180, 25);
        add(passwordField);

        // --- Button ---
        JButton loginBtn = new JButton("Admin Login");
        loginBtn.setBounds(120, 110, 180, 30);
        add(loginBtn);

        // --- Action ---
        loginBtn.addActionListener(e -> handleAdminLogin());
    }

    private void handleAdminLogin() {
        String user = usernameField.getText();
        String pass = new String(passwordField.getPassword());

        // Basic, hard-coded login
        if (user.equals("admin") && pass.equals("admin")) {
            new AdminDashboard().setVisible(true);
            dispose(); // Close login window on success
        } else {
            JOptionPane.showMessageDialog(this, "Invalid Admin Credentials.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    // --- Helpers ---
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
}
