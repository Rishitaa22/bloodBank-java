package com.bloodbank.view;

import com.bloodbank.dao.RequestDAO;
import com.bloodbank.dao.UserDAO;
import com.bloodbank.model.Request;
import com.bloodbank.model.User;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class AdminDashboard extends JFrame {
    private UserDAO userDAO = new UserDAO();
    private RequestDAO requestDAO = new RequestDAO();
    private DefaultTableModel userModel, requestModel;

    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Don't exit app
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("User Management", createUserPanel());
        tabs.addTab("All Requests", createRequestsPanel());
        
        add(tabs, BorderLayout.CENTER);

        loadAllUsers();
        loadAllRequests();
    }

    // --- Panel for User Management ---
    private JPanel createUserPanel() {
        String[] cols = {"Username", "Blood Group", "City", "Contact", "Availability", "Delete"};
        userModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return c == 5; } // Only delete col
        };
        JTable userTable = new JTable(userModel);
        userTable.getColumn("Delete").setCellRenderer(new ButtonRenderer("Delete"));
        userTable.getColumn("Delete").setCellEditor(new ButtonEditor(new JCheckBox(), "Delete", (row) -> {
            handleDeleteUser(row);
        }));
        
        return new JPanel(new BorderLayout()) {{
            add(new JScrollPane(userTable), BorderLayout.CENTER);
        }};
    }

    // --- Panel for All Requests ---
    private JPanel createRequestsPanel() {
        String[] cols = {"ID", "Sender", "Recipient", "Status", "Date"};
        requestModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; } // View only
        };
        JTable requestTable = new JTable(requestModel);
        
        return new JPanel(new BorderLayout()) {{
            add(new JScrollPane(requestTable), BorderLayout.CENTER);
        }};
    }

    // --- Data Loaders ---
    private void loadAllUsers() {
        userModel.setRowCount(0);
        List<User> users = userDAO.getAllUsers();
        for (User u : users) {
            userModel.addRow(new Object[]{
                u.getUsername(), u.getBloodGroup(), u.getCity(),
                u.getContact(), u.getAvailability(), "Delete"
            });
        }
    }

    private void loadAllRequests() {
        requestModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Request> requests = requestDAO.getAllRequests();
        for (Request r : requests) {
            requestModel.addRow(new Object[]{
                r.getRequestId(), r.getSenderUsername(),
                r.getRecipientUsername(), r.getStatus(), sdf.format(r.getRequestDate())
            });
        }
    }
    
    // --- Action Handlers ---
    private void handleDeleteUser(int row) {
        String userToDelete = (String) userModel.getValueAt(row, 0);
        
        int choice = JOptionPane.showConfirmDialog(this, 
            "Delete user '" + userToDelete + "'? This will also delete all their requests.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            if (userDAO.deleteUser(userToDelete)) {
                JOptionPane.showMessageDialog(this, "User deleted successfully.");
                loadAllUsers(); // Refresh table
                loadAllRequests(); // Their requests are also gone
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete user.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // --- Inner classes for Table Buttons ---
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String text) {
            setOpaque(true);
            setText(text);
            setBackground(Color.RED);
            setForeground(Color.WHITE);
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private int selectedRow;
        private java.util.function.Consumer<Integer> action;

        public ButtonEditor(JCheckBox checkBox, String text, java.util.function.Consumer<Integer> action) {
            super(checkBox);
            this.action = action;
            button = new JButton(text);
            button.setOpaque(true);
            button.setBackground(Color.RED);
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> fireEditingStopped());
        }
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.selectedRow = row;
            return button;
        }
        public Object getCellEditorValue() {
            action.accept(selectedRow);
            return "Delete";
        }
    }
}
