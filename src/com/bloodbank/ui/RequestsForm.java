package com.bloodbank.view;

import com.bloodbank.dao.RequestDAO;
import com.bloodbank.model.Request;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class RequestsForm extends JFrame {
    private String username;
    private RequestDAO dao = new RequestDAO();
    private DefaultTableModel inModel, outModel;

    public RequestsForm(String user) {
        this.username = user;
        setTitle("My Requests");
        setSize(700, 400);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Incoming", createIncomingPanel());
        tabs.addTab("Sent", createSentPanel());
        add(tabs);

        loadIncoming();
        loadSent();
    }

    // ---- INCOMING REQUESTS ----
    private JPanel createIncomingPanel() {
        JPanel p = new JPanel(new BorderLayout());
        inModel = new DefaultTableModel(new String[]{"ID", "From", "Date", "Status"}, 0) {
            public boolean isCellEditable(int r, int c) { return c == 3; }
        };
        JTable t = new JTable(inModel);
        t.removeColumn(t.getColumnModel().getColumn(0)); // hide ID
        t.getColumn("Status").setCellEditor(new DefaultCellEditor(new JComboBox<>(new String[]{"Pending", "Accepted", "Declined"})) {
            @Override
            public Object getCellEditorValue() {
                String newStatus = (String) super.getCellEditorValue();
                int row = t.getSelectedRow();
                int id = (int) inModel.getValueAt(row, 0);
                updateStatus(id, newStatus);
                return newStatus;
            }
        });
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    // ---- SENT REQUESTS ----
    private JPanel createSentPanel() {
        JPanel p = new JPanel(new BorderLayout());
        outModel = new DefaultTableModel(new String[]{"ID", "To", "Date", "Status", "Cancel"}, 0) {
            public boolean isCellEditable(int r, int c) {
                return c == 4 && "Pending".equals(getValueAt(r, 3));
            }
        };
        JTable t = new JTable(outModel);
        t.removeColumn(t.getColumnModel().getColumn(0)); // hide ID
        t.getColumn("Cancel").setCellRenderer(new ButtonRenderer());
        t.getColumn("Cancel").setCellEditor(new ButtonEditor(t));
        p.add(new JScrollPane(t), BorderLayout.CENTER);
        return p;
    }

    // ---- LOAD DATA ----
    private void loadIncoming() {
        inModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Request r : dao.getIncomingRequests(username))
            inModel.addRow(new Object[]{r.getRequestId(), r.getSenderUsername(), sdf.format(r.getRequestDate()), r.getStatus()});
    }

    private void loadSent() {
        outModel.setRowCount(0);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        for (Request r : dao.getSentRequests(username))
            outModel.addRow(new Object[]{r.getRequestId(), r.getRecipientUsername(), sdf.format(r.getRequestDate()), r.getStatus(), "Cancel"});
    }

    // ---- HELPER METHODS ----
    private void updateStatus(int id, String status) {
        if (dao.updateRequestStatus(id, status, username))
            JOptionPane.showMessageDialog(this, "Status updated to " + status);
        else JOptionPane.showMessageDialog(this, "Error updating request!");
        loadIncoming();
    }

    private void cancelRequest(int id) {
        if (dao.cancelRequest(id, username))
            JOptionPane.showMessageDialog(this, "Request cancelled!");
        else JOptionPane.showMessageDialog(this, "Could not cancel request!");
        loadSent();
    }

    // ---- BUTTON HANDLERS ----
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() { setOpaque(true); }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            setText("Cancel");
            setEnabled("Pending".equals(t.getValueAt(r, 3)));
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton btn; private JTable table;
        public ButtonEditor(JTable t) {
            super(new JCheckBox());
            this.table = t;
            btn = new JButton("Cancel");
            btn.addActionListener(e -> {
                int r = table.getSelectedRow();
                int id = (int) outModel.getValueAt(r, 0);
                cancelRequest(id);
                fireEditingStopped();
            });
        }
        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) { return btn; }
        public Object getCellEditorValue() { return "Cancel"; }
    }

    public static void main(String[] args) {
        new RequestsForm("demoUser").setVisible(true);
    }
}
