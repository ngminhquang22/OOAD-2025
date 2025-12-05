package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.InventoryReceipt;
import com.gaminglounge.model.InventoryReceiptDetail;
import com.gaminglounge.utils.DatabaseHelper;

public class InventoryDAL {

    public boolean createReceipt(InventoryReceipt receipt) {
        Connection conn = null;
        PreparedStatement pstmtReceipt = null;
        PreparedStatement pstmtDetail = null;
        PreparedStatement pstmtUpdateStock = null;

        String sqlReceipt = "INSERT INTO InventoryReceipts (StaffID, ReceiptType, Note) VALUES (?, ?, ?)";
        String sqlDetail = "INSERT INTO InventoryReceiptDetails (ReceiptID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
        
        // Update stock: Import adds, Export subtracts
        String sqlUpdateStock = "UPDATE Products SET StockQuantity = StockQuantity + ? WHERE ProductID = ?";

        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Insert Receipt
            pstmtReceipt = conn.prepareStatement(sqlReceipt, Statement.RETURN_GENERATED_KEYS);
            pstmtReceipt.setInt(1, receipt.getStaffId());
            pstmtReceipt.setString(2, receipt.getReceiptType());
            pstmtReceipt.setString(3, receipt.getNote());
            
            int affectedRows = pstmtReceipt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Creating receipt failed, no rows affected.");

            try (ResultSet generatedKeys = pstmtReceipt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    receipt.setReceiptId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating receipt failed, no ID obtained.");
                }
            }

            // 2. Insert Details & Update Stock
            pstmtDetail = conn.prepareStatement(sqlDetail);
            pstmtUpdateStock = conn.prepareStatement(sqlUpdateStock);

            for (InventoryReceiptDetail detail : receipt.getDetails()) {
                // Insert Detail
                pstmtDetail.setInt(1, receipt.getReceiptId());
                pstmtDetail.setInt(2, detail.getProductId());
                pstmtDetail.setInt(3, detail.getQuantity());
                pstmtDetail.setBigDecimal(4, detail.getUnitPrice());
                pstmtDetail.addBatch();

                // Update Stock
                int quantityChange = detail.getQuantity();
                if ("Export".equalsIgnoreCase(receipt.getReceiptType())) {
                    quantityChange = -quantityChange;
                }
                
                pstmtUpdateStock.setInt(1, quantityChange);
                pstmtUpdateStock.setInt(2, detail.getProductId());
                pstmtUpdateStock.addBatch();
            }

            pstmtDetail.executeBatch();
            pstmtUpdateStock.executeBatch();

            conn.commit(); // Commit Transaction
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return false;
        } finally {
            try {
                if (pstmtReceipt != null) pstmtReceipt.close();
                if (pstmtDetail != null) pstmtDetail.close();
                if (pstmtUpdateStock != null) pstmtUpdateStock.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List<InventoryReceipt> getAllReceipts() {
        List<InventoryReceipt> list = new ArrayList<>();
        String sql = "SELECT r.*, s.FullName as StaffName FROM InventoryReceipts r JOIN Staff s ON r.StaffID = s.StaffID ORDER BY ReceiptDate DESC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                InventoryReceipt r = new InventoryReceipt();
                r.setReceiptId(rs.getInt("ReceiptID"));
                r.setStaffId(rs.getInt("StaffID"));
                r.setStaffName(rs.getString("StaffName"));
                r.setReceiptDate(rs.getTimestamp("ReceiptDate"));
                r.setReceiptType(rs.getString("ReceiptType"));
                r.setNote(rs.getString("Note"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<InventoryReceiptDetail> getReceiptDetails(int receiptId) {
        List<InventoryReceiptDetail> list = new ArrayList<>();
        String sql = "SELECT d.*, p.ProductName FROM InventoryReceiptDetails d JOIN Products p ON d.ProductID = p.ProductID WHERE ReceiptID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, receiptId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                InventoryReceiptDetail d = new InventoryReceiptDetail();
                d.setDetailId(rs.getInt("DetailID"));
                d.setReceiptId(rs.getInt("ReceiptID"));
                d.setProductId(rs.getInt("ProductID"));
                d.setProductName(rs.getString("ProductName"));
                d.setQuantity(rs.getInt("Quantity"));
                d.setUnitPrice(rs.getBigDecimal("UnitPrice"));
                list.add(d);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
