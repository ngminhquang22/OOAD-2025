package com.gaminglounge.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.gaminglounge.bll.ComputerService;
import com.gaminglounge.model.Computer;
import com.gaminglounge.model.User;

public class AdminDashboardPanel extends JPanel {
    private User currentUser;
    private ComputerService computerService;
    
    // Layout Components
    private JPanel sidebarPanel;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private Map<String, JButton> navButtons;
    private String currentCard = "OVERVIEW";

    // Computer Panel Components
    private JPanel computersPanel;

    public AdminDashboardPanel(User user) {
        this.currentUser = user;
        System.out.println("Logged in as: " + user.getUsername() + ", RoleID: " + user.getRoleId() + ", RoleName: " + user.getRoleName());
        this.computerService = new ComputerService();
        this.navButtons = new HashMap<>();

        setLayout(new BorderLayout());
        
        // 1. Header (Top Bar)
        createHeader();

        // 2. Sidebar (Left Navigation)
        createSidebar();

        // 3. Content Area (Center)
        createContentArea();

        // Add components
        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
        
        // Show default
        // Check for Admin role by RoleID (1 = Admin)
        boolean isAdmin = (currentUser.getRoleId() == 1);
        if (!isAdmin) {
            showCard("COMPUTERS");
        } else {
            showCard("OVERVIEW");
        }
    }

    private void createHeader() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(30, 30, 30));
        headerPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        headerPanel.setPreferredSize(new Dimension(0, 70));
        
        JLabel titleLabel = new JLabel("GAMING LOUNGE MANAGER");
        titleLabel.setForeground(new Color(0, 122, 204));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        userPanel.setOpaque(false);
        
        // Show Username AND Role for clarity
        JLabel userLabel = new JLabel(currentUser.getUsername() + " (" + currentUser.getRoleName() + ")");
        userLabel.setForeground(Color.LIGHT_GRAY);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userPanel.add(userLabel);
        
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

    private void createSidebar() {
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setBackground(new Color(40, 40, 40));
        sidebarPanel.setPreferredSize(new Dimension(220, 0));
        sidebarPanel.setBorder(new EmptyBorder(20, 10, 20, 10));

        // DEBUG: Print role info
        System.out.println("DEBUG: User RoleID=" + currentUser.getRoleId() + ", RoleName=" + currentUser.getRoleName());

        // STRICT CHECK: Only RoleID 1 is Admin
        boolean isAdmin = (currentUser.getRoleId() == 1);

        if (isAdmin) {
            addNavButton("Tổng quan", "OVERVIEW");
        }
        
        addNavButton("Máy tính", "COMPUTERS");
        
        // Only add these if isAdmin is TRUE
        if (isAdmin) {
            addNavButton("Nhân viên", "STAFF");
            addNavButton("Khách hàng", "CUSTOMERS");
            addNavButton("Tài khoản", "USERS");
        }
        
        addNavButton("Giao dịch", "TRANSACTIONS");
        addNavButton("Kho hàng", "INVENTORY");
        addNavButton("Báo cáo", "REPORTS");
    }

    private void addNavButton(String text, String cardName) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        btn.setForeground(new Color(200, 200, 200));
        btn.setBackground(new Color(40, 40, 40));
        btn.setBorder(new EmptyBorder(12, 20, 12, 20));
        btn.setFocusPainted(false);
        btn.setMaximumSize(new Dimension(200, 50));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        
        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                if (!currentCard.equals(cardName)) {
                    btn.setBackground(new Color(60, 60, 60));
                }
            }
            public void mouseExited(MouseEvent e) {
                if (!currentCard.equals(cardName)) {
                    btn.setBackground(new Color(40, 40, 40));
                }
            }
        });

        btn.addActionListener(e -> showCard(cardName));
        
        sidebarPanel.add(btn);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 5))); // Spacing
        navButtons.put(cardName, btn);
    }

    private void createContentArea() {
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        boolean isAdmin = (currentUser.getRoleId() == 1);

        // 1. Overview Panel
        if (isAdmin) {
            contentPanel.add(createOverviewPanel(), "OVERVIEW");
        }

        // 2. Computers Panel
        contentPanel.add(createComputersPanel(), "COMPUTERS");

        // 3. Staff Panel
        if (isAdmin) {
            contentPanel.add(new StaffManagementPanel(), "STAFF");
        }

        // 4. Customers Panel
        if (isAdmin) {
            contentPanel.add(new CustomerManagementPanel(), "CUSTOMERS");
        }

        // 5. Users Panel
        if (isAdmin) {
            contentPanel.add(new UserManagementPanel(), "USERS");
        }

        // 6. Transactions Panel
        contentPanel.add(new TransactionManagementPanel(), "TRANSACTIONS");

        // 7. Inventory
        contentPanel.add(new InventoryManagementPanel(currentUser), "INVENTORY");
        contentPanel.add(createPlaceholderPanel("Báo cáo Thống kê"), "REPORTS");
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel title = new JLabel("Tổng quan hệ thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setPreferredSize(new Dimension(0, 150));
        
        statsPanel.add(createStatCard("Máy đang dùng", "12/50", new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Doanh thu hôm nay", "5.2M", new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Khách hàng mới", "+8", new Color(155, 89, 182)));
        statsPanel.add(createStatCard("Cảnh báo", "2 Máy lỗi", new Color(231, 76, 60)));

        panel.add(statsPanel, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(new Color(255, 255, 255, 200));
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        
        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        
        return card;
    }

    private JPanel createComputersPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JPanel topBar = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Danh sách máy trạm");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        JButton refreshBtn = new JButton("Làm mới");
        refreshBtn.addActionListener(e -> refreshComputers());
        
        topBar.add(title, BorderLayout.WEST);
        topBar.add(refreshBtn, BorderLayout.EAST);
        topBar.setBorder(new EmptyBorder(0, 0, 15, 0));
        panel.add(topBar, BorderLayout.NORTH);

        computersPanel = new JPanel(new GridLayout(0, 5, 15, 15));
        JScrollPane scroll = new JScrollPane(computersPanel);
        scroll.setBorder(null);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createPlaceholderPanel(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        JLabel label = new JLabel(title + " - Đang phát triển");
        label.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        label.setForeground(Color.GRAY);
        panel.add(label);
        return panel;
    }

    private void showCard(String cardName) {
        // Update active button style
        if (navButtons.containsKey(currentCard)) {
            JButton oldBtn = navButtons.get(currentCard);
            oldBtn.setBackground(new Color(40, 40, 40));
            oldBtn.setForeground(new Color(200, 200, 200));
            oldBtn.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        }
        
        currentCard = cardName;
        
        if (navButtons.containsKey(currentCard)) {
            JButton newBtn = navButtons.get(currentCard);
            newBtn.setBackground(new Color(0, 122, 204));
            newBtn.setForeground(Color.WHITE);
            newBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        }

        cardLayout.show(contentPanel, cardName);
        
        if ("COMPUTERS".equals(cardName)) {
            refreshComputers();
        }
    }

    private void refreshComputers() {
        if (computersPanel == null) return;
        
        computersPanel.removeAll();
        List<Computer> computers = computerService.getAllComputers();

        for (Computer pc : computers) {
            JPanel pcCard = new JPanel(new BorderLayout());
            pcCard.setBackground(new Color(60, 60, 60));
            pcCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(80, 80, 80), 1),
                new EmptyBorder(10, 10, 10, 10)
            ));
            pcCard.setPreferredSize(new Dimension(140, 120));
            
            // Status Color
            Color statusColor;
            if ("Trống".equals(pc.getStatus())) {
                statusColor = new Color(46, 204, 113);
            } else if ("Đang sử dụng".equals(pc.getStatus())) {
                statusColor = new Color(231, 76, 60);
            } else {
                statusColor = new Color(241, 196, 15);
            }
            
            JLabel iconLabel = new JLabel("🖥️");
            iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
            iconLabel.setForeground(statusColor);
            
            JLabel nameLabel = new JLabel(pc.getComputerName());
            nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JLabel statusLabel = new JLabel(pc.getStatus());
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statusLabel.setForeground(statusColor);
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            
            JPanel infoPanel = new JPanel(new GridLayout(2, 1));
            infoPanel.setOpaque(false);
            infoPanel.add(nameLabel);
            infoPanel.add(statusLabel);
            
            pcCard.add(iconLabel, BorderLayout.CENTER);
            pcCard.add(infoPanel, BorderLayout.SOUTH);

            // Context Menu
            JPopupMenu contextMenu = new JPopupMenu();
            JMenuItem toggleItem = new JMenuItem("Chuyển đổi trạng thái");
            toggleItem.addActionListener(e -> togglePC(pc));
            contextMenu.add(toggleItem);
            
            pcCard.setComponentPopupMenu(contextMenu);
            
            pcCard.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent evt) {
                    if (evt.getClickCount() == 2) togglePC(pc);
                }
                public void mouseEntered(MouseEvent e) {
                    pcCard.setBackground(new Color(70, 70, 70));
                }
                public void mouseExited(MouseEvent e) {
                    pcCard.setBackground(new Color(60, 60, 60));
                }
            });

            computersPanel.add(pcCard);
        }

        computersPanel.revalidate();
        computersPanel.repaint();
    }

    private void togglePC(Computer pc) {
        computerService.toggleComputerStatus(pc.getComputerId(), pc.getStatus());
        refreshComputers();
    }

    private void logout() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) {
            win.dispose();
            new LoginWindow().setVisible(true);
        }
    }
}
