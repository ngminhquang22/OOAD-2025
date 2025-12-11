package com.gaminglounge.gui;

import com.gaminglounge.bll.CustomerService;
import com.gaminglounge.bll.ServiceRequestService;
import com.gaminglounge.model.Customer;
import com.gaminglounge.model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.gaminglounge.model.ServiceRequest;
import java.util.List;

public class ClientDashboardWindow extends JFrame {
    private User currentUser;
    private Customer currentCustomer;
    private CustomerService customerService;
    private ServiceRequestService requestService;
    
    private JLabel timeLabel;
    private JLabel balanceLabel;
    private JTextArea chatArea;
    private JTextField chatInput;
    
    private Timer clockTimer;
    private Timer syncTimer;
    private Timer chatPollTimer;
    
    private int remainingMinutes;
    private int remainingSeconds = 0;

    public ClientDashboardWindow(User user) {
        this.currentUser = user;
        this.customerService = new CustomerService();
        this.requestService = new ServiceRequestService();
        this.currentCustomer = customerService.getCustomerByUserId(user.getUserId());
        
        if (currentCustomer == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy thông tin khách hàng!");
            dispose();
            return;
        }
        
        this.remainingMinutes = currentCustomer.getRemainingTimeMinutes();

        setTitle("Gaming Lounge Client - " + currentCustomer.getFullName());
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // 1. Header
        createHeader();

        // 2. Main Content (Timer)
        createMainContent();

        // 3. Sidebar (Services & Chat)
        createSidebar();

        // Start Timers
        startTimers();
    }

    private void createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("GAMING LOUNGE");
        title.setForeground(new Color(46, 204, 113));
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        infoPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel("Xin chào, " + currentCustomer.getFullName());
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        balanceLabel = new JLabel("Số dư: " + String.format("%,.0f", currentCustomer.getBalance()) + " VNĐ");
        balanceLabel.setForeground(new Color(241, 196, 15));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JButton logoutBtn = new JButton("Đăng xuất");
        logoutBtn.setBackground(new Color(231, 76, 60));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.addActionListener(e -> logout());

        infoPanel.add(userLabel);
        infoPanel.add(balanceLabel);
        infoPanel.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
    }

    private void createMainContent() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(44, 62, 80));
        
        timeLabel = new JLabel(formatTime(remainingMinutes, remainingSeconds));
        timeLabel.setFont(new Font("Consolas", Font.BOLD, 120));
        timeLabel.setForeground(Color.WHITE);
        
        JLabel subLabel = new JLabel("THỜI GIAN CÒN LẠI");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        subLabel.setForeground(new Color(189, 195, 199));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        panel.add(timeLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(20, 0, 0, 0);
        panel.add(subLabel, gbc);

        add(panel, BorderLayout.CENTER);
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(300, 0));
        sidebar.setBackground(new Color(236, 240, 241));
        sidebar.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Service Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        btnPanel.setOpaque(false);
        
        JButton depositBtn = new JButton("Nạp tiền");
        styleButton(depositBtn, new Color(52, 152, 219));
        depositBtn.addActionListener(e -> requestDeposit());
        
        JButton extendBtn = new JButton("Gia hạn giờ");
        styleButton(extendBtn, new Color(155, 89, 182));
        extendBtn.addActionListener(e -> requestExtend());

        btnPanel.add(depositBtn);
        btnPanel.add(extendBtn);

        // Chat Area
        JPanel chatPanel = new JPanel(new BorderLayout(0, 5));
        chatPanel.setOpaque(false);
        chatPanel.setBorder(BorderFactory.createTitledBorder("Hỗ trợ trực tuyến"));
        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        chatInput = new JTextField();
        JButton sendBtn = new JButton("Gửi");
        sendBtn.addActionListener(e -> sendChat());
        chatInput.addActionListener(e -> sendChat()); // Enter to send
        
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // Combine
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(btnPanel, BorderLayout.NORTH);
        top.add(Box.createVerticalStrut(20), BorderLayout.CENTER);

        sidebar.add(top, BorderLayout.NORTH);
        sidebar.add(chatPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.EAST);
    }

    private void startTimers() {
        // 1. Clock Timer (1 second) - Updates UI and decrements local time
        clockTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingMinutes <= 0 && remainingSeconds <= 0) {
                    timeLabel.setText("00:00:00");
                    timeLabel.setForeground(Color.RED);
                    return;
                }

                if (remainingSeconds == 0) {
                    if (remainingMinutes > 0) {
                        remainingMinutes--;
                        remainingSeconds = 59;
                    }
                } else {
                    remainingSeconds--;
                }
                
                timeLabel.setText(formatTime(remainingMinutes, remainingSeconds));
            }
        });
        clockTimer.start();

        // 2. Sync Timer (1 minute) - Updates DB
        syncTimer = new Timer(60000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (remainingMinutes >= 0) {
                    customerService.updateTime(currentCustomer.getCustomerId(), remainingMinutes);
                    // Also refresh balance in case admin updated it
                    Customer updated = customerService.getCustomerById(currentCustomer.getCustomerId());
                    if (updated != null) {
                        currentCustomer.setBalance(updated.getBalance());
                        balanceLabel.setText("Số dư: " + String.format("%,.0f", currentCustomer.getBalance()) + " VNĐ");
                    }
                }
            }
        });
        syncTimer.start();
        
        // 3. Chat Poll Timer (3 seconds)
        chatPollTimer = new Timer(3000, e -> refreshChat());
        chatPollTimer.start();
        
        // Initial load
        refreshChat();
    }

    private void refreshChat() {
        List<ServiceRequest> history = requestService.getCustomerHistory(currentCustomer.getCustomerId());
        StringBuilder sb = new StringBuilder();
        for (ServiceRequest req : history) {
            if ("Chat".equals(req.getRequestType())) {
                String sender = req.getSender();
                if (sender == null) sender = "Client";
                
                if ("Admin".equals(sender)) {
                    sb.append("ADMIN: ");
                } else {
                    sb.append("Bạn: ");
                }
                sb.append(req.getContent()).append("\n");
            }
        }
        
        if (!chatArea.getText().equals(sb.toString())) {
            chatArea.setText(sb.toString());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    private void requestDeposit() {
        String input = JOptionPane.showInputDialog(this, "Nhập số tiền muốn nạp:");
        if (input != null && !input.trim().isEmpty()) {
            requestService.sendRequest(currentCustomer.getCustomerId(), "Nạp tiền", "Yêu cầu nạp: " + input);
            appendChat("Bạn: Yêu cầu nạp " + input);
        }
    }

    private void requestExtend() {
        String input = JOptionPane.showInputDialog(this, "Nhập số giờ muốn mua thêm:");
        if (input != null && !input.trim().isEmpty()) {
            requestService.sendRequest(currentCustomer.getCustomerId(), "Mua giờ", "Yêu cầu mua thêm: " + input + " giờ");
            appendChat("Bạn: Yêu cầu mua " + input + " giờ");
        }
    }

    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            requestService.sendRequest(currentCustomer.getCustomerId(), "Chat", msg);
            // appendChat("Bạn: " + msg); // Removed local echo, rely on polling
            refreshChat(); // Immediate refresh
            chatInput.setText("");
        }
    }

    private void appendChat(String msg) {
        chatArea.append(msg + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private String formatTime(int minutes, int seconds) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format("%02d:%02d:%02d", h, m, seconds);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(0, 40));
    }

    private void logout() {
        clockTimer.stop();
        syncTimer.stop();
        if (chatPollTimer != null) chatPollTimer.stop();
        // Sync one last time
        customerService.updateTime(currentCustomer.getCustomerId(), remainingMinutes);
        
        dispose();
        new LoginWindow().setVisible(true);
    }
}
