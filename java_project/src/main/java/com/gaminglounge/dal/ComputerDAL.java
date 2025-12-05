package com.gaminglounge.dal;

import com.gaminglounge.model.Computer;
import com.gaminglounge.utils.DatabaseHelper;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

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

    public void updateStatus(int computerId, String status) {
        String sql = "UPDATE Computers SET Status = ? WHERE ComputerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, computerId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
