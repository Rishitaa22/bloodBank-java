package com.bloodbank.model;
public class User {
    private String username;
    private String password; 
    private String bloodGroup;
    private String city;
    private String contact;
    private String availability;

    // A constructor for loading user data
    public User(String username, String bloodGroup, String city, String contact, String availability) {
        this.username = username;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.contact = contact;
        this.availability = availability;
    }

    // A constructor for registration or updates
    public User(String username, String password, String bloodGroup, String city, String contact, String availability) {
        this(username, bloodGroup, city, contact, availability);
        this.password = password;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }
    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }
}
