package com.gaminglounge.gui;

import com.gaminglounge.bll.TimekeepingService;
import com.gaminglounge.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StaffTimekeepingDialog extends JDialog {
    private User currentUser;
    private TimekeepingService timekeepingService;
    private JLabel timeLabel;
    private boolean confirmed = false;

    public StaffTimekeepingDialog(JFrame parent, User user) {
        super(parent, "Chấm công nhân viên", true);
        this.currentUser = user;
        this.timekeepingService = new TimekeepingService();

        setSize(400, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(0, 120, 215));
        headerPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JLabel titleLabel = new JLabel("XÁC NHẬN CHẤM CÔNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel);
        add(headerPanel, BorderLayout.NORTH);

        // Content
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        contentPanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Xin chào, " + user.getUsername());
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        timeLabel = new JLabel();
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        timeLabel.setForeground(new Color(50, 50, 50));
        timeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        startClock();

        JLabel statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        boolean isCheckedIn = timekeepingService.isCheckedIn(user.getUserId());
        if (isCheckedIn) {
            statusLabel.setText("Trạng thái: Đang trong ca làm việc");
            statusLabel.setForeground(new Color(46, 204, 113));
        } else {
            statusLabel.setText("Trạng thái: Chưa vào ca");
            statusLabel.setForeground(new Color(231, 76, 60));
        }

        contentPanel.add(welcomeLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(timeLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(statusLabel);
        
        add(contentPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        
        JButton actionButton = new JButton(isCheckedIn ? "Kết thúc ca (Check-out)" : "Bắt đầu ca (Check-in)");
        actionButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        actionButton.setBackground(isCheckedIn ? new Color(231, 76, 60) : new Color(46, 204, 113));
        actionButton.setForeground(Color.WHITE);
        actionButton.setFocusPainted(false);
        actionButton.setPreferredSize(new Dimension(200, 40));
        
        actionButton.addActionListener(e -> {
            boolean success;
            if (isCheckedIn) {
                success = timekeepingService.checkOut(user.getUserId());
                if (success) JOptionPane.showMessageDialog(this, "Đã kết thúc ca làm việc!");
            } else {
                success = timekeepingService.checkIn(user.getUserId());
                if (success) JOptionPane.showMessageDialog(this, "Đã bắt đầu ca làm việc!");
            }
            
            if (success) {
                confirmed = true;
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra, vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton skipButton = new JButton("Vào trang chủ");
        skipButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        skipButton.setBackground(Color.LIGHT_GRAY);
        skipButton.setFocusPainted(false);
        skipButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        buttonPanel.add(actionButton);
        buttonPanel.add(skipButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void startClock() {
        Timer timer = new Timer(1000, e -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            timeLabel.setText(sdf.format(new Date()));
        });
        timer.start();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
