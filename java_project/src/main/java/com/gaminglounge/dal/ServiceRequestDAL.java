package com.gaminglounge.dal;

import com.gaminglounge.model.ServiceRequest;
import com.gaminglounge.utils.DatabaseHelper;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestDAL {

    public ServiceRequestDAL() {
        createTableIfNotExists();
    }

    private void createTableIfNotExists() {
        String sql = "CREATE TABLE IF NOT EXISTS ServiceRequests (" +
                     "RequestID INT PRIMARY KEY AUTO_INCREMENT, " +
                     "CustomerID INT NOT NULL, " +
                     "RequestType VARCHAR(50) NOT NULL, " +
                     "Content TEXT, " +
                     "Status VARCHAR(20) DEFAULT 'Pending', " +
                     "CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                     "Sender VARCHAR(20) DEFAULT 'Client', " +
                     "FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID))";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Try to add Sender column if it doesn't exist (migration)
            try {
                stmt.execute("ALTER TABLE ServiceRequests ADD COLUMN Sender VARCHAR(20) DEFAULT 'Client'");
            } catch (SQLException ex) {
                // Column likely exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createRequest(int customerId, String type, String content) {
        return createRequest(customerId, type, content, "Client");
    }

    public boolean createRequest(int customerId, String type, String content, String sender) {
        String sql = "INSERT INTO ServiceRequests (CustomerID, RequestType, Content, Status, Sender) VALUES (?, ?, ?, 'Pending', ?)";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, type);
            stmt.setString(3, content);
            stmt.setString(4, sender);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ServiceRequest> getPendingRequests() {
        List<ServiceRequest> list = new ArrayList<>();
        // Exclude 'Chat' from normal requests
        String sql = "SELECT r.*, c.FullName FROM ServiceRequests r " +
                     "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                     "WHERE r.Status = 'Pending' AND r.RequestType != 'Chat' ORDER BY r.CreatedAt ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ServiceRequest r = mapResultSetToRequest(rs);
                r.setCustomerName(rs.getString("FullName"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ServiceRequest> getPendingChats() {
        List<ServiceRequest> list = new ArrayList<>();
        // Only get chats from Client that are Pending
        String sql = "SELECT r.*, c.FullName FROM ServiceRequests r " +
                     "JOIN Customers c ON r.CustomerID = c.CustomerID " +
                     "WHERE r.Status = 'Pending' AND r.RequestType = 'Chat' AND r.Sender = 'Client' ORDER BY r.CreatedAt ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                ServiceRequest r = mapResultSetToRequest(rs);
                r.setCustomerName(rs.getString("FullName"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ServiceRequest> getRequestsByCustomer(int customerId) {
        List<ServiceRequest> list = new ArrayList<>();
        String sql = "SELECT * FROM ServiceRequests WHERE CustomerID = ? ORDER BY CreatedAt ASC";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(mapResultSetToRequest(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private ServiceRequest mapResultSetToRequest(ResultSet rs) throws SQLException {
        ServiceRequest r = new ServiceRequest(
            rs.getInt("RequestID"),
            rs.getInt("CustomerID"),
            rs.getString("RequestType"),
            rs.getString("Content"),
            rs.getString("Status"),
            rs.getTimestamp("CreatedAt")
        );
        try {
            r.setSender(rs.getString("Sender"));
        } catch (SQLException e) {
            r.setSender("Client"); // Fallback
        }
        return r;
    }

    public boolean completeRequest(int requestId) {
        String sql = "UPDATE ServiceRequests SET Status = 'Completed' WHERE RequestID = ?";
        try (Connection conn = DatabaseHelper.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, requestId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
