package com.gaminglounge.model;

import java.sql.Timestamp;

public class ServiceRequest {
    private int requestId;
    private int customerId;
    private String customerName; // Joined
    private String requestType;
    private String content;
    private String status;
    private Timestamp createdAt;
    private String sender; // 'Client' or 'Admin'

    public ServiceRequest() {}

    public ServiceRequest(int requestId, int customerId, String requestType, String content, String status, Timestamp createdAt) {
        this.requestId = requestId;
        this.customerId = customerId;
        this.requestType = requestType;
        this.content = content;
        this.status = status;
        this.createdAt = createdAt;
        this.sender = "Client"; // Default
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getRequestType() { return requestType; }
    public void setRequestType(String requestType) { this.requestType = requestType; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
}
