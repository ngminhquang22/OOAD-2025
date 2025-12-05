package com.gaminglounge.model;

import java.math.BigDecimal;
import java.sql.Time;

public class Staff {
    private int staffId;
    private int userId;
    private String fullName;
    private String position;
    private Time shiftStart;
    private Time shiftEnd;
    private BigDecimal salary;
    
    // Extra fields for display if needed (from User table)
    private String username;
    private String email;

    public Staff() {}

    public Staff(int staffId, int userId, String fullName, String position, Time shiftStart, Time shiftEnd, BigDecimal salary) {
        this.staffId = staffId;
        this.userId = userId;
        this.fullName = fullName;
        this.position = position;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.salary = salary;
    }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public Time getShiftStart() { return shiftStart; }
    public void setShiftStart(Time shiftStart) { this.shiftStart = shiftStart; }

    public Time getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(Time shiftEnd) { this.shiftEnd = shiftEnd; }

    public BigDecimal getSalary() { return salary; }
    public void setSalary(BigDecimal salary) { this.salary = salary; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
