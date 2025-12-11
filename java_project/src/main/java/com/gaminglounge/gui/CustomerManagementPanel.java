package com.gaminglounge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.gaminglounge.bll.CustomerService;
import com.gaminglounge.bll.StaffService;
import com.gaminglounge.bll.ServiceRequestService;
import com.gaminglounge.model.Customer;
import com.gaminglounge.model.Staff;
import com.gaminglounge.model.User;
import com.gaminglounge.model.ServiceRequest;

import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.Timer;

public class CustomerManagementPanel extends JPanel {
    private CustomerService customerService;
    private StaffService staffService;
    private ServiceRequestService requestService;
    private User currentUser;
    
    // Customer List Components
    private JTable customerTable;
    private DefaultTableModel tableModel;

    // Chat Components
    private JTable chatTable;
    private DefaultTableModel chatModel;
    private Timer chatRefreshTimer;

    public CustomerManagementPanel(User user) {
        this.currentUser = user;
        this.customerService = new CustomerService();
        this.staffService = new StaffService();
        this.requestService = new ServiceRequestService();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabbedPane.addTab("Danh sách khách hàng", createCustomerListPanel());
        tabbedPane.addTab("Tin nhắn / Hỗ trợ", createChatPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCustomerListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton addButton = createToolbarButton("Thêm khách hàng", new Color(0, 122, 204));
        JButton editButton = createToolbarButton("Sửa", null);
        JButton deleteButton = createToolbarButton("Xóa", new Color(231, 76, 60));
        JButton refreshButton = createToolbarButton("Làm mới", null);
        JButton topUpButton = createToolbarButton("Nạp tiền", new Color(46, 204, 113));
        JButton extendButton = createToolbarButton("Gia hạn", new Color(155, 89, 182));
        JButton chatButton = createToolbarButton("Gửi tin nhắn", new Color(241, 196, 15));

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteCustomer());
        refreshButton.addActionListener(e -> loadData());
        topUpButton.addActionListener(e -> showTopUpDialog());
        extendButton.addActionListener(e -> showExtendDialog());
        chatButton.addActionListener(e -> showChatDialog());

        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(refreshButton);
        toolBar.addSeparator(new Dimension(20, 0));
        toolBar.add(topUpButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(extendButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(chatButton);
        
        panel.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Họ tên", "Tài khoản", "Email", "SĐT", "Số dư", "Hạng TV", "Giờ còn lại", "Máy đang dùng"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        customerTable = new JTable(tableModel);
        customerTable.setRowHeight(25);
        customerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        customerTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panel.add(new JScrollPane(customerTable), BorderLayout.CENTER);
        
        loadData();
        return panel;
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header
        JLabel title = new JLabel("Tin nhắn từ khách hàng");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setBorder(new EmptyBorder(0, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        // Table
        String[] cols = {"ID", "Khách hàng", "Nội dung", "Thời gian", "CustomerID"};
        chatModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        chatTable = new JTable(chatModel);
        chatTable.setRowHeight(30);
        chatTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Hide CustomerID column
        chatTable.getColumnModel().getColumn(4).setMinWidth(0);
        chatTable.getColumnModel().getColumn(4).setMaxWidth(0);
        chatTable.getColumnModel().getColumn(4).setWidth(0);
        
        // Double click to reply/mark read
        chatTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = chatTable.getSelectedRow();
                    if (row != -1) {
                        int id = (int) chatTable.getValueAt(row, 0);
                        String customer = (String) chatTable.getValueAt(row, 1);
                        String content = (String) chatTable.getValueAt(row, 2);
                        int customerId = (int) chatTable.getValueAt(row, 4);
                        showReplyDialog(id, customerId, customer, content);
                    }
                }
            }
        });

        panel.add(new JScrollPane(chatTable), BorderLayout.CENTER);

        // Auto Refresh every 3 seconds
        chatRefreshTimer = new Timer(3000, e -> loadChats());
        chatRefreshTimer.start();

        loadChats();
        return panel;
    }

    private void loadChats() {
        List<ServiceRequest> list = requestService.getPendingChats();
        chatModel.setRowCount(0);
        for (ServiceRequest r : list) {
            chatModel.addRow(new Object[]{
                r.getRequestId(),
                r.getCustomerName(),
                r.getContent(),
                r.getCreatedAt(),
                r.getCustomerId()
            });
        }
    }

    private void showReplyDialog(int requestId, int customerId, String customerName, String content) {
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
        JButton sendBtn = new JButton("Gửi & Hoàn tất");
        
        sendBtn.addActionListener(e -> {
            String reply = replyField.getText().trim();
            if (!reply.isEmpty()) {
                requestService.sendReply(customerId, reply);
            }
            // Mark as completed (read)
            requestService.completeRequest(requestId);
            dialog.dispose();
            loadChats();
        });

        inputPanel.add(new JLabel("Trả lời (Admin):"), BorderLayout.NORTH);
        inputPanel.add(replyField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.SOUTH);

        dialog.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        dialog.add(inputPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (chatRefreshTimer != null) chatRefreshTimer.stop();
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
        List<Customer> list = customerService.getAllCustomers();
        for (Customer c : list) {
            tableModel.addRow(new Object[]{
                c.getCustomerId(),
                c.getFullName(),
                c.getUsername(),
                c.getEmail(),
                c.getPhoneNumber(),
                c.getBalance(),
                c.getMembershipLevel(),
                formatTime(c.getRemainingTimeMinutes()),
                c.getCurrentMachine() == null ? "" : c.getCurrentMachine()
            });
        }
    }

    private String formatTime(int minutes) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format("%02d:%02d:00", h, m);
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm khách hàng", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField emailField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField balanceField = new JTextField("0.00");
        String[] levels = {"Thường", "VIP", "VVIP"};
        JComboBox<String> levelBox = new JComboBox<>(levels);
        JTextField timeField = new JTextField("0");

        addFormField(formPanel, gbc, 0, "Tài khoản:", usernameField);
        addFormField(formPanel, gbc, 1, "Mật khẩu:", passwordField);
        addFormField(formPanel, gbc, 2, "Email:", emailField);
        addFormField(formPanel, gbc, 3, "Họ tên:", nameField);
        addFormField(formPanel, gbc, 4, "SĐT:", phoneField);
        addFormField(formPanel, gbc, 5, "Số dư ban đầu:", balanceField);
        addFormField(formPanel, gbc, 6, "Hạng thành viên:", levelBox);
        addFormField(formPanel, gbc, 7, "Giờ chơi (phút):", timeField);

        dialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Lưu");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 122, 204));
        saveButton.setForeground(Color.WHITE);
        
        saveButton.addActionListener(e -> {
            try {
                Customer c = new Customer();
                c.setFullName(nameField.getText());
                c.setPhoneNumber(phoneField.getText());
                c.setBalance(new BigDecimal(balanceField.getText()));
                c.setMembershipLevel((String) levelBox.getSelectedItem());
                c.setRemainingTimeMinutes(Integer.parseInt(timeField.getText()));

                if (customerService.addCustomer(c, usernameField.getText(), new String(passwordField.getPassword()), emailField.getText())) {
                    JOptionPane.showMessageDialog(dialog, "Thêm thành công!");
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Thêm thất bại.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi dữ liệu: " + ex.getMessage());
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        btnPanel.add(saveButton);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void addFormField(JPanel panel, GridBagConstraints gbc, int row, String label, JComponent field) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(lbl, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(field, gbc);
    }

    private void showEditDialog() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để sửa.");
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentPhone = (String) tableModel.getValueAt(selectedRow, 4);
        String currentLevel = (String) tableModel.getValueAt(selectedRow, 6);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa thông tin khách hàng", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JTextField nameField = new JTextField(currentName);
        JTextField phoneField = new JTextField(currentPhone);
        String[] levels = {"Thường", "VIP", "VVIP"};
        JComboBox<String> levelBox = new JComboBox<>(levels);
        levelBox.setSelectedItem(currentLevel);

        addFormField(formPanel, gbc, 0, "Họ tên:", nameField);
        addFormField(formPanel, gbc, 1, "SĐT:", phoneField);
        addFormField(formPanel, gbc, 2, "Hạng thành viên:", levelBox);
        
        dialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Cập nhật");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 122, 204));
        saveButton.setForeground(Color.WHITE);
        
        saveButton.addActionListener(e -> {
            try {
                Customer c = new Customer();
                c.setCustomerId(customerId);
                c.setFullName(nameField.getText());
                c.setPhoneNumber(phoneField.getText());
                c.setMembershipLevel((String) levelBox.getSelectedItem());

                if (customerService.updateCustomer(c)) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi dữ liệu: " + ex.getMessage());
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        btnPanel.add(saveButton);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void deleteCustomer() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để xóa.");
            return;
        }

        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa khách hàng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (customerService.deleteCustomer(customerId)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại.");
            }
        }
    }

    private void showTopUpDialog() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để nạp tiền.");
            return;
        }
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        
        String input = JOptionPane.showInputDialog(this, "Nhập số tiền nạp vào tài khoản:", "50000");
        if (input != null && !input.isEmpty()) {
            try {
                BigDecimal amount = new BigDecimal(input);
                
                int confirm = JOptionPane.showConfirmDialog(this, 
                    String.format("Nạp %.0f VNĐ vào tài khoản khách hàng?\nĐồng ý?", amount),
                    "Xác nhận nạp tiền", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    Staff currentStaff = staffService.getStaffByUserId(currentUser.getUserId());
                    int staffId = (currentStaff != null) ? currentStaff.getStaffId() : 1; // Fallback to 1 for testing if Admin has no staff record
                    
                    if (customerService.topUp(customerId, amount, staffId, "Nạp tiền tại quầy")) {
                        JOptionPane.showMessageDialog(this, "Nạp tiền thành công!");
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Nạp tiền thất bại.");
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ.");
            }
        }
    }

    private void showExtendDialog() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để gia hạn.");
            return;
        }
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        
        String input = JOptionPane.showInputDialog(this, "Nhập số tiền mua giờ (8000đ = 1 giờ):", "8000");
        if (input != null && !input.isEmpty()) {
            try {
                double amountVal = Double.parseDouble(input);
                int minutesToAdd = (int) ((amountVal / 8000) * 60);
                BigDecimal amount = BigDecimal.valueOf(amountVal);
                
                int confirm = JOptionPane.showConfirmDialog(this, 
                    String.format("Khách trả %.0f VNĐ.\nQuy đổi: %d phút.\nĐồng ý?", amountVal, minutesToAdd),
                    "Xác nhận gia hạn", JOptionPane.YES_NO_OPTION);
                
                if (confirm == JOptionPane.YES_OPTION) {
                    Staff currentStaff = staffService.getStaffByUserId(currentUser.getUserId());
                    int staffId = (currentStaff != null) ? currentStaff.getStaffId() : 1;
                    
                    if (customerService.extendService(customerId, minutesToAdd, amount, staffId)) {
                        JOptionPane.showMessageDialog(this, "Gia hạn thành công!");
                        loadData();
                    } else {
                        JOptionPane.showMessageDialog(this, "Gia hạn thất bại.");
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ.");
            }
        }
    }

    private void showChatDialog() {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để xem lịch sử chat.");
            return;
        }
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String customerName = (String) tableModel.getValueAt(selectedRow, 1);
        
        List<ServiceRequest> history = requestService.getCustomerHistory(customerId);
        
        StringBuilder sb = new StringBuilder();
        if (history.isEmpty()) {
            sb.append("Chưa có lịch sử chat nào.");
        } else {
            boolean hasChat = false;
            for (ServiceRequest req : history) {
                if ("Chat".equals(req.getRequestType())) {
                    hasChat = true;
                    sb.append("[").append(req.getCreatedAt()).append("] ");
                    
                    String sender = req.getSender();
                    if (sender == null) sender = "Client";
                    
                    if ("Admin".equals(sender)) {
                        sb.append("ADMIN: ");
                    } else {
                        sb.append("KHÁCH: ");
                    }
                    
                    sb.append(req.getContent()).append("\n");
                    sb.append("--------------------------------------------------\n");
                }
            }
            if (!hasChat) sb.append("Chưa có tin nhắn chat nào.");
        }
        
        JTextArea area = new JTextArea(sb.toString());
        area.setEditable(false);
        area.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Lịch sử Chat: " + customerName, true);
        dialog.setLayout(new BorderLayout());
        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        
        JButton closeBtn = new JButton("Đóng");
        closeBtn.addActionListener(e -> dialog.dispose());
        JPanel btnPanel = new JPanel();
        btnPanel.add(closeBtn);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
