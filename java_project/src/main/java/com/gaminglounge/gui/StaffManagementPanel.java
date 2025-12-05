package com.gaminglounge.gui;

import com.gaminglounge.bll.StaffService;
import com.gaminglounge.model.Staff;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.Time;
import java.util.List;

public class StaffManagementPanel extends JPanel {
    private StaffService staffService;
    private JTable staffTable;
    private DefaultTableModel tableModel;

    public StaffManagementPanel() {
        staffService = new StaffService();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton addButton = createToolbarButton("Thêm nhân viên", new Color(0, 122, 204));
        JButton editButton = createToolbarButton("Sửa", null);
        JButton deleteButton = createToolbarButton("Xóa", new Color(231, 76, 60));
        JButton refreshButton = createToolbarButton("Làm mới", null);

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteStaff());
        refreshButton.addActionListener(e -> loadData());

        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(refreshButton);
        
        add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Họ tên", "Tài khoản", "Email", "Vị trí", "Ca bắt đầu", "Ca kết thúc", "Lương"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        staffTable = new JTable(tableModel);
        staffTable.setRowHeight(25);
        staffTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        staffTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(staffTable);
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
        List<Staff> staffList = staffService.getAllStaff();
        for (Staff s : staffList) {
            tableModel.addRow(new Object[]{
                s.getStaffId(),
                s.getFullName(),
                s.getUsername(),
                s.getEmail(),
                s.getPosition(),
                s.getShiftStart(),
                s.getShiftEnd(),
                s.getSalary()
            });
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm nhân viên", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 450);
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
        JTextField positionField = new JTextField();
        JTextField startField = new JTextField("08:00:00");
        JTextField endField = new JTextField("17:00:00");
        JTextField salaryField = new JTextField("0.00");

        addFormField(formPanel, gbc, 0, "Tài khoản:", usernameField);
        addFormField(formPanel, gbc, 1, "Mật khẩu:", passwordField);
        addFormField(formPanel, gbc, 2, "Email:", emailField);
        addFormField(formPanel, gbc, 3, "Họ tên:", nameField);
        addFormField(formPanel, gbc, 4, "Vị trí:", positionField);
        addFormField(formPanel, gbc, 5, "Ca bắt đầu (HH:mm:ss):", startField);
        addFormField(formPanel, gbc, 6, "Ca kết thúc (HH:mm:ss):", endField);
        addFormField(formPanel, gbc, 7, "Lương:", salaryField);

        dialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Lưu");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 122, 204));
        saveButton.setForeground(Color.WHITE);
        
        saveButton.addActionListener(e -> {
            try {
                Staff s = new Staff();
                s.setFullName(nameField.getText());
                s.setPosition(positionField.getText());
                s.setShiftStart(Time.valueOf(startField.getText()));
                s.setShiftEnd(Time.valueOf(endField.getText()));
                s.setSalary(new BigDecimal(salaryField.getText()));

                if (staffService.addStaff(s, usernameField.getText(), new String(passwordField.getPassword()), emailField.getText())) {
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
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để sửa.");
            return;
        }

        int staffId = (int) tableModel.getValueAt(selectedRow, 0);
        // Ideally fetch fresh data, but using table data for now
        String currentName = (String) tableModel.getValueAt(selectedRow, 1);
        String currentPos = (String) tableModel.getValueAt(selectedRow, 4);
        Time currentStart = (Time) tableModel.getValueAt(selectedRow, 5);
        Time currentEnd = (Time) tableModel.getValueAt(selectedRow, 6);
        BigDecimal currentSalary = (BigDecimal) tableModel.getValueAt(selectedRow, 7);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa nhân viên", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;

        JTextField nameField = new JTextField(currentName);
        JTextField positionField = new JTextField(currentPos);
        JTextField startField = new JTextField(currentStart.toString());
        JTextField endField = new JTextField(currentEnd.toString());
        JTextField salaryField = new JTextField(currentSalary.toString());

        addFormField(formPanel, gbc, 0, "Họ tên:", nameField);
        addFormField(formPanel, gbc, 1, "Vị trí:", positionField);
        addFormField(formPanel, gbc, 2, "Ca bắt đầu:", startField);
        addFormField(formPanel, gbc, 3, "Ca kết thúc:", endField);
        addFormField(formPanel, gbc, 4, "Lương:", salaryField);

        dialog.add(formPanel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Cập nhật");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        saveButton.setBackground(new Color(0, 122, 204));
        saveButton.setForeground(Color.WHITE);
        
        saveButton.addActionListener(e -> {
            try {
                Staff s = new Staff();
                s.setStaffId(staffId);
                s.setFullName(nameField.getText());
                s.setPosition(positionField.getText());
                s.setShiftStart(Time.valueOf(startField.getText()));
                s.setShiftEnd(Time.valueOf(endField.getText()));
                s.setSalary(new BigDecimal(salaryField.getText()));

                if (staffService.updateStaff(s)) {
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

    private void deleteStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để xóa.");
            return;
        }

        int staffId = (int) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhân viên này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (staffService.deleteStaff(staffId)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại (Có thể do ràng buộc dữ liệu).");
            }
        }
    }
}
