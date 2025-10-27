package com.bloodbank.view; 
import com.bloodbank.dao.UserDAO; 
import com.bloodbank.model.User; 
import javax.swing.*;
import java.awt.event.*;

public class RegistrationForm extends JFrame {
    private JTextField usernameField, cityField, contactField;
    private JPasswordField passwordField, confirmPasswordField;
    private JComboBox<String> bloodGroupComboBox, availabilityComboBox;
    private JButton registerButton, backButton;
    private UserDAO userDAO = new UserDAO();

    public RegistrationForm() {
        setTitle("Register New User");
        setSize(450, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(null);

        int y = 50, xLabel = 50, w = 150, h = 25, xField = xLabel + w + 10, wField = 200;
        addLabel("Username:", xLabel, y);
        usernameField = addTextField(xField, y); y += 40;
        addLabel("Password:", xLabel, y);
        passwordField = addPassField(xField, y); y += 40;
        addLabel("Confirm Password:", xLabel, y); 
        confirmPasswordField = addPassField(xField, y); y += 40;
        addLabel("Blood Group:", xLabel, y);
        bloodGroupComboBox = addComboBox(xField, y, new String[]{"A+","A-","B+","B-","AB+","AB-","O+","O-"}); y += 40;
        addLabel("City:", xLabel, y); cityField = addTextField(xField, y); y += 40;
        addLabel("Contact (Phone):", xLabel, y); 
        contactField = addTextField(xField, y);
        contactField.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) { if (!Character.isDigit(e.getKeyChar()) || contactField.getText().length() >= 10) e.consume(); }
        });
        y += 40;
        addLabel("Availability:", xLabel, y);
        availabilityComboBox = addComboBox(xField, y, new String[]{"Available", "Not Available"});
        y += 50;

        registerButton = new JButton("Register"); 
        registerButton.setBounds(xField - 80, y, 120, 30); add(registerButton);
        backButton = new JButton("Back to Login");
        backButton.setBounds(xField + 50, y, 120, 30); add(backButton);

        registerButton.addActionListener(e -> handleRegistration());
        backButton.addActionListener(e -> { new LoginForm().setVisible(true); dispose(); });
    }

    private JLabel addLabel(String t, int x, int y){
    	JLabel l=new JLabel(t);
    	l.setBounds(x,y,150,25);
    	add(l);
    	return l;
    	}
    private JTextField addTextField(int x,int y){
    	JTextField f=new JTextField();
    	f.setBounds(x,y,200,25);
    	add(f);
    	return f;
    	}
    private JPasswordField addPassField(int x,int y){
    	JPasswordField f=new JPasswordField();
    	f.setBounds(x,y,200,25);
    	add(f);
    	return f;
    	}
    private JComboBox<String> addComboBox(int x,int y,String[] s){
    	JComboBox<String> c=new JComboBox<>(s);
    	c.setBounds(x,y,200,25);
    	add(c);
    	return c;
    	}

    private void handleRegistration() {
        String username=usernameField.getText(), pass=new String(passwordField.getPassword()), 
        confirm=new String(confirmPasswordField.getPassword()), blood=(String)bloodGroupComboBox.getSelectedItem(), 
        city=cityField.getText(), contact=contactField.getText(), avail=(String)availabilityComboBox.getSelectedItem();

        if(username.isEmpty()||pass.isEmpty()||city.isEmpty()||contact.isEmpty()){
        	JOptionPane.showMessageDialog(this,"Please fill all fields.");
        	return;
        	}
        if(!pass.equals(confirm)){
        	JOptionPane.showMessageDialog(this,"Passwords do not match.");
        	return;
        	}
        if(contact.length()!=10){
        	JOptionPane.showMessageDialog(this,"Phone number must be 10 digits.");
        	return;
        	}

        User u=new User(username,pass,blood,city,contact,avail);
        if(userDAO.registerUser(u)){
        	JOptionPane.showMessageDialog(this,"Registration successful!");
        	new LoginForm().setVisible(true);
        	dispose();
        	}
        else JOptionPane.showMessageDialog(this,"Username already exists.");
    }
}
