package com.gaminglounge.gui;

import com.gaminglounge.bll.ServiceRequestService;
import com.gaminglounge.model.ServiceRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class AdminChatPanel extends JPanel {
    private ServiceRequestService requestService;
    private JTable chatTable;
    private DefaultTableModel chatModel;
    private Timer refreshTimer;

    public AdminChatPanel() {
        this.requestService = new ServiceRequestService();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Header
        JLabel title = new JLabel("Hỗ trợ trực tuyến (Chat)");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Khách hàng", "Nội dung", "Thời gian"};
        chatModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        chatTable = new JTable(chatModel);
        chatTable.setRowHeight(30);
        chatTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Double click to reply/mark read
        chatTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = chatTable.getSelectedRow();
                    if (row != -1) {
                        int id = (int) chatTable.getValueAt(row, 0);
                        String customer = (String) chatTable.getValueAt(row, 1);
                        String content = (String) chatTable.getValueAt(row, 2);
                        showReplyDialog(id, customer, content);
                    }
                }
            }
        });

        add(new JScrollPane(chatTable), BorderLayout.CENTER);

        // Auto Refresh every 3 seconds
        refreshTimer = new Timer(3000, e -> loadChats());
        refreshTimer.start();

        loadChats();
    }

    private void loadChats() {
        List<ServiceRequest> list = requestService.getPendingChats();
        chatModel.setRowCount(0);
        for (ServiceRequest r : list) {
            chatModel.addRow(new Object[]{
                r.getRequestId(),
                r.getCustomerName(),
                r.getContent(),
                r.getCreatedAt()
            });
        }
    }

    private void showReplyDialog(int requestId, String customerName, String content) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Chat với " + customerName, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JTextArea historyArea = new JTextArea("Khách hàng: " + content + "\n");
        historyArea.setEditable(false);
        historyArea.setLineWrap(true);
        historyArea.setWrapStyleWord(true);
        historyArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        JTextField replyField = new JTextField();
        JButton sendBtn = new JButton("Đã xem / Xong");
        
        sendBtn.addActionListener(e -> {
            // Mark as completed (read)
            requestService.completeRequest(requestId);
            dialog.dispose();
            loadChats();
        });

        inputPanel.add(new JLabel("Ghi chú (Admin):"), BorderLayout.NORTH);
        inputPanel.add(replyField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.SOUTH);

        dialog.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        dialog.add(inputPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (refreshTimer != null) refreshTimer.stop();
    }
}
