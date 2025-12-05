package com.gaminglounge.gui;

import java.awt.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.gaminglounge.bll.OrderService;
import com.gaminglounge.model.Order;
import com.gaminglounge.model.OrderDetail;

public class OrderManagementPanel extends JPanel {
    private OrderService orderService;
    private JTable orderTable;
    private DefaultTableModel tableModel;

    public OrderManagementPanel() {
        orderService = new OrderService();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 0, 0, 0)); // Top padding only as it's inside a tab

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton refreshButton = createToolbarButton("Làm mới", null);
        JButton detailsButton = createToolbarButton("Xem chi tiết", new Color(0, 122, 204));

        refreshButton.addActionListener(e -> loadData());
        detailsButton.addActionListener(e -> showDetailsDialog());

        toolBar.add(refreshButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(detailsButton);
        
        add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Khách hàng", "Nhân viên", "Ngày đặt", "Tổng tiền", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(tableModel);
        orderTable.setRowHeight(25);
        orderTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        orderTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        add(scrollPane, BorderLayout.CENTER);

        loadData();
    }
    
    private JButton createToolbarButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (bg != null) {
            btn.setBackground(bg);
            btn.setForeground(Color.WHITE);
        }
        return btn;
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Order> list = orderService.getAllOrders();
        for (Order o : list) {
            tableModel.addRow(new Object[]{
                o.getOrderId(),
                o.getCustomerName(),
                o.getStaffName(),
                o.getOrderDate(),
                o.getTotalAmount(),
                o.getStatus()
            });
        }
    }

    private void showDetailsDialog() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xem chi tiết.");
            return;
        }

        int orderId = (int) tableModel.getValueAt(selectedRow, 0);
        List<OrderDetail> details = orderService.getOrderDetails(orderId);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn #" + orderId, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(this);

        String[] columnNames = {"Sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"};
        DefaultTableModel detailModel = new DefaultTableModel(columnNames, 0);
        
        for (OrderDetail d : details) {
            detailModel.addRow(new Object[]{
                d.getProductName(),
                d.getQuantity(),
                d.getUnitPrice(),
                d.getSubTotal()
            });
        }

        JTable detailTable = new JTable(detailModel);
        detailTable.setRowHeight(25);
        detailTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        detailTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(detailTable);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        dialog.add(scrollPane, BorderLayout.CENTER);
        
        JButton closeButton = new JButton("Đóng");
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(closeButton);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}
