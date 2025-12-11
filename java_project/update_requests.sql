
USE GamingLoungeDB;

CREATE TABLE IF NOT EXISTS ServiceRequests (
    RequestID INT PRIMARY KEY AUTO_INCREMENT,
    CustomerID INT NOT NULL,
    RequestType VARCHAR(50) NOT NULL, -- 'Deposit', 'Extend', 'Chat', 'Order'
    Content TEXT,
    Status VARCHAR(20) DEFAULT 'Pending', -- 'Pending', 'Completed'
    CreatedAt DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (CustomerID) REFERENCES Customers(CustomerID)
);
