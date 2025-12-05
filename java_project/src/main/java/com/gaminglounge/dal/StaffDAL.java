package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.Staff;
import com.gaminglounge.utils.DatabaseHelper;

public class StaffDAL {

    public List<Staff> getAllStaff() {
        List<Staff> staffList = new ArrayList<>();
        String sql = "SELECT s.*, u.Username, u.Email FROM Staff s " +
                     "JOIN Users u ON s.UserID = u.UserID " +
                     "WHERE u.IsActive = 1";
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("StaffID"),
                    rs.getInt("UserID"),
                    rs.getString("FullName"),
                    rs.getString("Position"),
                    getSafeTime(rs, "ShiftStart"),
                    getSafeTime(rs, "ShiftEnd"),
                    rs.getBigDecimal("Salary")
                );
                staff.setUsername(rs.getString("Username"));
                staff.setEmail(rs.getString("Email"));
                staffList.add(staff);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return staffList;
    }

    private Time getSafeTime(ResultSet rs, String column) throws SQLException {
        try {
            return rs.getTime(column);
        } catch (SQLException | RuntimeException e) {
            // Handle invalid time like "24:00:00"
            String timeStr = rs.getString(column);
            if ("24:00:00".equals(timeStr)) {
                return Time.valueOf("00:00:00");
            }
            return null; // Or handle other cases
        }
    }

    public boolean addStaff(Staff staff, String username, String password, String email) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // Start transaction

            // 1. Get RoleID for 'Staff'
            int roleId = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT RoleID FROM Roles WHERE RoleName = 'Staff'")) {
                if (rs.next()) {
                    roleId = rs.getInt(1);
                } else {
                    throw new SQLException("Role 'Staff' not found.");
                }
            }

            // 2. Insert User
            int userId = -1;
            String userSql = "INSERT INTO Users (Username, PasswordHash, RoleID, Email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password); // In real app, hash this!
                pstmt.setInt(3, roleId);
                pstmt.setString(4, email);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
            }

            if (userId == -1) throw new SQLException("Failed to create user.");

            // 3. Insert Staff
            String staffSql = "INSERT INTO Staff (UserID, FullName, Position, ShiftStart, ShiftEnd, Salary) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(staffSql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, staff.getFullName());
                pstmt.setString(3, staff.getPosition());
                pstmt.setTime(4, staff.getShiftStart());
                pstmt.setTime(5, staff.getShiftEnd());
                pstmt.setBigDecimal(6, staff.getSalary());
                pstmt.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean updateStaff(Staff staff) {
        String sql = "UPDATE Staff SET FullName = ?, Position = ?, ShiftStart = ?, ShiftEnd = ?, Salary = ? WHERE StaffID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, staff.getFullName());
            pstmt.setString(2, staff.getPosition());
            pstmt.setTime(3, staff.getShiftStart());
            pstmt.setTime(4, staff.getShiftEnd());
            pstmt.setBigDecimal(5, staff.getSalary());
            pstmt.setInt(6, staff.getStaffId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteStaff(int staffId) {
        // Soft delete by setting User.IsActive = 0
        String sql = "UPDATE Users u JOIN Staff s ON u.UserID = s.UserID SET u.IsActive = 0 WHERE s.StaffID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Staff getStaffByUserId(int userId) {
        String sql = "SELECT s.*, u.Username, u.Email FROM Staff s " +
                     "JOIN Users u ON s.UserID = u.UserID " +
                     "WHERE s.UserID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Staff staff = new Staff(
                    rs.getInt("StaffID"),
                    rs.getInt("UserID"),
                    rs.getString("FullName"),
                    rs.getString("Position"),
                    getSafeTime(rs, "ShiftStart"),
                    getSafeTime(rs, "ShiftEnd"),
                    rs.getBigDecimal("Salary")
                );
                staff.setUsername(rs.getString("Username"));
                staff.setEmail(rs.getString("Email"));
                return staff;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
