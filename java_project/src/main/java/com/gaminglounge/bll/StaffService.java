package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.StaffDAL;
import com.gaminglounge.model.Staff;

public class StaffService {
    private StaffDAL staffDAL = new StaffDAL();

    public List<Staff> getAllStaff() {
        return staffDAL.getAllStaff();
    }

    public boolean addStaff(Staff staff, String username, String password, String email) {
        // Basic validation
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        return staffDAL.addStaff(staff, username, password, email);
    }

    public boolean updateStaff(Staff staff) {
        return staffDAL.updateStaff(staff);
    }

    public boolean deleteStaff(int staffId) {
        return staffDAL.deleteStaff(staffId);
    }

    public Staff getStaffByUserId(int userId) {
        return staffDAL.getStaffByUserId(userId);
    }
}
