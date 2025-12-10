package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.Timekeeping;
import com.gaminglounge.utils.DatabaseHelper;

public class TimekeepingDAL {

    public List<Timekeeping> getAll() {
        List<Timekeeping> list = new ArrayList<>();
        String sql = "SELECT t.*, s.FullName FROM Timekeeping t " +
                     "JOIN Staff s ON t.StaffID = s.StaffID " +
                     "ORDER BY t.CheckInTime DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Timekeeping tk = new Timekeeping(
                    rs.getInt("TimekeepingID"),
                    rs.getInt("StaffID"),
                    rs.getTimestamp("CheckInTime"),
                    rs.getTimestamp("CheckOutTime"),
                    rs.getString("Note")
                );
                tk.setStaffName(rs.getString("FullName"));
                list.add(tk);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean checkIn(int staffId) {
        String sql = "INSERT INTO Timekeeping (StaffID, CheckInTime) VALUES (?, NOW())";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkOut(int staffId) {
        // Find the latest open check-in for this staff
        String sql = "UPDATE Timekeeping SET CheckOutTime = NOW() " +
                     "WHERE StaffID = ? AND CheckOutTime IS NULL " +
                     "ORDER BY CheckInTime DESC LIMIT 1";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isCheckedIn(int staffId) {
        String sql = "SELECT COUNT(*) FROM Timekeeping WHERE StaffID = ? AND CheckOutTime IS NULL";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
