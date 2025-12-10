package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.TimekeepingDAL;
import com.gaminglounge.model.Timekeeping;

public class TimekeepingService {
    private TimekeepingDAL timekeepingDAL = new TimekeepingDAL();

    public List<Timekeeping> getAllTimekeeping() {
        return timekeepingDAL.getAll();
    }

    public boolean checkIn(int staffId) {
        if (timekeepingDAL.isCheckedIn(staffId)) {
            return false; // Already checked in
        }
        return timekeepingDAL.checkIn(staffId);
    }

    public boolean checkOut(int staffId) {
        if (!timekeepingDAL.isCheckedIn(staffId)) {
            return false; // Not checked in
        }
        return timekeepingDAL.checkOut(staffId);
    }
    
    public boolean isCheckedIn(int staffId) {
        return timekeepingDAL.isCheckedIn(staffId);
    }
}
