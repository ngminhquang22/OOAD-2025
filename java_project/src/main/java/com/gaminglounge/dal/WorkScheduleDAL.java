package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.WorkSchedule;
import com.gaminglounge.utils.DatabaseHelper;

public class WorkScheduleDAL {

    public List<WorkSchedule> getAllSchedules() {
        List<WorkSchedule> list = new ArrayList<>();
        String sql = "SELECT ws.*, s.FullName FROM WorkSchedules ws " +
                     "JOIN Staff s ON ws.StaffID = s.StaffID " +
                     "ORDER BY ws.WorkDate DESC, ws.ShiftStart ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                WorkSchedule ws = new WorkSchedule();
                ws.setScheduleId(rs.getInt("ScheduleID"));
                ws.setStaffId(rs.getInt("StaffID"));
                ws.setStaffName(rs.getString("FullName"));
                ws.setWorkDate(rs.getDate("WorkDate"));
                ws.setShiftStart(rs.getTime("ShiftStart"));
                ws.setShiftEnd(rs.getTime("ShiftEnd"));
                ws.setNote(rs.getString("Note"));
                list.add(ws);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<WorkSchedule> getSchedulesByDate(Date date) {
        List<WorkSchedule> list = new ArrayList<>();
        String sql = "SELECT ws.*, s.FullName FROM WorkSchedules ws " +
                     "JOIN Staff s ON ws.StaffID = s.StaffID " +
                     "WHERE ws.WorkDate = ? " +
                     "ORDER BY ws.ShiftStart ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, date);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WorkSchedule ws = new WorkSchedule();
                    ws.setScheduleId(rs.getInt("ScheduleID"));
                    ws.setStaffId(rs.getInt("StaffID"));
                    ws.setStaffName(rs.getString("FullName"));
                    ws.setWorkDate(rs.getDate("WorkDate"));
                    ws.setShiftStart(rs.getTime("ShiftStart"));
                    ws.setShiftEnd(rs.getTime("ShiftEnd"));
                    ws.setNote(rs.getString("Note"));
                    list.add(ws);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<WorkSchedule> getSchedulesByDateRange(Date startDate, Date endDate) {
        List<WorkSchedule> list = new ArrayList<>();
        String sql = "SELECT ws.*, s.FullName FROM WorkSchedules ws " +
                     "JOIN Staff s ON ws.StaffID = s.StaffID " +
                     "WHERE ws.WorkDate BETWEEN ? AND ? " +
                     "ORDER BY ws.WorkDate ASC, ws.ShiftStart ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, startDate);
            pstmt.setDate(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    WorkSchedule ws = new WorkSchedule();
                    ws.setScheduleId(rs.getInt("ScheduleID"));
                    ws.setStaffId(rs.getInt("StaffID"));
                    ws.setStaffName(rs.getString("FullName"));
                    ws.setWorkDate(rs.getDate("WorkDate"));
                    ws.setShiftStart(rs.getTime("ShiftStart"));
                    ws.setShiftEnd(rs.getTime("ShiftEnd"));
                    ws.setNote(rs.getString("Note"));
                    list.add(ws);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addSchedule(WorkSchedule ws) {
        String sql = "INSERT INTO WorkSchedules (StaffID, WorkDate, ShiftStart, ShiftEnd, Note) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ws.getStaffId());
            pstmt.setDate(2, ws.getWorkDate());
            pstmt.setTime(3, ws.getShiftStart());
            pstmt.setTime(4, ws.getShiftEnd());
            pstmt.setString(5, ws.getNote());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateSchedule(WorkSchedule ws) {
        String sql = "UPDATE WorkSchedules SET StaffID=?, WorkDate=?, ShiftStart=?, ShiftEnd=?, Note=? WHERE ScheduleID=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, ws.getStaffId());
            pstmt.setDate(2, ws.getWorkDate());
            pstmt.setTime(3, ws.getShiftStart());
            pstmt.setTime(4, ws.getShiftEnd());
            pstmt.setString(5, ws.getNote());
            pstmt.setInt(6, ws.getScheduleId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteSchedule(int scheduleId) {
        String sql = "DELETE FROM WorkSchedules WHERE ScheduleID=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scheduleId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Check for overlap for a specific staff on a specific date
    public boolean hasOverlap(int staffId, Date date, Time start, Time end, int excludeScheduleId) {
        String sql = "SELECT COUNT(*) FROM WorkSchedules " +
                     "WHERE StaffID = ? AND WorkDate = ? AND ScheduleID != ? " +
                     "AND ((ShiftStart < ? AND ShiftEnd > ?) OR (ShiftStart >= ? AND ShiftStart < ?))";
        // Logic: New Start < Existing End AND New End > Existing Start
        // Simplified overlap check: (StartA <= EndB) and (EndA >= StartB)
        // But strictly: (StartA < EndB) and (EndA > StartB)
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, staffId);
            pstmt.setDate(2, date);
            pstmt.setInt(3, excludeScheduleId);
            pstmt.setTime(4, end);
            pstmt.setTime(5, start);
            pstmt.setTime(6, start); // Redundant check logic in SQL above is a bit mixed, let's fix logic
            
            // Correct Overlap Logic:
            // Existing: [S_e, E_e]
            // New: [S_n, E_n]
            // Overlap if: S_n < E_e AND E_n > S_e
            
            String correctSql = "SELECT COUNT(*) FROM WorkSchedules " +
                                "WHERE StaffID = ? AND WorkDate = ? AND ScheduleID != ? " +
                                "AND ShiftStart < ? AND ShiftEnd > ?";
            
            try (PreparedStatement p2 = conn.prepareStatement(correctSql)) {
                p2.setInt(1, staffId);
                p2.setDate(2, date);
                p2.setInt(3, excludeScheduleId);
                p2.setTime(4, end);
                p2.setTime(5, start);
                
                try (ResultSet rs = p2.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
