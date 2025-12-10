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
        Customer c = customerDAL.getCustomerById(customerId);
        if (c == null) return false;
        return processTopUp(customerId, c.getBalance(), amount, staffId, note);
    }

    public boolean extendService(int customerId, int minutes, BigDecimal cost, int staffId) {
        Customer c = customerDAL.getCustomerById(customerId);
        if (c == null) return false;
        return processTimeTopUp(customerId, c.getRemainingTimeMinutes(), minutes, cost, staffId);
    }

    public boolean sendMessage(int customerId, int senderUserId, String content) {
        return customerDAL.sendSupportMessage(customerId, senderUserId, content);
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

    public boolean chargeCustomer(int customerId, BigDecimal amount, int staffId, String note) {
        Customer c = customerDAL.getCustomerById(customerId);
        if (c == null) return false;
        if (c.getBalance().compareTo(amount) < 0) return false; // Not enough balance

        BigDecimal newBalance = c.getBalance().subtract(amount);
        if (customerDAL.updateBalance(customerId, newBalance)) {
            Transaction t = new Transaction();
            t.setCustomerId(customerId);
            t.setStaffId(staffId);
            t.setAmount(amount.negate()); // Negative amount for deduction
            t.setTransactionType("Thanh toán");
            t.setNote(note);
            transactionDAL.addTransaction(t);
            return true;
        }
        return false;
    }
}
