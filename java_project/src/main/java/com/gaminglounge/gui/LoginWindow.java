package com.gaminglounge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.gaminglounge.bll.AuthService;
import com.gaminglounge.model.User;

public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private AuthService authService;

    public LoginWindow() {
        authService = new AuthService();
        setTitle("Gaming Lounge Login");
        setSize(400, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main Panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(new EmptyBorder(40, 40, 40, 40));
        
        // Logo/Title
        JLabel titleLabel = new JLabel("GAMING LOUNGE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(0, 200, 255)); // Cyan accent
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, titleLabel.getPreferredSize().height));
        
        JLabel subtitleLabel = new JLabel("Hệ thống quản lý");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, subtitleLabel.getPreferredSize().height));

        // Form Fields
        JLabel userLabel = new JLabel("Tên đăng nhập");
        userLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        usernameField = new JTextField();
        usernameField.putClientProperty("JTextField.placeholderText", "Nhập tên đăng nhập");
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        usernameField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel passLabel = new JLabel("Mật khẩu");
        passLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        passwordField = new JPasswordField();
        passwordField.putClientProperty("JTextField.placeholderText", "Nhập mật khẩu");
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        passwordField.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Login Button
        JButton loginButton = new JButton("ĐĂNG NHẬP QUẢN TRỊ");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setBackground(new Color(0, 120, 215));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
        loginButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> login());
        
        // Client Login Link
        JButton clientLoginBtn = new JButton("Khách hàng đăng nhập tại đây");
        clientLoginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        clientLoginBtn.setForeground(new Color(0, 120, 215));
        clientLoginBtn.setContentAreaFilled(false);
        clientLoginBtn.setBorderPainted(false);
        clientLoginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        clientLoginBtn.addActionListener(e -> {
            new ClientLoginWindow().setVisible(true);
            this.dispose();
        });
        
        // Add components with spacing
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(titleLabel);
        mainPanel.add(subtitleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        
        mainPanel.add(userLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(usernameField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        mainPanel.add(passLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        mainPanel.add(passwordField);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));
        
        mainPanel.add(loginButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(clientLoginBtn);
        mainPanel.add(Box.createVerticalGlue());
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel, BorderLayout.CENTER);
        
        // Allow Enter key to login
        getRootPane().setDefaultButton(loginButton);
    }

    private void login() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try {
            User user = authService.login(username, password);
            
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Sai tên đăng nhập hoặc mật khẩu!", "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 1. Check if Member (Client) -> DENY ACCESS
            if ("Member".equalsIgnoreCase(user.getRoleName()) || user.getRoleId() == 3) {
                JOptionPane.showMessageDialog(this, 
                    "Tài khoản Khách hàng vui lòng sử dụng trang đăng nhập dành riêng cho khách hàng.", 
                    "Truy cập bị từ chối", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Check if Staff (Role ID 2)
            if (user.getRoleId() == 2) {
                StaffTimekeepingDialog dialog = new StaffTimekeepingDialog(this, user);
                dialog.setVisible(true);
                
                // Only proceed if user didn't just close the window (confirmed means they clicked a button)
                if (dialog.isConfirmed()) {
                    new DashboardWindow(user).setVisible(true);
                    this.dispose();
                }
            } else {
                new DashboardWindow(user).setVisible(true);
                this.dispose();
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
        }
    }
}
