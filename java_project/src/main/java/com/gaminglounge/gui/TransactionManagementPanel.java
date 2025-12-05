package com.gaminglounge.gui;

import java.awt.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.gaminglounge.bll.TransactionService;
import com.gaminglounge.model.Transaction;

public class TransactionManagementPanel extends JPanel {
    // Transaction History Components
    private TransactionService transactionService;
    private JTable transactionTable;
    private DefaultTableModel tableModel;

    public TransactionManagementPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Tab 1: Lịch sử giao dịch (The original content)
        JPanel historyPanel = createTransactionHistoryPanel();
        tabbedPane.addTab("Lịch sử giao dịch", historyPanel);
        
        // Tab 2: Danh sách hóa đơn (OrderManagementPanel)
        tabbedPane.addTab("Danh sách hóa đơn", new OrderManagementPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        transactionService = new TransactionService();

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton refreshButton = new JButton("Làm mới");
        refreshButton.setFocusPainted(false);
        refreshButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        refreshButton.addActionListener(e -> loadData());
        
        toolBar.add(refreshButton);
        panel.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Khách hàng", "Nhân viên", "Số tiền", "Loại GD", "Ngày giờ", "Ghi chú"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(25);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadData();
        return panel;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Transaction> list = transactionService.getAllTransactions();
        for (Transaction t : list) {
            tableModel.addRow(new Object[]{
                t.getTransactionId(),
                t.getCustomerName(),
                t.getStaffName(),
                t.getAmount(),
                t.getTransactionType(),
                t.getTransactionDate(),
                t.getNote()
            });
        }
    }
}
