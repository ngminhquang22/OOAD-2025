package com.gaminglounge.bll;

import java.util.List;
import java.util.stream.Collectors; // Quan trọng: Để dùng stream().filter

import com.gaminglounge.dal.TransactionDAL;
import com.gaminglounge.model.Transaction;

public class TransactionService {
    
    // Khai báo DAL để kết nối CSDL
    private TransactionDAL transactionDAL = new TransactionDAL(); 

    // 1. Lấy tất cả giao dịch (Dùng cho Admin)
    public List<Transaction> getAllTransactions() {
        return transactionDAL.getAllTransactions();
    }

    // 2. Thêm giao dịch mới
    public boolean addTransaction(Transaction t) {
        return transactionDAL.addTransaction(t);
    }

    // 3. Tìm kiếm giao dịch
    public List<Transaction> searchTransactions(String keyword) {
        return transactionDAL.searchTransactions(keyword);
    }

    // 4. --- HÀM BẠN ĐANG THIẾU ---
    // Lấy lịch sử giao dịch của riêng 1 khách hàng
    public List<Transaction> getHistoryByCustomer(int customerId) {
        // Lấy tất cả lên
        List<Transaction> all = transactionDAL.getAllTransactions();
        
        // Lọc (Filter) chỉ lấy của khách hàng này
        // Và Sắp xếp (Sort) ngày mới nhất lên đầu
        return all.stream()
                .filter(t -> t.getCustomerId() == customerId)
                .sorted((t1, t2) -> t2.getTransactionDate().compareTo(t1.getTransactionDate())) 
                .collect(Collectors.toList());
    }
}