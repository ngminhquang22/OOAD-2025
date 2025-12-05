package com.gaminglounge.gui;

import java.awt.*;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.gaminglounge.bll.UserService;
import com.gaminglounge.model.User;

public class UserManagementPanel extends JPanel {
    private UserService userService;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagementPanel() {
        userService = new UserService();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton addButton = createToolbarButton("Thêm tài khoản", new Color(0, 122, 204));
        JButton editButton = createToolbarButton("Sửa", null);
        JButton deleteButton = createToolbarButton("Xóa", new Color(231, 76, 60));
        JButton statusButton = createToolbarButton("Khóa/Mở khóa", new Color(241, 196, 15));
        JButton pwdButton = createToolbarButton("Đổi mật khẩu", null);
        JButton refreshButton = createToolbarButton("Làm mới", null);

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteUser());
        statusButton.addActionListener(e -> toggleStatus());
        pwdButton.addActionListener(e -> showChangePasswordDialog());
        refreshButton.addActionListener(e -> loadData());

        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(20, 0));
        toolBar.add(statusButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(pwdButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(refreshButton);
        
        add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Tài khoản", "Email", "Vai trò", "Trạng thái"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setRowHeight(25);
        userTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        userTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(userTable);
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
        List<User> list = userService.getAllUsers();
        for (User u : list) {
            tableModel.addRow(new Object[]{
                u.getUserId(),
                u.getUsername(),
                u.getEmail(),
                u.getRoleName(),
                u.isActive() ? "Hoạt động" : "Đã khóa"
            });
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm tài khoản", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
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
        
        List<String> roles = userService.getAllRoles();
        JComboBox<String> roleBox = new JComboBox<>(roles.toArray(new String[0]));

        addFormField(formPanel, gbc, 0, "Tài khoản:", usernameField);
        addFormField(formPanel, gbc, 1, "Mật khẩu:", passwordField);
        addFormField(formPanel, gbc, 2, "Email:", emailField);
        addFormField(formPanel, gbc, 3, "Vai trò:", roleBox);

        dialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Lưu");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 122, 204));
        saveButton.setForeground(Color.WHITE);
        
        saveButton.addActionListener(e -> {
            if (userService.addUser(usernameField.getText(), new String(passwordField.getPassword()), emailField.getText(), (String) roleBox.getSelectedItem())) {
                JOptionPane.showMessageDialog(dialog, "Thêm thành công!");
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Thêm thất bại (Tài khoản đã tồn tại?).");
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
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để sửa.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        String currentUsername = (String) tableModel.getValueAt(selectedRow, 1);
        String currentEmail = (String) tableModel.getValueAt(selectedRow, 2);
        String currentRole = (String) tableModel.getValueAt(selectedRow, 3);
        boolean isActive = "Hoạt động".equals(tableModel.getValueAt(selectedRow, 4));

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa tài khoản: " + currentUsername, true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JTextField emailField = new JTextField(currentEmail);
        List<String> roles = userService.getAllRoles();
        JComboBox<String> roleBox = new JComboBox<>(roles.toArray(new String[0]));
        roleBox.setSelectedItem(currentRole);

        addFormField(formPanel, gbc, 0, "Email:", emailField);
        addFormField(formPanel, gbc, 1, "Vai trò:", roleBox);

        dialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Cập nhật");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 122, 204));
        saveButton.setForeground(Color.WHITE);
        
        saveButton.addActionListener(e -> {
            User u = new User();
            u.setUserId(userId);
            u.setEmail(emailField.getText());
            u.setActive(isActive); // Keep current status
            
            if (userService.updateUser(u, (String) roleBox.getSelectedItem())) {
                JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                dialog.dispose();
                loadData();
            } else {
                JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại.");
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(new EmptyBorder(0, 20, 20, 20));
        btnPanel.add(saveButton);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản để xóa.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa (khóa vĩnh viễn) tài khoản này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (userService.deleteUser(userId)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại.");
            }
        }
    }

    private void toggleStatus() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản.");
            return;
        }

        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        boolean isActive = "Hoạt động".equals(tableModel.getValueAt(selectedRow, 4));
        
        if (userService.toggleStatus(userId, isActive)) {
            JOptionPane.showMessageDialog(this, "Đã thay đổi trạng thái!");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Thất bại.");
        }
    }
    
    private void showChangePasswordDialog() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài khoản.");
            return;
        }
        int userId = (int) tableModel.getValueAt(selectedRow, 0);
        
        String newPwd = JOptionPane.showInputDialog(this, "Nhập mật khẩu mới:");
        if (newPwd != null && !newPwd.isEmpty()) {
            if (userService.changePassword(userId, newPwd)) {
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Thất bại.");
            }
        }
    }
}
