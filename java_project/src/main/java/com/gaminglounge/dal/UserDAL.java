package com.gaminglounge.dal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.gaminglounge.model.User;
import com.gaminglounge.utils.DatabaseHelper;

public class UserDAL {

    public User getByUsername(String username) {
        // Allow fetching inactive users to check status later, but exclude deleted ones
        String sql = "SELECT u.*, r.RoleName FROM Users u " +
                     "JOIN Roles r ON u.RoleID = r.RoleID " +
                     "WHERE u.Username = ? AND (u.IsDeleted = 0 OR u.IsDeleted IS NULL)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User u = new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("PasswordHash"),
                    rs.getString("Email"),
                    rs.getInt("RoleID"),
                    rs.getString("RoleName")
                );
                // Use getInt for safety as discussed
                u.setActive(rs.getInt("IsActive") == 1);
                return u;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> getAllUsers(boolean includeDeleted) {
        List<User> list = new ArrayList<>();
        String sql = "SELECT u.*, r.RoleName FROM Users u JOIN Roles r ON u.RoleID = r.RoleID";
        
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int activeBit = rs.getInt("IsActive");
                boolean isActive = (activeBit == 1);
                
                // Handle IsDeleted column safely (it might not exist if migration failed silently, but we added it)
                boolean isDeleted = false;
                try {
                    isDeleted = rs.getInt("IsDeleted") == 1;
                } catch (SQLException e) {
                    // Column might not exist yet if initDB wasn't run or failed
                }

                if (!includeDeleted && isDeleted) {
                    continue;
                }

                User u = new User(
                    rs.getInt("UserID"),
                    rs.getString("Username"),
                    rs.getString("PasswordHash"),
                    rs.getString("Email"),
                    rs.getInt("RoleID"),
                    rs.getString("RoleName")
                );
                u.setActive(isActive);
                u.setDeleted(isDeleted);
                list.add(u);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addUser(User user) {
        String sql = "INSERT INTO Users (Username, PasswordHash, Email, RoleID, IsActive) VALUES (?, ?, ?, ?, 1)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPasswordHash());
            pstmt.setString(3, user.getEmail());
            pstmt.setInt(4, user.getRoleId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user) {
        String sql = "UPDATE Users SET Email = ?, RoleID = ?, IsActive = ? WHERE UserID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user.getEmail());
            pstmt.setInt(2, user.getRoleId());
            pstmt.setBoolean(3, user.isActive());
            pstmt.setInt(4, user.getUserId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean changePassword(int userId, String newPasswordHash) {
        String sql = "UPDATE Users SET PasswordHash = ? WHERE UserID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, newPasswordHash);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int userId, boolean isActive) {
        String sql = "UPDATE Users SET IsActive = ? WHERE UserID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBoolean(1, isActive);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteUser(int userId) {
        // Try Hard Delete first
        Connection conn = null;
        try {
            conn = DatabaseHelper.getConnection();
            conn.setAutoCommit(false);

            // 1. Delete from Customers if exists
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Customers WHERE UserID = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 2. Delete from Staff if exists
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Staff WHERE UserID = ?")) {
                ps.setInt(1, userId);
                ps.executeUpdate();
            }

            // 3. Delete from Users
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Users WHERE UserID = ?")) {
                ps.setInt(1, userId);
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    conn.commit();
                    return true;
                }
            }
            conn.rollback();
            return false;
        } catch (SQLException e) {
            // If hard delete fails (e.g. foreign key constraints), fallback to soft delete
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            System.err.println("Hard delete failed, falling back to soft delete: " + e.getMessage());
            return softDeleteUser(userId);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) {}
            }
        }
    }

    private boolean softDeleteUser(int userId) {
        String sql = "UPDATE Users SET IsDeleted = 1 WHERE UserID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAllRoles() {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT RoleName FROM Roles";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                roles.add(rs.getString("RoleName"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    public int getRoleIdByName(String roleName) {
        String sql = "SELECT RoleID FROM Roles WHERE RoleName = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, roleName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("RoleID");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
