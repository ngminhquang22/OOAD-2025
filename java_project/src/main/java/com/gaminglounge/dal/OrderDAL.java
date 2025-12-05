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
}
