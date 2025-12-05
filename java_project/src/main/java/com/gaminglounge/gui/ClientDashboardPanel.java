package com.gaminglounge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.math.BigDecimal;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gaminglounge.bll.CustomerService;
import com.gaminglounge.model.Customer;
import com.gaminglounge.model.User;

public class ClientDashboardPanel extends JPanel {
    private User user;
    private Customer customer;
    private CustomerService customerService;
    
    private JLabel balanceLabel;
    private JLabel timeLabel;
    private javax.swing.Timer timer;
    private int remainingSeconds;

    public ClientDashboardPanel(User user) {
        this.user = user;
        this.customerService = new CustomerService();
        this.customer = customerService.getCustomerByUserId(user.getUserId());

        setLayout(new BorderLayout());
        
        // 1. Header
        createHeader();

        // 2. Main Content (Cards)
        createMainContent();

        // 3. Chat Panel (Bottom)
        createChatPanel();

        startTimer();
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 30, 30));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerPanel.setPreferredSize(new Dimension(0, 70));

        JLabel titleLabel = new JLabel("GAMING LOUNGE CLIENT");
        titleLabel.setForeground(new Color(0, 122, 204));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);

        JLabel welcomeLabel = new JLabel("Xin chào, " + (customer != null ? customer.getFullName() : user.getUsername()));
        welcomeLabel.setForeground(Color.LIGHT_GRAY);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userPanel.add(welcomeLabel);

        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setBackground(new Color(231, 76, 60));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.addActionListener(e -> logout());
        userPanel.add(logoutButton);

        headerPanel.add(userPanel, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);
    }

    private void createMainContent() {
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(10, 10, 10, 10);

        // 1. Machine Info Card
        gbc.gridx = 0;
        gbc.gridy = 0;
        JPanel machineCard = createStatCard("Thông tin máy", new Color(52, 152, 219));
        
        JLabel machineLabel = new JLabel("PC-05"); // Placeholder
        machineLabel.setForeground(Color.WHITE);
        machineLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        machineLabel.setHorizontalAlignment(SwingConstants.CENTER);
        machineCard.add(machineLabel, BorderLayout.CENTER);
        
        JLabel statusLabel = new JLabel("Đang hoạt động");
        statusLabel.setForeground(Color.LIGHT_GRAY);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        machineCard.add(statusLabel, BorderLayout.SOUTH);
        
        contentPanel.add(machineCard, gbc);

        // 2. Time Card
        gbc.gridx = 1;
        JPanel timeCard = createStatCard("Thời gian còn lại", new Color(46, 204, 113));
        
        int minutes = (customer != null ? customer.getRemainingTimeMinutes() : 0);
        remainingSeconds = minutes * 60;
        
        timeLabel = new JLabel(formatTime(remainingSeconds));
        timeLabel.setForeground(Color.WHITE);
        timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeCard.add(timeLabel, BorderLayout.CENTER);
        
        contentPanel.add(timeCard, gbc);

        // 3. Balance Card
        gbc.gridx = 2;
        JPanel balanceCard = createStatCard("Số dư tài khoản", new Color(241, 196, 15));
        balanceLabel = new JLabel(formatCurrency(customer != null ? customer.getBalance() : BigDecimal.ZERO));
        balanceLabel.setForeground(Color.WHITE);
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        balanceCard.add(balanceLabel, BorderLayout.CENTER);
        
        JButton topUpButton = new JButton("Nạp tiền ngay");
        topUpButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        topUpButton.setBackground(new Color(0, 122, 204));
        topUpButton.setForeground(Color.WHITE);
        topUpButton.setFocusPainted(false);
        topUpButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        topUpButton.addActionListener(e -> showTopUpDialog());
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(topUpButton);
        balanceCard.add(btnPanel, BorderLayout.SOUTH);
        
        contentPanel.add(balanceCard, gbc);

        add(contentPanel, BorderLayout.CENTER);
    }

    private void createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout());
        chatPanel.setBorder(new EmptyBorder(10, 20, 20, 20));
        chatPanel.setPreferredSize(new Dimension(0, 220));

        JLabel chatTitle = new JLabel("Hỗ trợ trực tuyến");
        chatTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        chatTitle.setForeground(new Color(200, 200, 200));
        chatTitle.setBorder(new EmptyBorder(0, 0, 10, 0));
        chatPanel.add(chatTitle, BorderLayout.NORTH);

        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatArea.setBackground(new Color(40, 40, 40));
        chatArea.setForeground(Color.LIGHT_GRAY);
        chatArea.setText("System: Chào mừng bạn đến với Gaming Lounge!\nSystem: Nếu cần hỗ trợ, vui lòng nhắn tin tại đây.");
        
        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
        chatPanel.add(scroll, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        JTextField chatInput = new JTextField();
        chatInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chatInput.setBackground(new Color(50, 50, 50));
        chatInput.setForeground(Color.WHITE);
        chatInput.setCaretColor(Color.WHITE);
        
        JButton sendButton = new JButton("Gửi tin nhắn");
        sendButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        sendButton.setBackground(new Color(0, 122, 204));
        sendButton.setForeground(Color.WHITE);
        sendButton.setFocusPainted(false);
        
        // Simple echo for demo
        sendButton.addActionListener(e -> {
            String text = chatInput.getText().trim();
            if (!text.isEmpty()) {
                chatArea.append("\nBạn: " + text);
                chatInput.setText("");
                // Auto-scroll
                chatArea.setCaretPosition(chatArea.getDocument().getLength());
            }
        });
        
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        add(chatPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(45, 45, 48));
        // Rounded border effect via FlatLaf global settings, plus padding
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(accentColor);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        card.add(titleLabel, BorderLayout.NORTH);
        
        return card;
    }

    private void startTimer() {
        if (customer == null) return;
        
        timer = new javax.swing.Timer(1000, e -> {
            if (remainingSeconds > 0) {
                remainingSeconds--;
                timeLabel.setText(formatTime(remainingSeconds));
                
                // Sync with DB every minute (when seconds hit 00)
                if (remainingSeconds % 60 == 0) {
                    customerService.updateTime(customer.getCustomerId(), remainingSeconds / 60);
                }
            } else {
                timer.stop();
                timeLabel.setText("Hết giờ!");
                JOptionPane.showMessageDialog(this, "Hết giờ chơi! Vui lòng nạp thêm giờ.");
                logout();
            }
        });
        timer.start();
    }

    private String formatTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    private void showTopUpDialog() {
        String input = JOptionPane.showInputDialog(this, "Nhập số tiền cần nạp (8000đ = 1 giờ):");
        if (input != null && !input.isEmpty()) {
            try {
                double amount = Double.parseDouble(input);
                if (amount > 0) {
                    int minutesToAdd = (int) ((amount / 8000) * 60);
                    
                    int confirm = JOptionPane.showConfirmDialog(this, 
                        String.format("Bạn nạp %,.0f VNĐ.\nQuy đổi: %d phút.\nĐồng ý?", amount, minutesToAdd),
                        "Xác nhận nạp tiền", JOptionPane.YES_NO_OPTION);
                        
                    if (confirm == JOptionPane.YES_OPTION) {
                        // Update Time and Record Transaction
                        // Using staffId = 0 for Self-Service/System
                        if (customerService.processTimeTopUp(customer.getCustomerId(), customer.getRemainingTimeMinutes(), minutesToAdd, BigDecimal.valueOf(amount), 0)) {
                            int newTimeMinutes = customer.getRemainingTimeMinutes() + minutesToAdd;
                            customer.setRemainingTimeMinutes(newTimeMinutes);
                            remainingSeconds += minutesToAdd * 60;
                            timeLabel.setText(formatTime(remainingSeconds));
                            JOptionPane.showMessageDialog(this, "Nạp tiền thành công! (Đã cộng giờ)");
                        } else {
                            JOptionPane.showMessageDialog(this, "Nạp tiền thất bại.");
                        }
                    }
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ.");
            }
        }
    }

    private void logout() {
        if (timer != null && timer.isRunning()) {
            timer.stop();
        }
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) {
            win.dispose();
            new LoginWindow().setVisible(true);
        }
    }
}
