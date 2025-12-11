package com.gaminglounge.gui;

import com.gaminglounge.bll.ServiceRequestService;
import com.gaminglounge.model.ServiceRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class ServiceRequestPanel extends JPanel {
    private ServiceRequestService requestService;
    private JTable table;
    private DefaultTableModel model;
    private Timer refreshTimer;

    public ServiceRequestPanel() {
        this.requestService = new ServiceRequestService();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton refreshBtn = new JButton("Làm mới");
        JButton completeBtn = new JButton("Hoàn thành / Đã xử lý");
        styleButton(refreshBtn, new Color(52, 152, 219));
        styleButton(completeBtn, new Color(46, 204, 113));

        refreshBtn.addActionListener(e -> loadRequests());
        completeBtn.addActionListener(e -> completeSelectedRequest());

        toolbar.add(refreshBtn);
        toolbar.add(completeBtn);
        add(toolbar, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Khách hàng", "Loại", "Nội dung", "Thời gian", "Trạng thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Auto Refresh every 5 seconds
        refreshTimer = new Timer(5000, e -> loadRequests());
        refreshTimer.start();

        loadRequests();
    }

    private void loadRequests() {
        List<ServiceRequest> list = requestService.getPendingRequests();
        model.setRowCount(0);
        for (ServiceRequest r : list) {
            model.addRow(new Object[]{
                r.getRequestId(),
                r.getCustomerName(),
                r.getRequestType(),
                r.getContent(),
                r.getCreatedAt(),
                r.getStatus()
            });
        }
    }

    private void completeSelectedRequest() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        
        int id = (int) table.getValueAt(row, 0);
        String type = (String) table.getValueAt(row, 2);
        String content = (String) table.getValueAt(row, 3);

        int confirm = JOptionPane.showConfirmDialog(this, 
            "Xác nhận đã xử lý yêu cầu này?\n" + type + ": " + content, 
            "Xác nhận", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (requestService.completeRequest(id)) {
                loadRequests();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi khi cập nhật trạng thái.");
            }
        }
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
    
    // Stop timer when panel is removed
    @Override
    public void removeNotify() {
        super.removeNotify();
        if (refreshTimer != null) refreshTimer.stop();
    }
}
