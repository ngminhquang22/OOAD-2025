package com.gaminglounge.dal;

import com.gaminglounge.model.Transaction;
import com.gaminglounge.utils.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
}
