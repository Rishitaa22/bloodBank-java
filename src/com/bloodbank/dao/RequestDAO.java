package com.bloodbank.dao;

import com.bloodbank.model.Request;
import java.sql.*;
import java.util.*;

public class RequestDAO {
    
    // --- All your existing methods ---

    private Request map(ResultSet rs) throws SQLException {
        return new Request(
                rs.getInt("request_id"),
                rs.getString("sender_username"),
                rs.getString("recipient_username"),
                rs.getString("status"),
                rs.getTimestamp("request_date")
        );
    }
    private List<Request> getRequests(String sql, String user) {
        List<Request> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, user);
            ResultSet rs = p.executeQuery();
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public List<Request> getIncomingRequests(String user) {
        return getRequests("SELECT * FROM requests WHERE recipient_username = ?", user);
    }

    public List<Request> getSentRequests(String user) {
        return getRequests("SELECT * FROM requests WHERE sender_username = ?", user);
    }

    public boolean hasPendingRequest(String sender, String recipient) {
        String sql = "SELECT COUNT(*) FROM requests WHERE sender_username=? AND recipient_username=? AND status='Pending'";
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, sender); p.setString(2, recipient);
            ResultSet rs = p.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean createRequest(String sender, String recipient) {
        String sql = "INSERT INTO requests (sender_username, recipient_username, status) VALUES (?, ?, 'Pending')";
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, sender); p.setString(2, recipient);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean cancelRequest(int id, String sender) {
        String sql = "DELETE FROM requests WHERE request_id=? AND sender_username=? AND status='Pending'";
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id); p.setString(2, sender);
            return p.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }
    
    public boolean updateRequestStatus(int id, String status, String recipient) {
        Connection c = null; 
        try {
            c = DatabaseConnection.connect();
            c.setAutoCommit(false); 

            try (PreparedStatement p1 = c.prepareStatement("UPDATE requests SET status=? WHERE request_id=?")) {
                p1.setString(1, status); p1.setInt(2, id); p1.executeUpdate();
            }

            if ("Accepted".equals(status)) {
                new UserDAO().updateUserAvailability(recipient, "Not Available");
                try (PreparedStatement p2 = c.prepareStatement(
                        "UPDATE requests SET status='Declined' WHERE recipient_username=? AND status='Pending' AND request_id!=?")) {
                    p2.setString(1, recipient); p2.setInt(2, id); p2.executeUpdate();
                }
            }

            c.commit(); 
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (c != null) {
                    System.err.println("Transaction is being rolled back.");
                    c.rollback();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (c != null) {
                    c.setAutoCommit(true); // Reset connection to default
                    c.close(); // Manually close connection
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // --- NEW ADMIN METHOD ---

    // ✅ NEW METHOD: Get ALL requests for the admin
    public List<Request> getAllRequests() {
        List<Request> list = new ArrayList<>();
        String sql = "SELECT * FROM requests ORDER BY request_date DESC";
        try (Connection c = DatabaseConnection.connect();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet rs = p.executeQuery()) {
            
            while (rs.next()) {
                list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
