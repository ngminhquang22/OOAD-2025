package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.Category;
import com.gaminglounge.model.Product;
import com.gaminglounge.utils.DatabaseHelper;

public class ProductDAL {

    public List<Product> getAllProducts() {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT p.*, c.CategoryName FROM Products p JOIN Categories c ON p.CategoryID = c.CategoryID";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Product p = new Product();
                p.setProductId(rs.getInt("ProductID"));
                p.setProductName(rs.getString("ProductName"));
                p.setCategoryId(rs.getInt("CategoryID"));
                p.setCategoryName(rs.getString("CategoryName"));
                p.setPrice(rs.getBigDecimal("Price"));
                p.setStockQuantity(rs.getInt("StockQuantity"));
                p.setImageUrl(rs.getString("ImageURL"));
                p.setService(rs.getBoolean("IsService"));
                list.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Category> getAllCategories() {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT * FROM Categories";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                list.add(new Category(rs.getInt("CategoryID"), rs.getString("CategoryName")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addProduct(Product p) {
        String sql = "INSERT INTO Products (ProductName, CategoryID, Price, StockQuantity, IsService) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, p.getProductName());
            pstmt.setInt(2, p.getCategoryId());
            pstmt.setBigDecimal(3, p.getPrice());
            pstmt.setInt(4, p.getStockQuantity());
            pstmt.setBoolean(5, p.isService());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateProduct(Product p) {
        String sql = "UPDATE Products SET ProductName=?, CategoryID=?, Price=?, StockQuantity=?, IsService=? WHERE ProductID=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, p.getProductName());
            pstmt.setInt(2, p.getCategoryId());
            pstmt.setBigDecimal(3, p.getPrice());
            pstmt.setInt(4, p.getStockQuantity());
            pstmt.setBoolean(5, p.isService());
            pstmt.setInt(6, p.getProductId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteProduct(int productId) {
        // Check dependencies first (OrderDetails, InventoryReceiptDetails)
        // For simplicity, we might just fail if used, or implement soft delete if schema supported it.
        // Schema doesn't have IsActive for Products, so hard delete.
        String sql = "DELETE FROM Products WHERE ProductID=?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); // Likely foreign key constraint violation
            return false;
        }
    }
}
