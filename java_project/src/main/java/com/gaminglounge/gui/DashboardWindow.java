package com.gaminglounge.gui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import com.gaminglounge.model.User;

public class DashboardWindow extends JFrame {

    public DashboardWindow(User user) {
        setTitle("Quản lý Gaming Lounge");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Check for Member role (RoleID 3 or RoleName "Hội viên")
        // Using RoleID is safer against encoding issues
        if (user.getRoleId() == 3 || "Hội viên".equalsIgnoreCase(user.getRoleName())) {
            add(new ClientDashboardPanel(user), BorderLayout.CENTER);
        } else {
            add(new AdminDashboardPanel(user), BorderLayout.CENTER);
        }
    }
}
