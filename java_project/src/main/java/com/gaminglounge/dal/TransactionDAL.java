package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.gaminglounge.model.Transaction;
import com.gaminglounge.utils.DatabaseHelper;

public class TransactionDAL {

    public List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.FullName as CustomerName, s.FullName as StaffName " +
                     "FROM Transactions t " +
                     "JOIN Customers c ON t.CustomerID = c.CustomerID " +
                     "LEFT JOIN Staff s ON t.StaffID = s.StaffID " +
                     "ORDER BY t.TransactionDate DESC";
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Transaction t = new Transaction(
                    rs.getInt("TransactionID"),
                    rs.getInt("CustomerID"),
                    rs.getInt("StaffID"),
                    rs.getBigDecimal("Amount"),
                    rs.getTimestamp("TransactionDate"),
                    rs.getString("TransactionType"),
                    rs.getString("Note")
                );
                t.setCustomerName(rs.getString("CustomerName"));
                t.setStaffName(rs.getString("StaffName"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addTransaction(Transaction t) {
        String sql = "INSERT INTO Transactions (CustomerID, StaffID, Amount, TransactionType, Note) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, t.getCustomerId());
            if (t.getStaffId() > 0) {
                pstmt.setInt(2, t.getStaffId());
            } else {
                pstmt.setNull(2, Types.INTEGER);
            }
            pstmt.setBigDecimal(3, t.getAmount());
            pstmt.setString(4, t.getTransactionType());
            pstmt.setString(5, t.getNote());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Transaction> searchTransactions(String keyword) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT t.*, c.FullName as CustomerName, s.FullName as StaffName " +
                     "FROM Transactions t " +
                     "JOIN Customers c ON t.CustomerID = c.CustomerID " +
                     "LEFT JOIN Staff s ON t.StaffID = s.StaffID " +
                     "WHERE c.FullName LIKE ? OR t.TransactionID LIKE ? " +
                     "ORDER BY t.TransactionDate DESC";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchPattern = "%" + keyword + "%";
            pstmt.setString(1, searchPattern);
            pstmt.setString(2, searchPattern);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Transaction t = new Transaction(
                    rs.getInt("TransactionID"),
                    rs.getInt("CustomerID"),
                    rs.getInt("StaffID"),
                    rs.getBigDecimal("Amount"),
                    rs.getTimestamp("TransactionDate"),
                    rs.getString("TransactionType"),
                    rs.getString("Note")
                );
                t.setCustomerName(rs.getString("CustomerName"));
                t.setStaffName(rs.getString("StaffName"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public List<Transaction> getHistoryByCustomer(int customerId) {
        List<Transaction> all = getAllTransactions();
        
        // Lọc trong list lấy ra những giao dịch của customerId này
        return all.stream()
                .filter(t -> t.getCustomerId() == customerId)
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate())) // Sắp xếp mới nhất lên đầu
                .collect(Collectors.toList());
    }
}
