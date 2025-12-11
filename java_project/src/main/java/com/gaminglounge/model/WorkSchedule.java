package com.gaminglounge.model;

import java.sql.Date;
import java.sql.Time;

public class WorkSchedule {
    private int scheduleId;
    private int staffId;
    private String staffName; // Helper for display
    private Date workDate;
    private Time shiftStart;
    private Time shiftEnd;
    private String note;

    public WorkSchedule() {}

    public WorkSchedule(int scheduleId, int staffId, Date workDate, Time shiftStart, Time shiftEnd, String note) {
        this.scheduleId = scheduleId;
        this.staffId = staffId;
        this.workDate = workDate;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        this.note = note;
    }

    public int getScheduleId() { return scheduleId; }
    public void setScheduleId(int scheduleId) { this.scheduleId = scheduleId; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public Date getWorkDate() { return workDate; }
    public void setWorkDate(Date workDate) { this.workDate = workDate; }

    public Time getShiftStart() { return shiftStart; }
    public void setShiftStart(Time shiftStart) { this.shiftStart = shiftStart; }

    public Time getShiftEnd() { return shiftEnd; }
    public void setShiftEnd(Time shiftEnd) { this.shiftEnd = shiftEnd; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return staffName + " (" + shiftStart + " - " + shiftEnd + ")";
    }
}
