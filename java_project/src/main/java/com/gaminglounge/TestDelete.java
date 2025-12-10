package com.gaminglounge;

import com.gaminglounge.bll.UserService;
import com.gaminglounge.model.User;
import com.gaminglounge.utils.DatabaseHelper;
import java.util.List;

public class TestDelete {
    public static void main(String[] args) {
        DatabaseHelper.initDB();
        UserService userService = new UserService();

        // 1. Create a test user
        String testUser = "testdelete_" + System.currentTimeMillis();
        System.out.println("Creating user: " + testUser);
        boolean added = userService.addUser(testUser, "pass", "email@test.com", "Admin");
        if (!added) {
            System.err.println("Failed to add user");
            return;
        }

        // 2. Verify it exists and is active
        List<User> users = userService.getAllUsers(false);
        User found = users.stream().filter(u -> u.getUsername().equals(testUser)).findFirst().orElse(null);
        if (found == null) {
            System.err.println("User not found after creation!");
            return;
        }
        System.out.println("User found. ID: " + found.getUserId() + ", Active: " + found.isActive());

        // ADDED: Create a customer record and a SupportRequest to force Soft Delete
        try (java.sql.Connection conn = DatabaseHelper.getConnection()) {
             // 1. Add Customer
             int custId = -1;
             try (java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO Customers (UserID, FullName) VALUES (?, 'Test Customer')", java.sql.Statement.RETURN_GENERATED_KEYS)) {
                 ps.setInt(1, found.getUserId());
                 ps.executeUpdate();
                 java.sql.ResultSet rs = ps.getGeneratedKeys();
                 if (rs.next()) custId = rs.getInt(1);
             }
             
             // 2. Add SupportRequest linked to Customer
             if (custId != -1) {
                 try (java.sql.PreparedStatement ps = conn.prepareStatement("INSERT INTO SupportRequests (CustomerID, Subject) VALUES (?, 'Help')")) {
                     ps.setInt(1, custId);
                     ps.executeUpdate();
                     System.out.println("Added SupportRequest to force Soft Delete.");
                 }
             }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Delete the user
        System.out.println("Deleting user ID: " + found.getUserId());
        boolean deleted = userService.deleteUser(found.getUserId());
        if (!deleted) {
            System.err.println("Failed to delete user");
            return;
        }
        System.out.println("Delete returned true.");

        // 4. Verify it is GONE from active list
        users = userService.getAllUsers(false);
        found = users.stream().filter(u -> u.getUsername().equals(testUser)).findFirst().orElse(null);
        if (found != null) {
            System.err.println("ERROR: User still exists in active list!");
            System.out.println("User details: ID=" + found.getUserId() + ", Active=" + found.isActive());
        } else {
            System.out.println("SUCCESS: User is gone from active list.");
        }

        // 5. Verify it exists in inactive list (if soft deleted)
        users = userService.getAllUsers(true);
        found = users.stream().filter(u -> u.getUsername().equals(testUser)).findFirst().orElse(null);
        if (found != null) {
            System.out.println("User found in full list. Active: " + found.isActive());
        } else {
            System.out.println("User completely removed (Hard Delete).");
        }
    }
}
