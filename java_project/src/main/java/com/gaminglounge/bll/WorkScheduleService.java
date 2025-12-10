package com.gaminglounge.bll;

import java.sql.Date;
import java.util.List;

import com.gaminglounge.dal.WorkScheduleDAL;
import com.gaminglounge.model.WorkSchedule;

public class WorkScheduleService {
    private WorkScheduleDAL dal;

    public WorkScheduleService() {
        dal = new WorkScheduleDAL();
    }

    public List<WorkSchedule> getAllSchedules() {
        return dal.getAllSchedules();
    }

    public List<WorkSchedule> getSchedulesByDate(Date date) {
        return dal.getSchedulesByDate(date);
    }

    public List<WorkSchedule> getSchedulesByDateRange(Date startDate, Date endDate) {
        return dal.getSchedulesByDateRange(startDate, endDate);
    }

    public boolean addSchedule(WorkSchedule ws) {
        // Business Rule: Check for overlap
        if (dal.hasOverlap(ws.getStaffId(), ws.getWorkDate(), ws.getShiftStart(), ws.getShiftEnd(), -1)) {
            throw new IllegalArgumentException("Ca làm việc bị trùng với lịch đã có của nhân viên này.");
        }
        return dal.addSchedule(ws);
    }

    public boolean updateSchedule(WorkSchedule ws) {
        // Business Rule: Check for overlap
        if (dal.hasOverlap(ws.getStaffId(), ws.getWorkDate(), ws.getShiftStart(), ws.getShiftEnd(), ws.getScheduleId())) {
            throw new IllegalArgumentException("Ca làm việc bị trùng với lịch đã có của nhân viên này.");
        }
        return dal.updateSchedule(ws);
    }

    public boolean deleteSchedule(int scheduleId) {
        return dal.deleteSchedule(scheduleId);
    }
}
