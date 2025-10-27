package com.bloodbank.model;
import java.sql.Timestamp;
public class Request {
    private int requestId;
    private String senderUsername;
    private String recipientUsername;
    private String status;
    private Timestamp requestDate;    
    public Request(int requestId, String senderUsername, String recipientUsername, String status, Timestamp requestDate) {
        this.requestId = requestId;
        this.senderUsername = senderUsername;
        this.recipientUsername = recipientUsername;
        this.status = status;
        this.requestDate = requestDate;
    }    
    public int getRequestId() { return requestId; }
    public String getSenderUsername() { return senderUsername; }
    public String getRecipientUsername() { return recipientUsername; }
    public String getStatus() { return status; }
    public Timestamp getRequestDate() { return requestDate; }
}
