package com.bloodbank.view;

import com.bloodbank.dao.RequestDAO;
import com.bloodbank.dao.UserDAO;
import com.bloodbank.model.User;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class MainDashboard extends JFrame {
    private JComboBox<String> bloodBox;
    private JTextField cityField;
    private JTable donorTable;
    private DefaultTableModel model;
    private String username;
    private UserDAO userDAO = new UserDAO();
    private RequestDAO requestDAO = new RequestDAO();

    public MainDashboard(String username) {
        this.username = username;

        setTitle("Blood Bank Dashboard");
        setSize(800, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        add(makeTopPanel(), BorderLayout.NORTH);
        add(makeTablePanel(), BorderLayout.CENTER);

        searchDonors();
    }

    private JPanel makeTopPanel() {
        JPanel top = new JPanel(new BorderLayout());

        // --- Left: Search ---
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Blood Group:"));
        bloodBox = new JComboBox<>(new String[]{"All", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        searchPanel.add(bloodBox);
        searchPanel.add(new JLabel("City:"));
        cityField = new JTextField(12);
        searchPanel.add(cityField);
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> searchDonors());
        searchPanel.add(searchBtn);

        // --- Right: Action Buttons ---
        JPanel actions = new JPanel();
        actions.add(makeButton("My Profile", new Color(0,128,0), e -> new ProfileUpdateForm(username).setVisible(true)));
        actions.add(makeButton("Requests", new Color(0,100,255), e -> new RequestsForm(username).setVisible(true)));
        
        // --- THIS IS THE UPDATED LINE ---
        actions.add(makeButton("Admin", Color.RED, e -> new AdminLoginForm().setVisible(true)));
        // --- END OF UPDATE ---
        
        actions.add(makeButton("Logout", new Color(100,100,100), e -> logout()));

        top.add(searchPanel, BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);
        return top;
    }

    private JPanel makeTablePanel() {
        String[] cols = {"Username", "Blood Group", "City", "Contact", "Availability", "Request"};
        model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 5 && "Available".equals(getValueAt(r, 4));
            }
        };
        donorTable = new JTable(model);
        donorTable.getColumn("Request").setCellRenderer(new BtnRenderer());
        donorTable.getColumn("Request").setCellEditor(new BtnEditor(new JCheckBox()));
        return new JPanel(new BorderLayout()) {{
            add(new JScrollPane(donorTable), BorderLayout.CENTER);
        }};
    }

    private void searchDonors() {
        model.setRowCount(0);
        String bg = (String) bloodBox.getSelectedItem();
        String city = cityField.getText().trim();

        List<User> donors = userDAO.searchDonors(bg, city, username);
        for (User u : donors) {
            model.addRow(new Object[]{
                u.getUsername(), u.getBloodGroup(), u.getCity(),
                u.getContact(), u.getAvailability(), "Send Request"
            });
        }
    }

    private void sendRequest(String toUser) {
        if (requestDAO.hasPendingRequest(username, toUser))
            JOptionPane.showMessageDialog(this, "Request already pending!", "Warning", JOptionPane.WARNING_MESSAGE);
        else if (requestDAO.createRequest(username, toUser))
            JOptionPane.showMessageDialog(this, "Request sent to " + toUser, "Success", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(this, "Database Error!", "Error", JOptionPane.ERROR_MESSAGE);
    }

    private JButton makeButton(String text, Color color, java.awt.event.ActionListener action) {
        JButton btn = new JButton(text);
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.addActionListener(action);
        return btn;
    }

    private void logout() {
        if (JOptionPane.showConfirmDialog(this, "Logout?", "Confirm", JOptionPane.YES_NO_OPTION)
            == JOptionPane.YES_OPTION) {
            dispose();
            new LoginForm().setVisible(true);
        }
    }

    // --- Button in table ---
    class BtnRenderer extends JButton implements TableCellRenderer {
        BtnRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText(v == null ? "" : v.toString());
            setEnabled("Available".equals(t.getValueAt(r, 4)));
            return this;
        }
    }

    class BtnEditor extends DefaultCellEditor {
        JButton btn = new JButton();
        String label; boolean pushed;

        BtnEditor(JCheckBox cb) {
            super(cb);
            btn.setOpaque(true);
            btn.addActionListener(e -> fireEditingStopped());
        }

        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            label = v == null ? "" : v.toString();
            btn.setText(label);
            pushed = true;
            return btn;
        }

        public Object getCellEditorValue() {
            if (pushed) {
                int row = donorTable.getSelectedRow();
                sendRequest(donorTable.getValueAt(row, 0).toString());
            }
            pushed = false;
            return label;
        }
    }

    public static void main(String[] args) {
        new MainDashboard("DemoUser").setVisible(true);
    }
}
