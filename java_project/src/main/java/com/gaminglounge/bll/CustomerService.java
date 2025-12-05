package com.gaminglounge.bll;

import java.math.BigDecimal;
import java.util.List;

import com.gaminglounge.dal.CustomerDAL;
import com.gaminglounge.dal.TransactionDAL;
import com.gaminglounge.model.Customer;
import com.gaminglounge.model.Transaction;

public class CustomerService {
    private CustomerDAL customerDAL = new CustomerDAL();
    private TransactionDAL transactionDAL = new TransactionDAL();

    public List<Customer> getAllCustomers() {
        return customerDAL.getAllCustomers();
    }

    public Customer getCustomerByUserId(int userId) {
        return customerDAL.getByUserId(userId);
    }

    public boolean addCustomer(Customer customer, String username, String password, String email) {
        // Basic validation
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        return customerDAL.addCustomer(customer, username, password, email);
    }

    public boolean updateCustomer(Customer customer) {
        return customerDAL.updateCustomer(customer);
    }

    public boolean deleteCustomer(int customerId) {
        return customerDAL.deleteCustomer(customerId);
    }

    public boolean updateBalance(int customerId, BigDecimal newBalance) {
        return customerDAL.updateBalance(customerId, newBalance);
    }

    public boolean topUp(int customerId, BigDecimal amount, int staffId, String note) {
        // 1. Get current balance (we need to fetch fresh to be safe, or assume caller knows)
        // Let's fetch fresh.
        // Since we don't have getById(customerId) exposed efficiently, we might rely on caller or add it.
        // But wait, updateBalance just sets the value.
        // We should probably use a "addToBalance" method in DAL to be atomic, but for now:
        // We will assume the caller has the correct "newBalance" or we fetch it.
        // Let's just use updateBalance with (current + amount).
        // But I don't have current balance here.
        // I'll implement a simple getBalance in DAL or just fetch the customer.
        // For now, I'll assume the caller calls this method INSTEAD of updateBalance for TopUps.
        // But I need the current balance to calculate new balance.
        
        // Let's add getCustomerById to DAL first? Or just iterate getAllCustomers? No, inefficient.
        // I'll add getCustomerById to CustomerDAL.
        
        // Actually, let's just do it in the GUI for now to minimize changes, 
        // OR better: The GUI already has the current balance.
        // So I will change this method signature to:
        // topUp(int customerId, BigDecimal currentBalance, BigDecimal amount, int staffId, String note)
        
        BigDecimal newBalance = amount; // Placeholder if we don't have current.
        // Wait, if I pass currentBalance, I can calculate new.
        
        return false; // Placeholder
    }
    
    public boolean processTopUp(int customerId, BigDecimal currentBalance, BigDecimal amount, int staffId, String note) {
        BigDecimal newBalance = currentBalance.add(amount);
        if (customerDAL.updateBalance(customerId, newBalance)) {
            Transaction t = new Transaction();
            t.setCustomerId(customerId);
            t.setStaffId(staffId);
            t.setAmount(amount);
            t.setTransactionType("Nạp tiền");
            t.setNote(note);
            transactionDAL.addTransaction(t);
            return true;
        }
        return false;
    }

    public boolean processTimeTopUp(int customerId, int currentMinutes, int minutesToAdd, BigDecimal amount, int staffId) {
        int newTime = currentMinutes + minutesToAdd;
        if (customerDAL.updateTime(customerId, newTime)) {
            Transaction t = new Transaction();
            t.setCustomerId(customerId);
            t.setStaffId(staffId);
            t.setAmount(amount);
            t.setTransactionType("Mua giờ");
            t.setNote("Mua " + minutesToAdd + " phút");
            transactionDAL.addTransaction(t);
            return true;
        }
        return false;
    }

    public boolean updateTime(int customerId, int minutes) {
        return customerDAL.updateTime(customerId, minutes);
    }
}
