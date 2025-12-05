package com.gaminglounge.model;

public class Computer {
    private int computerId;
    private String computerName;
    private String status;
    private String location;

    public Computer() {}

    public Computer(int computerId, String computerName, String status, String location) {
        this.computerId = computerId;
        this.computerName = computerName;
        this.status = status;
        this.location = location;
    }

    public int getComputerId() { return computerId; }
    public void setComputerId(int computerId) { this.computerId = computerId; }

    public String getComputerName() { return computerName; }
    public void setComputerName(String computerName) { this.computerName = computerName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
