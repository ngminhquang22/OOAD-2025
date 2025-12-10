package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.ComputerDAL;
import com.gaminglounge.model.Computer;

public class ComputerService {
    private ComputerDAL computerDAL = new ComputerDAL();

    public List<Computer> getAllComputers() {
        return computerDAL.getAll();
    }

    public String toggleComputerStatus(int computerId, String currentStatus) throws java.sql.SQLException {
        String newStatus = "Trống".equals(currentStatus) ? "Đang sử dụng" : "Trống";
        boolean updated = computerDAL.updateStatus(computerId, newStatus);
        return updated ? newStatus : null;
    }

    public boolean setComputerStatus(int computerId, String status) throws java.sql.SQLException {
        return computerDAL.updateStatus(computerId, status);
    }

    public boolean addComputer(Computer computer) {
        if (computer.getComputerName() == null || computer.getComputerName().isEmpty()) return false;
        if (computer.getStatus() == null) computer.setStatus("Trống");
        return computerDAL.addComputer(computer);
    }

    public boolean updateComputer(Computer computer) {
        return computerDAL.updateComputer(computer);
    }

    public boolean deleteComputer(int computerId) {
        return computerDAL.deleteComputer(computerId);
    }
}
