package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.Computer;
import com.gaminglounge.utils.DatabaseHelper;

public class ComputerDAL {

    public List<Computer> getAll() {
        List<Computer> computers = new ArrayList<>();
        String sql = "SELECT * FROM Computers";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                computers.add(new Computer(
                    rs.getInt("ComputerID"),
                    rs.getString("ComputerName"),
                    rs.getString("Status"),
                    rs.getString("Location")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return computers;
    }

    public boolean updateStatus(int computerId, String status) throws SQLException {
        String sql = "UPDATE Computers SET Status = ? WHERE ComputerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, computerId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean addComputer(Computer computer) {
        String sql = "INSERT INTO Computers (ComputerName, Status, Location) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, computer.getComputerName());
            pstmt.setString(2, computer.getStatus());
            pstmt.setString(3, computer.getLocation());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateComputer(Computer computer) {
        String sql = "UPDATE Computers SET ComputerName = ?, Location = ? WHERE ComputerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, computer.getComputerName());
            pstmt.setString(2, computer.getLocation());
            pstmt.setInt(3, computer.getComputerId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteComputer(int computerId) {
        String sql = "DELETE FROM Computers WHERE ComputerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, computerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
