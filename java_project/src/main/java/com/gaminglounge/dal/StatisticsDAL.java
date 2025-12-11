package com.gaminglounge.dal;

import com.gaminglounge.utils.DatabaseHelper;
import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class StatisticsDAL {

    // Get total deposit revenue (Top-up for Services)
    public double getDepositRevenue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM(Amount) FROM Transactions WHERE TransactionType = 'Nạp tiền' AND DATE(TransactionDate) BETWEEN ? AND ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get total time sales revenue (Buying Time)
    public double getTimeSalesRevenue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM(Amount) FROM Transactions WHERE TransactionType = 'Mua giờ' AND DATE(TransactionDate) BETWEEN ? AND ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get total order revenue (Consumption from Balance) - For reference only
    public double getOrderRevenue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM(TotalAmount) FROM Orders WHERE Status = 'Completed' AND DATE(OrderDate) BETWEEN ? AND ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Get total session revenue (consumption) in a date range
    public double getSessionRevenue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM(TotalAmount) FROM Sessions WHERE Status = 'Hoàn thành' AND DATE(EndTime) BETWEEN ? AND ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    
    // Get revenue by date for chart (last 7 days for example, or range)
    // Returns Map<DateString, Double>
    public Map<String, Double> getDailyDepositRevenue(LocalDate startDate, LocalDate endDate) {
        Map<String, Double> data = new HashMap<>();
        String sql = "SELECT DATE(TransactionDate) as Date, SUM(Amount) as Total " +
                     "FROM Transactions " +
                     "WHERE TransactionType = 'Nạp tiền' AND DATE(TransactionDate) BETWEEN ? AND ? " +
                     "GROUP BY DATE(TransactionDate) " +
                     "ORDER BY DATE(TransactionDate)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                data.put(rs.getDate("Date").toString(), rs.getDouble("Total"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    // Inventory Statistics
    public int getImportCount(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM InventoryReceipts WHERE ReceiptType = 'Import' AND DATE(ReceiptDate) BETWEEN ? AND ?";
        return getCount(sql, startDate, endDate);
    }

    public int getExportCount(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) FROM InventoryReceipts WHERE ReceiptType = 'Export' AND DATE(ReceiptDate) BETWEEN ? AND ?";
        return getCount(sql, startDate, endDate);
    }

    public double getImportValue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT SUM(d.Quantity * d.UnitPrice) " +
                     "FROM InventoryReceiptDetails d " +
                     "JOIN InventoryReceipts r ON d.ReceiptID = r.ReceiptID " +
                     "WHERE r.ReceiptType = 'Import' AND DATE(r.ReceiptDate) BETWEEN ? AND ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getCount(String sql, LocalDate startDate, LocalDate endDate) {
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // Dashboard Overview Stats
    public int getNewCustomersCount(LocalDate date) {
        String sql = "SELECT COUNT(*) FROM Users u JOIN Customers c ON u.UserID = c.UserID WHERE DATE(u.CreatedAt) = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getComputerCountByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM Computers WHERE Status = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getTotalComputers() {
        String sql = "SELECT COUNT(*) FROM Computers";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getWarningComputersCount() {
        String sql = "SELECT COUNT(*) FROM Computers WHERE Status IN ('Maintenance', 'Broken', 'Error')";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
