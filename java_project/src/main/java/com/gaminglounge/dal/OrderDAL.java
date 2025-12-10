package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.Order;
import com.gaminglounge.model.OrderDetail;
import com.gaminglounge.utils.DatabaseHelper;

public class OrderDAL {

    public List<Order> getAllOrders() {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT o.*, c.FullName as CustomerName, s.FullName as StaffName " +
                     "FROM Orders o " +
                     "JOIN Customers c ON o.CustomerID = c.CustomerID " +
                     "LEFT JOIN Staff s ON o.StaffID = s.StaffID " +
                     "ORDER BY o.OrderDate DESC";
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Order o = new Order(
                    rs.getInt("OrderID"),
                    rs.getInt("CustomerID"),
                    rs.getInt("StaffID"),
                    rs.getTimestamp("OrderDate"),
                    rs.getBigDecimal("TotalAmount"),
                    rs.getInt("PromotionID"),
                    rs.getString("Status")
                );
                o.setCustomerName(rs.getString("CustomerName"));
                o.setStaffName(rs.getString("StaffName"));
                list.add(o);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT od.*, p.ProductName " +
                     "FROM OrderDetails od " +
                     "JOIN Products p ON od.ProductID = p.ProductID " +
                     "WHERE od.OrderID = ?";
        
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, orderId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                OrderDetail od = new OrderDetail(
                    rs.getInt("OrderDetailID"),
                    rs.getInt("OrderID"),
                    rs.getInt("ProductID"),
                    rs.getInt("Quantity"),
                    rs.getBigDecimal("UnitPrice"),
                    rs.getBigDecimal("SubTotal")
                );
                od.setProductName(rs.getString("ProductName"));
                list.add(od);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean createOrder(Order order, List<OrderDetail> details) {
        Connection conn = null;
        PreparedStatement pstmtOrder = null;
        PreparedStatement pstmtDetail = null;
        PreparedStatement pstmtUpdateStock = null;
        PreparedStatement pstmtTransaction = null;
        PreparedStatement pstmtUpdateBalance = null;

        String sqlOrder = "INSERT INTO Orders (CustomerID, StaffID, OrderDate, TotalAmount, Status) VALUES (?, ?, NOW(), ?, 'Completed')";
        String sqlDetail = "INSERT INTO OrderDetails (OrderID, ProductID, Quantity, UnitPrice) VALUES (?, ?, ?, ?)";
        String sqlUpdateStock = "UPDATE Products SET StockQuantity = StockQuantity - ? WHERE ProductID = ? AND StockQuantity >= ?";
        
        // We also need to create a Transaction and Update Balance
        String sqlTransaction = "INSERT INTO Transactions (CustomerID, StaffID, Amount, TransactionType, TransactionDate, Note) VALUES (?, ?, ?, 'Mua hàng', NOW(), ?)";
        String sqlUpdateBalance = "UPDATE Customers SET Balance = Balance - ? WHERE CustomerID = ? AND Balance >= ?";

        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false); // Start Transaction

            // 1. Check Balance & Update Balance
            pstmtUpdateBalance = conn.prepareStatement(sqlUpdateBalance);
            pstmtUpdateBalance.setBigDecimal(1, order.getTotalAmount());
            pstmtUpdateBalance.setInt(2, order.getCustomerId());
            pstmtUpdateBalance.setBigDecimal(3, order.getTotalAmount());
            int balanceRows = pstmtUpdateBalance.executeUpdate();
            if (balanceRows == 0) throw new SQLException("Số dư không đủ hoặc không tìm thấy khách hàng.");

            // 2. Insert Order
            pstmtOrder = conn.prepareStatement(sqlOrder, Statement.RETURN_GENERATED_KEYS);
            pstmtOrder.setInt(1, order.getCustomerId());
            pstmtOrder.setInt(2, order.getStaffId());
            pstmtOrder.setBigDecimal(3, order.getTotalAmount());
            pstmtOrder.executeUpdate();
            
            int orderId = 0;
            try (ResultSet rs = pstmtOrder.getGeneratedKeys()) {
                if (rs.next()) orderId = rs.getInt(1);
            }

            // 3. Insert Details & Update Stock
            pstmtDetail = conn.prepareStatement(sqlDetail);
            pstmtUpdateStock = conn.prepareStatement(sqlUpdateStock);
            
            for (OrderDetail detail : details) {
                // Update Stock
                pstmtUpdateStock.setInt(1, detail.getQuantity());
                pstmtUpdateStock.setInt(2, detail.getProductId());
                pstmtUpdateStock.setInt(3, detail.getQuantity()); // Ensure enough stock
                int stockRows = pstmtUpdateStock.executeUpdate();
                if (stockRows == 0) throw new SQLException("Không đủ hàng trong kho cho sản phẩm ID: " + detail.getProductId());

                // Insert Detail
                pstmtDetail.setInt(1, orderId);
                pstmtDetail.setInt(2, detail.getProductId());
                pstmtDetail.setInt(3, detail.getQuantity());
                pstmtDetail.setBigDecimal(4, detail.getUnitPrice());
                // SubTotal is generated, so we don't insert it
                pstmtDetail.addBatch();
            }
            pstmtDetail.executeBatch();

            // 4. Create Transaction
            pstmtTransaction = conn.prepareStatement(sqlTransaction);
            pstmtTransaction.setInt(1, order.getCustomerId());
            pstmtTransaction.setInt(2, order.getStaffId());
            pstmtTransaction.setBigDecimal(3, order.getTotalAmount().negate()); // Negative for spending
            pstmtTransaction.setString(4, "Mua hàng (Order #" + orderId + ")");
            pstmtTransaction.executeUpdate();

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            return false;
        } finally {
            try {
                if (pstmtOrder != null) pstmtOrder.close();
                if (pstmtDetail != null) pstmtDetail.close();
                if (pstmtUpdateStock != null) pstmtUpdateStock.close();
                if (pstmtTransaction != null) pstmtTransaction.close();
                if (pstmtUpdateBalance != null) pstmtUpdateBalance.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
