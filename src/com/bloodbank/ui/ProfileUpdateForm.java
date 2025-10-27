package com.bloodbank.view;

import com.bloodbank.dao.UserDAO;
import com.bloodbank.model.User;

import javax.swing.*;

public class ProfileUpdateForm extends JFrame {
    private JTextField usernameField, cityField, contactField;
    private JPasswordField passwordField;
    private JComboBox<String> bloodGroupBox, availabilityBox;
    private UserDAO userDAO = new UserDAO();
    private String originalUsername;

    public ProfileUpdateForm(String username) {
        originalUsername = username;

        setTitle("Update My Profile");
        setSize(400, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        // --- Labels & Fields ---
        int y = 50;
        usernameField = addTextField("Username:", y); y += 40;
        passwordField = new JPasswordField(); addField("New Password (optional):", passwordField, y); y += 40;

        bloodGroupBox = new JComboBox<>(new String[]{"A+","A-","B+","B-","AB+","AB-","O+","O-"});
        addField("Blood Group:", bloodGroupBox, y); y += 40;

        cityField = addTextField("City:", y); y += 40;
        contactField = addTextField("Contact:", y); y += 40;

        availabilityBox = new JComboBox<>(new String[]{"Available","Not Available"});
        addField("Availability:", availabilityBox, y); y += 50;

        JButton updateBtn = addButton("Update", 80, y);
        JButton backBtn = addButton("Back", 210, y);

        // --- Actions ---
        updateBtn.addActionListener(e -> handleUpdate());
        backBtn.addActionListener(e -> dispose());

        loadUserProfile();
    }

    private JTextField addTextField(String label, int y) {
        JTextField field = new JTextField();
        addField(label, field, y);
        return field;
    }

    private void addField(String label, JComponent comp, int y) {
        JLabel l = new JLabel(label);
        l.setBounds(50, y, 120, 25);
        add(l);
        comp.setBounds(180, y, 150, 25);
        add(comp);
    }

    private JButton addButton(String text, int x, int y) {
        JButton btn = new JButton(text);
        btn.setBounds(x, y, 100, 30);
        add(btn);
        return btn;
    }

    private void loadUserProfile() {
        User u = userDAO.getUserByUsername(originalUsername);
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Error loading profile.");
            dispose();
            return;
        }
        usernameField.setText(u.getUsername());
        cityField.setText(u.getCity());
        contactField.setText(u.getContact());
        bloodGroupBox.setSelectedItem(u.getBloodGroup());
        availabilityBox.setSelectedItem(u.getAvailability());
    }

    // --- UPDATED LOGIC ---
    private void handleUpdate() {
        String name = usernameField.getText();
        String pass = new String(passwordField.getPassword());
        String city = cityField.getText();
        String contact = contactField.getText();

        // Validation: Only check fields that are NOT password
        if (name.isEmpty() || city.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields (password is optional)!");
            return;
        }
        
        // Check if password field is filled
        boolean passwordChanged = !pass.isEmpty();

        User updatedUser = new User(name,
                (passwordChanged ? pass : null), // Pass null if password is not being changed
                (String) bloodGroupBox.getSelectedItem(),
                city, contact, (String) availabilityBox.getSelectedItem());

        // Call the new, smarter DAO method
        if (userDAO.updateUserProfile(originalUsername, updatedUser, passwordChanged)) {
            JOptionPane.showMessageDialog(this, "Profile updated!");
            originalUsername = name; // Update username in case it was changed
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Update failed! Username may be taken.");
        }
    }
}
