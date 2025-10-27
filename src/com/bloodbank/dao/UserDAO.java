package com.bloodbank.dao;

import com.bloodbank.model.User;
import java.sql.*;
import java.util.*;

public class UserDAO {

    //Check if login credentials are correct
    public boolean validateLogin(String username, String password) {
        String sql = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return password.equals(rs.getString("password"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Register a new user
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (username, password, blood_group, city, contact, availability) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getBloodGroup());
            ps.setString(4, user.getCity());
            ps.setString(5, user.getContact());
            ps.setString(6, user.getAvailability());

            return ps.executeUpdate() > 0; // success if row inserted
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get a user by username
    // Renamed to match the method called by ProfileUpdateForm
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getString("username"),
                        rs.getString("blood_group"),
                        rs.getString("city"),
                        rs.getString("contact"),
                        rs.getString("availability")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // not found
    }

    //Update user details
    //Replaced your simple updateUser with the smarter version that supports optional password changes
    public boolean updateUserProfile(String oldUsername, User user, boolean changePassword) {
        // Build SQL string dynamically
        StringBuilder sql = new StringBuilder("UPDATE users SET username = ?, blood_group = ?, city = ?, contact = ?, availability = ?");
        if (changePassword) {
            sql.append(", password = ?");
        }
        sql.append(" WHERE username = ?");

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int i = 1; // Parameter index
            ps.setString(i++, user.getUsername());
            ps.setString(i++, user.getBloodGroup());
            ps.setString(i++, user.getCity());
            ps.setString(i++, user.getContact());
            ps.setString(i++, user.getAvailability());
            
            if (changePassword) {
                ps.setString(i++, user.getPassword()); // Set password
            }
            
            ps.setString(i++, oldUsername); // Set username for WHERE clause

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Change only user availability
    // Renamed to match the method name used in RequestDAO
    public boolean updateUserAvailability(String username, String status) {
        String sql = "UPDATE users SET availability=? WHERE username=?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Search donors (by blood group, city)
    public List<User> searchDonors(String bloodGroup, String city, String excludeUsername) {
        List<User> donors = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE username != ?"
                       + ("All".equals(bloodGroup) ? "" : " AND blood_group = ?")
                       + (city.isEmpty() ? "" : " AND city LIKE ?");

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int i = 1;
            ps.setString(i++, excludeUsername);
            if (!"All".equals(bloodGroup)) ps.setString(i++, bloodGroup);
            if (!city.isEmpty()) ps.setString(i, "%" + city + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                donors.add(new User(
                        rs.getString("username"),
                        rs.getString("blood_group"),
                        rs.getString("city"),
                        rs.getString("contact"),
                        rs.getString("availability")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return donors;
    }

    // --- NEW ADMIN METHODS ---

    // ✅ NEW METHOD: Get all users for the admin (except the admin itself)
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        // Exclude the 'admin' user from the list
        String sql = "SELECT * FROM users WHERE username != 'admin'";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(new User(
                        rs.getString("username"),
                        rs.getString("blood_group"),
                        rs.getString("city"),
                        rs.getString("contact"),
                        rs.getString("availability")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    // ✅ NEW METHOD: Delete a user and all their associated requests
    public boolean deleteUser(String username) {
        Connection conn = null;
        try {
            conn = DatabaseConnection.connect();
            conn.setAutoCommit(false); // Start transaction

            // 1. Delete all requests involving this user (as sender or recipient)
            String deleteReqSql = "DELETE FROM requests WHERE sender_username = ? OR recipient_username = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteReqSql)) {
                
                // --- THIS IS THE FIX ---
                ps.setString(1, username);
                ps.setString(2, username); // <-- This line was missing
                // --- END OF FIX ---
                
                ps.executeUpdate();
            }

            // 2. Delete the user itself
            String deleteUserSql = "DELETE FROM users WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(deleteUserSql)) {
                ps.setString(1, username);
                ps.executeUpdate();
            }

            conn.commit(); // Commit transaction
            return true;
            
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback(); // Rollback on error
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
