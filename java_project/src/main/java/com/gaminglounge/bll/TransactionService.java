package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.TransactionDAL;
import com.gaminglounge.model.Transaction;

public class TransactionService {
    private TransactionDAL transactionDAL = new TransactionDAL();

    public List<Transaction> getAllTransactions() {
        return transactionDAL.getAllTransactions();
    }

    public boolean addTransaction(Transaction t) {
        return transactionDAL.addTransaction(t);
    }
}
