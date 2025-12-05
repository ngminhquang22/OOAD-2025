package com.gaminglounge.dal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.Customer;
import com.gaminglounge.utils.DatabaseHelper;

public class CustomerDAL {

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT c.*, u.Username, u.Email FROM Customers c " +
                     "JOIN Users u ON c.UserID = u.UserID " +
                     "WHERE u.IsActive = 1";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Customer c = new Customer(
                    rs.getInt("CustomerID"),
                    rs.getInt("UserID"),
                    rs.getString("FullName"),
                    rs.getString("PhoneNumber"),
                    rs.getBigDecimal("Balance"),
                    rs.getString("MembershipLevel"),
                    rs.getInt("RemainingTimeMinutes")
                );
                c.setUsername(rs.getString("Username"));
                c.setEmail(rs.getString("Email"));
                list.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Customer getByUserId(int userId) {
        String sql = "SELECT * FROM Customers WHERE UserID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Customer(
                    rs.getInt("CustomerID"),
                    rs.getInt("UserID"),
                    rs.getString("FullName"),
                    rs.getString("PhoneNumber"),
                    rs.getBigDecimal("Balance"),
                    rs.getString("MembershipLevel"),
                    rs.getInt("RemainingTimeMinutes")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addCustomer(Customer customer, String username, String password, String email) {
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            // 1. Get RoleID for 'Member'
            int roleId = 0;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT RoleID FROM Roles WHERE RoleName = 'Member'")) {
                if (rs.next()) {
                    roleId = rs.getInt(1);
                } else {
                    throw new SQLException("Role 'Member' not found.");
                }
            }

            // 2. Insert User
            int userId = -1;
            String userSql = "INSERT INTO Users (Username, PasswordHash, RoleID, Email) VALUES (?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(userSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, username);
                pstmt.setString(2, password);
                pstmt.setInt(3, roleId);
                pstmt.setString(4, email);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    userId = rs.getInt(1);
                }
            }

            if (userId == -1) throw new SQLException("Failed to create user.");

            // 3. Insert Customer
            String custSql = "INSERT INTO Customers (UserID, FullName, PhoneNumber, Balance, MembershipLevel, RemainingTimeMinutes) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(custSql)) {
                pstmt.setInt(1, userId);
                pstmt.setString(2, customer.getFullName());
                pstmt.setString(3, customer.getPhoneNumber());
                pstmt.setBigDecimal(4, customer.getBalance());
                pstmt.setString(5, customer.getMembershipLevel());
                pstmt.setInt(6, customer.getRemainingTimeMinutes());
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

    public boolean updateCustomer(Customer customer) {
        String sql = "UPDATE Customers SET FullName = ?, PhoneNumber = ?, MembershipLevel = ? WHERE CustomerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, customer.getFullName());
            pstmt.setString(2, customer.getPhoneNumber());
            pstmt.setString(3, customer.getMembershipLevel());
            pstmt.setInt(4, customer.getCustomerId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateBalance(int customerId, BigDecimal newBalance) {
        String sql = "UPDATE Customers SET Balance = ? WHERE CustomerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBigDecimal(1, newBalance);
            pstmt.setInt(2, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTime(int customerId, int minutes) {
        String sql = "UPDATE Customers SET RemainingTimeMinutes = ? WHERE CustomerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, minutes);
            pstmt.setInt(2, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteCustomer(int customerId) {
        // Soft delete via User.IsActive
        String sql = "UPDATE Users u JOIN Customers c ON u.UserID = c.UserID SET u.IsActive = 0 WHERE c.CustomerID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
