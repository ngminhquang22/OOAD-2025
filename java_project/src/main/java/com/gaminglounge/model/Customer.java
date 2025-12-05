package com.gaminglounge.model;

import java.math.BigDecimal;

public class Customer {
    private int customerId;
    private int userId;
    private String fullName;
    private String phoneNumber;
    private BigDecimal balance;
    private String membershipLevel;
    private int remainingTimeMinutes;
    
    // Extra fields for display
    private String username;
    private String email;

    public Customer() {}

    public Customer(int customerId, int userId, String fullName, String phoneNumber, BigDecimal balance, String membershipLevel, int remainingTimeMinutes) {
        this.customerId = customerId;
        this.userId = userId;
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.balance = balance;
        this.membershipLevel = membershipLevel;
        this.remainingTimeMinutes = remainingTimeMinutes;
    }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }

    public String getMembershipLevel() { return membershipLevel; }
    public void setMembershipLevel(String membershipLevel) { this.membershipLevel = membershipLevel; }

    public int getRemainingTimeMinutes() { return remainingTimeMinutes; }
    public void setRemainingTimeMinutes(int remainingTimeMinutes) { this.remainingTimeMinutes = remainingTimeMinutes; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
