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
import com.gaminglounge.model.Customer;
import com.gaminglounge.model.Staff;
import com.gaminglounge.model.User;

public class CustomerManagementPanel extends JPanel {
    private CustomerService customerService;
    private StaffService staffService;
    private User currentUser;
    private JTable customerTable;
    private DefaultTableModel tableModel;

    public CustomerManagementPanel(User user) {
        this.currentUser = user;
        this.customerService = new CustomerService();
        this.staffService = new StaffService();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

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
        JButton chatButton = createToolbarButton("Chat", new Color(241, 196, 15));

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
        
        add(toolBar, BorderLayout.NORTH);

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
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
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
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách hàng để chat.");
            return;
        }
        int customerId = (int) tableModel.getValueAt(selectedRow, 0);
        String customerName = (String) tableModel.getValueAt(selectedRow, 1);
        
        String message = JOptionPane.showInputDialog(this, "Gửi tin nhắn đến " + customerName + ":");
        if (message != null && !message.trim().isEmpty()) {
            Staff currentStaff = staffService.getStaffByUserId(currentUser.getUserId());
            int staffId = (currentStaff != null) ? currentStaff.getStaffId() : 1;
            
            if (customerService.sendMessage(customerId, staffId, message)) {
                JOptionPane.showMessageDialog(this, "Đã gửi tin nhắn.");
            } else {
                JOptionPane.showMessageDialog(this, "Gửi tin nhắn thất bại.");
            }
        }
    }
}
