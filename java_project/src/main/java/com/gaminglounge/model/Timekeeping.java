package com.gaminglounge.model;

import java.sql.Timestamp;

public class Timekeeping {
    private int timekeepingId;
    private int staffId;
    private Timestamp checkInTime;
    private Timestamp checkOutTime;
    private String note;
    
    // Extra for display
    private String staffName;

    public Timekeeping() {}

    public Timekeeping(int timekeepingId, int staffId, Timestamp checkInTime, Timestamp checkOutTime, String note) {
        this.timekeepingId = timekeepingId;
        this.staffId = staffId;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.note = note;
    }

    public int getTimekeepingId() { return timekeepingId; }
    public void setTimekeepingId(int timekeepingId) { this.timekeepingId = timekeepingId; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public Timestamp getCheckInTime() { return checkInTime; }
    public void setCheckInTime(Timestamp checkInTime) { this.checkInTime = checkInTime; }

    public Timestamp getCheckOutTime() { return checkOutTime; }
    public void setCheckOutTime(Timestamp checkOutTime) { this.checkOutTime = checkOutTime; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }
}
