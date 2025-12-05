package com.gaminglounge.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHelper {
    // Update these with your MySQL credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/GamingLoungeDB?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=UTF-8";
    private static final String USER = "root";
    private static final String PASS = ""; // Default XAMPP password is empty

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }

    public static void initDB() {
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                System.err.println("MySQL JDBC Driver not found! Please add the library to your classpath.");
                e.printStackTrace();
                return;
            }

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                // 1. Roles
                stmt.execute("CREATE TABLE IF NOT EXISTS Roles (" +
                        "RoleID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "RoleName VARCHAR(50) NOT NULL UNIQUE, " +
                        "Description VARCHAR(255))");

                // 2. Users
                stmt.execute("CREATE TABLE IF NOT EXISTS Users (" +
                        "UserID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "Username VARCHAR(50) NOT NULL UNIQUE, " +
                        "PasswordHash VARCHAR(255) NOT NULL, " +
                        "Email VARCHAR(100), " +
                        "RoleID INT NOT NULL, " +
                        "CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "IsActive BIT DEFAULT 1, " +
                        "FOREIGN KEY (RoleID) REFERENCES Roles(RoleID))");

                // 3. Customers
                stmt.execute("CREATE TABLE IF NOT EXISTS Customers (" +
                        "CustomerID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "UserID INT UNIQUE NOT NULL, " +
                        "FullName VARCHAR(100) NOT NULL, " +
                        "PhoneNumber VARCHAR(15), " +
                        "Balance DECIMAL(18, 2) DEFAULT 0, " +
                        "MembershipLevel VARCHAR(50) DEFAULT 'Standard', " +
                        "RemainingTimeMinutes INT DEFAULT 0, " +
                        "FOREIGN KEY (UserID) REFERENCES Users(UserID))");

                // 4. Staff
                stmt.execute("CREATE TABLE IF NOT EXISTS Staff (" +
                        "StaffID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "UserID INT UNIQUE NOT NULL, " +
                        "FullName VARCHAR(100) NOT NULL, " +
                        "Position VARCHAR(50), " +
                        "ShiftStart TIME, " +
                        "ShiftEnd TIME, " +
                        "Salary DECIMAL(18, 2), " +
                        "FOREIGN KEY (UserID) REFERENCES Users(UserID))");

                // 5. Computers
                stmt.execute("CREATE TABLE IF NOT EXISTS Computers (" +
                        "ComputerID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "ComputerName VARCHAR(50) NOT NULL UNIQUE, " +
                        "Status VARCHAR(20) DEFAULT 'Available', " +
                        "IPAddress VARCHAR(20), " +
                        "Location VARCHAR(100), " +
                        "HardwareSpecs TEXT)");

                // 6. Sessions
                stmt.execute("CREATE TABLE IF NOT EXISTS Sessions (" +
                        "SessionID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "CustomerID INT, " +
                        "ComputerID INT NOT NULL, " +
                        "StartTime DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "EndTime DATETIME, " +
                        "TotalAmount DECIMAL(18, 2), " +
                        "Status VARCHAR(20) DEFAULT 'Active', " +
                        "FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID), " +
                        "FOREIGN KEY (ComputerID) REFERENCES Computers(ComputerID))");

                // 7. Categories
                stmt.execute("CREATE TABLE IF NOT EXISTS Categories (" +
                        "CategoryID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "CategoryName VARCHAR(100) NOT NULL)");

                // 8. Products
                stmt.execute("CREATE TABLE IF NOT EXISTS Products (" +
                        "ProductID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "ProductName VARCHAR(100) NOT NULL, " +
                        "CategoryID INT NOT NULL, " +
                        "Price DECIMAL(18, 2) NOT NULL, " +
                        "StockQuantity INT DEFAULT 0, " +
                        "ImageURL VARCHAR(255), " +
                        "IsService BIT DEFAULT 0, " +
                        "FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID))");

                // 9. Promotions
                stmt.execute("CREATE TABLE IF NOT EXISTS Promotions (" +
                        "PromotionID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "PromotionName VARCHAR(100) NOT NULL, " +
                        "Description VARCHAR(255), " +
                        "DiscountType VARCHAR(20) DEFAULT 'Percentage', " +
                        "DiscountValue DECIMAL(18, 2) NOT NULL, " +
                        "StartDate DATETIME, " +
                        "EndDate DATETIME, " +
                        "MinOrderValue DECIMAL(18, 2) DEFAULT 0, " +
                        "IsActive BIT DEFAULT 1)");

                // 10. Orders
                stmt.execute("CREATE TABLE IF NOT EXISTS Orders (" +
                        "OrderID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "CustomerID INT, " +
                        "StaffID INT, " +
                        "OrderDate DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "TotalAmount DECIMAL(18, 2) DEFAULT 0, " +
                        "PromotionID INT, " +
                        "Status VARCHAR(20) DEFAULT 'Pending', " +
                        "FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID), " +
                        "FOREIGN KEY (StaffID) REFERENCES Staff(StaffID), " +
                        "FOREIGN KEY (PromotionID) REFERENCES Promotions(PromotionID))");

                // 11. OrderDetails
                stmt.execute("CREATE TABLE IF NOT EXISTS OrderDetails (" +
                        "OrderDetailID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "OrderID INT NOT NULL, " +
                        "ProductID INT NOT NULL, " +
                        "Quantity INT NOT NULL, " +
                        "UnitPrice DECIMAL(18, 2) NOT NULL, " +
                        "SubTotal DECIMAL(18, 2), " +
                        "FOREIGN KEY (OrderID) REFERENCES Orders(OrderID), " +
                        "FOREIGN KEY (ProductID) REFERENCES Products(ProductID))");

                // 12. Transactions
                stmt.execute("CREATE TABLE IF NOT EXISTS Transactions (" +
                        "TransactionID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "CustomerID INT NOT NULL, " +
                        "StaffID INT, " +
                        "Amount DECIMAL(18, 2) NOT NULL, " +
                        "TransactionDate DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "TransactionType VARCHAR(50) DEFAULT 'Deposit', " +
                        "Note VARCHAR(255), " +
                        "FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID), " +
                        "FOREIGN KEY (StaffID) REFERENCES Staff(StaffID))");

                // 13. Timekeeping
                stmt.execute("CREATE TABLE IF NOT EXISTS Timekeeping (" +
                        "TimekeepingID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "StaffID INT NOT NULL, " +
                        "CheckInTime DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "CheckOutTime DATETIME, " +
                        "Note VARCHAR(255), " +
                        "FOREIGN KEY (StaffID) REFERENCES Staff(StaffID))");

                // 14. InventoryReceipts
                stmt.execute("CREATE TABLE IF NOT EXISTS InventoryReceipts (" +
                        "ReceiptID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "StaffID INT NOT NULL, " +
                        "ReceiptDate DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "ReceiptType VARCHAR(20) NOT NULL, " +
                        "Note VARCHAR(255), " +
                        "FOREIGN KEY (StaffID) REFERENCES Staff(StaffID))");

                // 15. InventoryReceiptDetails
                stmt.execute("CREATE TABLE IF NOT EXISTS InventoryReceiptDetails (" +
                        "DetailID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "ReceiptID INT NOT NULL, " +
                        "ProductID INT NOT NULL, " +
                        "Quantity INT NOT NULL, " +
                        "UnitPrice DECIMAL(18, 2), " +
                        "FOREIGN KEY (ReceiptID) REFERENCES InventoryReceipts(ReceiptID), " +
                        "FOREIGN KEY (ProductID) REFERENCES Products(ProductID))");

                // 16. SupportRequests
                stmt.execute("CREATE TABLE IF NOT EXISTS SupportRequests (" +
                        "RequestID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "CustomerID INT NOT NULL, " +
                        "StaffID INT, " +
                        "Subject VARCHAR(100), " +
                        "Status VARCHAR(20) DEFAULT 'Open', " +
                        "CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID), " +
                        "FOREIGN KEY (StaffID) REFERENCES Staff(StaffID))");

                // 17. SupportMessages
                stmt.execute("CREATE TABLE IF NOT EXISTS SupportMessages (" +
                        "MessageID INT PRIMARY KEY AUTO_INCREMENT, " +
                        "RequestID INT NOT NULL, " +
                        "SenderUserID INT NOT NULL, " +
                        "Content TEXT, " +
                        "SentAt DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (RequestID) REFERENCES SupportRequests(RequestID), " +
                        "FOREIGN KEY (SenderUserID) REFERENCES Users(UserID))");

                // Seed Data
                seedData(conn);

                System.out.println("Database initialized successfully.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void seedData(Connection conn) throws SQLException {
        // Seed Roles
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM Roles")) {
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO Roles (RoleName, Description) VALUES " +
                        "('Quản trị viên', 'Quản trị hệ thống'), " +
                        "('Nhân viên', 'Nhân viên phục vụ'), " +
                        "('Hội viên', 'Khách hàng thành viên')");
            }
        }

        // Seed Admin
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Users WHERE Username = ?")) {
            pstmt.setString(1, "admin");
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                Statement stmt = conn.createStatement();
                ResultSet rsRole = stmt.executeQuery("SELECT RoleID FROM Roles WHERE RoleName = 'Quản trị viên'");
                if (rsRole.next()) {
                    int roleId = rsRole.getInt(1);
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO Users (Username, PasswordHash, RoleID, Email) VALUES (?, ?, ?, ?)")) {
                        insertStmt.setString(1, "admin");
                        insertStmt.setString(2, "admin123");
                        insertStmt.setInt(3, roleId);
                        insertStmt.setString(4, "admin@example.com");
                        insertStmt.executeUpdate();
                        System.out.println("Default admin created: admin/admin123");
                    }
                }
            }
        }

        // Seed Client
        try (PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM Users WHERE Username = ?")) {
            pstmt.setString(1, "client");
            ResultSet rs = pstmt.executeQuery();
            if (!rs.next()) {
                Statement stmt = conn.createStatement();
                ResultSet rsRole = stmt.executeQuery("SELECT RoleID FROM Roles WHERE RoleName = 'Hội viên'");
                if (rsRole.next()) {
                    int roleId = rsRole.getInt(1);
                    
                    // Insert User
                    int userId = -1;
                    try (PreparedStatement insertStmt = conn.prepareStatement(
                            "INSERT INTO Users (Username, PasswordHash, RoleID, Email) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                        insertStmt.setString(1, "client");
                        insertStmt.setString(2, "client123");
                        insertStmt.setInt(3, roleId);
                        insertStmt.setString(4, "client@example.com");
                        insertStmt.executeUpdate();
                        
                        ResultSet rsKey = insertStmt.getGeneratedKeys();
                        if (rsKey.next()) {
                            userId = rsKey.getInt(1);
                        }
                    }

                    // Insert Customer Profile
                    if (userId != -1) {
                        try (PreparedStatement insertCust = conn.prepareStatement(
                                "INSERT INTO Customers (UserID, FullName, Balance, RemainingTimeMinutes) VALUES (?, ?, ?, ?)")) {
                            insertCust.setInt(1, userId);
                            insertCust.setString(2, "Test Client");
                            insertCust.setBigDecimal(3, new java.math.BigDecimal("50.00"));
                            insertCust.setInt(4, 120);
                            insertCust.executeUpdate();
                            System.out.println("Default client created: client/client123");
                        }
                    }
                }
            }
        }

        // Seed Computers
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM Computers")) {
            if (rs.next() && rs.getInt(1) == 0) {
                for (int i = 1; i <= 10; i++) {
                    String name = String.format("PC-%02d", i);
                    stmt.execute("INSERT INTO Computers (ComputerName, Status, Location) VALUES " +
                            "('" + name + "', 'Trống', 'Main Hall')");
                }
                System.out.println("Seeded 10 computers.");
            }
        }
    }
}
