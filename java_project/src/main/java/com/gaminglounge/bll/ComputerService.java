package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.ComputerDAL;
import com.gaminglounge.model.Computer;

public class ComputerService {
    private ComputerDAL computerDAL = new ComputerDAL();

    public List<Computer> getAllComputers() {
        return computerDAL.getAll();
    }

    public String toggleComputerStatus(int computerId, String currentStatus) {
        String newStatus = "Trống".equals(currentStatus) ? "Đang sử dụng" : "Trống";
        computerDAL.updateStatus(computerId, newStatus);
        return newStatus;
    }
}
