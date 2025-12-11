package com.gaminglounge.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
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
import javax.swing.table.DefaultTableModel;
import javax.swing.JTable;

import com.gaminglounge.bll.ComputerService;
import com.gaminglounge.bll.StaffService;
import com.gaminglounge.bll.StatisticsService;
import com.gaminglounge.bll.WorkScheduleService;
import com.gaminglounge.model.Computer;
import com.gaminglounge.model.Staff;
import com.gaminglounge.model.User;
import com.gaminglounge.model.WorkSchedule;

import java.util.Calendar;
import java.util.Date;
import java.time.LocalDate;
import java.text.SimpleDateFormat;

public class AdminDashboardPanel extends JPanel {
    private User currentUser;
    private ComputerService computerService;
    private WorkScheduleService workScheduleService;
    private StatisticsService statisticsService;
    private StaffService staffService;
    
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
        this.workScheduleService = new WorkScheduleService();
        this.statisticsService = new StatisticsService();
        this.staffService = new StaffService();
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
        } else {
            addNavButton("Lịch làm việc", "MY_SCHEDULE");
        }
        
        addNavButton("Máy tính", "COMPUTERS");
        
        // Only add these if isAdmin is TRUE
        if (isAdmin) {
            addNavButton("Nhân viên", "STAFF");
            addNavButton("Tài khoản", "USERS");
        }
        
        // Available for both Admin and Staff
        addNavButton("Khách hàng", "CUSTOMERS");
        
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
        } else {
            contentPanel.add(createMySchedulePanel(), "MY_SCHEDULE");
        }

        // 2. Computers Panel
        contentPanel.add(createComputersPanel(), "COMPUTERS");

        // 3. Staff Panel
        if (isAdmin) {
            contentPanel.add(new StaffManagementPanel(), "STAFF");
        }

        // 4. Customers Panel
        contentPanel.add(new CustomerManagementPanel(currentUser), "CUSTOMERS");

        // 5. Users Panel
        if (isAdmin) {
            contentPanel.add(new UserManagementPanel(), "USERS");
        }

        // 6. Transactions Panel
        contentPanel.add(new TransactionManagementPanel(), "TRANSACTIONS");

        // 8. Inventory
        contentPanel.add(new InventoryManagementPanel(currentUser), "INVENTORY");
        contentPanel.add(new StatisticsPanel(), "REPORTS");
    }

    private JPanel createOverviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        JLabel title = new JLabel("Tổng quan hệ thống");
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(title, BorderLayout.NORTH);

        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        statsPanel.setPreferredSize(new Dimension(0, 150));
        
        // Fetch Real Data
        LocalDate today = LocalDate.now();
        int activeComputers = statisticsService.getComputerCountByStatus("Occupied");
        int totalComputers = statisticsService.getTotalComputers();
        
        double depositRev = statisticsService.getTotalDepositRevenue(today, today);
        double timeRev = statisticsService.getTotalTimeSalesRevenue(today, today);
        double totalRev = depositRev + timeRev;
        
        int newCustomers = statisticsService.getNewCustomersCount(today);
        int warnings = statisticsService.getWarningComputersCount();
        
        statsPanel.add(createStatCard("Máy đang dùng", activeComputers + "/" + totalComputers, new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Doanh thu hôm nay", String.format("%,.0f", totalRev), new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Khách hàng mới", "+" + newCustomers, new Color(155, 89, 182)));
        statsPanel.add(createStatCard("Cảnh báo", warnings + " Máy lỗi", new Color(231, 76, 60)));

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
        
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        if (currentUser.getRoleId() == 1) { // Admin
            JButton addBtn = new JButton("Thêm máy");
            addBtn.setBackground(new Color(46, 204, 113));
            addBtn.setForeground(Color.WHITE);
            addBtn.setFocusPainted(false);
            addBtn.addActionListener(e -> showComputerDialog(null));
            actionPanel.add(addBtn);
        }

        JButton refreshBtn = new JButton("Làm mới");
        refreshBtn.addActionListener(e -> refreshComputers());
        actionPanel.add(refreshBtn);
        
        topBar.add(title, BorderLayout.WEST);
        topBar.add(actionPanel, BorderLayout.EAST);
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

    private JPanel createMySchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.WHITE);
        header.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel title = new JLabel("Lịch làm việc của tôi");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        header.add(title, BorderLayout.WEST);
        
        // Week Navigation
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        navPanel.setOpaque(false);
        
        JButton prevBtn = new JButton("< Tuần trước");
        JLabel weekLabel = new JLabel();
        weekLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        weekLabel.setBorder(new EmptyBorder(0, 15, 0, 15));
        JButton nextBtn = new JButton("Tuần sau >");
        
        navPanel.add(prevBtn);
        navPanel.add(weekLabel);
        navPanel.add(nextBtn);
        header.add(navPanel, BorderLayout.EAST);
        
        panel.add(header, BorderLayout.NORTH);
        
        // Timetable
        String[] columnNames = {"Ca / Thứ", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 3) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        JTable table = new JTable(model);
        table.setRowHeight(120);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(230, 240, 255));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setGridColor(new Color(200, 200, 200));
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        
        // Use JTextArea renderer for multiline
        table.setDefaultRenderer(Object.class, new javax.swing.table.TableCellRenderer() {
            javax.swing.JTextArea textArea = new javax.swing.JTextArea();
            {
                textArea.setLineWrap(true);
                textArea.setWrapStyleWord(true);
                textArea.setOpaque(true);
                textArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            }
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                if (isSelected) {
                    textArea.setForeground(t.getSelectionForeground());
                    textArea.setBackground(t.getSelectionBackground());
                } else {
                    textArea.setForeground(Color.BLACK);
                    textArea.setBackground(Color.WHITE);
                    if (column == 0) {
                        textArea.setBackground(new Color(245, 245, 245));
                        textArea.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    } else {
                        textArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                        // Highlight if shift exists
                        if (value != null && !value.toString().isEmpty()) {
                            textArea.setBackground(new Color(220, 255, 220)); // Light Green for assigned shifts
                        }
                    }
                }
                textArea.setText((value == null) ? "" : value.toString());
                return textArea;
            }
        });
        
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        
        // Logic
        Calendar currentWeekStart = Calendar.getInstance();
        currentWeekStart.setFirstDayOfWeek(Calendar.MONDAY);
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        
        Runnable loadSchedule = () -> {
            // Update Label
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            Calendar endOfWeek = (Calendar) currentWeekStart.clone();
            endOfWeek.add(Calendar.DAY_OF_YEAR, 6);
            weekLabel.setText("Tuần: " + sdf.format(currentWeekStart.getTime()) + " - " + sdf.format(endOfWeek.getTime()));
            
            // Update Headers
            Calendar cal = (Calendar) currentWeekStart.clone();
            String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
            SimpleDateFormat daySdf = new SimpleDateFormat("dd/MM");
            
            table.getColumnModel().getColumn(0).setHeaderValue("Ca / Thứ");
            for (int i = 0; i < 7; i++) {
                table.getColumnModel().getColumn(i + 1).setHeaderValue(days[i] + " (" + daySdf.format(cal.getTime()) + ")");
                cal.add(Calendar.DAY_OF_YEAR, 1);
            }
            table.getTableHeader().repaint();
            
            // Clear
            for (int r = 0; r < 3; r++) {
                for (int c = 1; c <= 7; c++) {
                    model.setValueAt("", r, c);
                }
            }
            model.setValueAt("Ca Sáng\n(07:00 - 15:00)", 0, 0);
            model.setValueAt("Ca Chiều\n(15:00 - 23:00)", 1, 0);
            model.setValueAt("Ca Đêm\n(23:00 - 07:00)", 2, 0);
            
            // Fetch Data
            java.sql.Date startDate = new java.sql.Date(currentWeekStart.getTimeInMillis());
            java.sql.Date endDate = new java.sql.Date(endOfWeek.getTimeInMillis());
            List<WorkSchedule> schedules = workScheduleService.getSchedulesByDateRange(startDate, endDate);
            
            // Get current staff ID
            Staff currentStaff = staffService.getStaffByUserId(currentUser.getUserId());
            int currentStaffId = (currentStaff != null) ? currentStaff.getStaffId() : -1;

            for (WorkSchedule ws : schedules) {
                // Filter for current user
                if (ws.getStaffId() != currentStaffId) continue;
                
                Calendar wsCal = Calendar.getInstance();
                wsCal.setTime(ws.getWorkDate());
                long diff = ws.getWorkDate().getTime() - startDate.getTime();
                int dayIndex = (int) (diff / (1000 * 60 * 60 * 24)) + 1;
                
                if (dayIndex < 1 || dayIndex > 7) continue;
                
                int rowIndex = -1;
                int startHour = Integer.parseInt(ws.getShiftStart().toString().split(":")[0]);
                if (startHour >= 6 && startHour < 14) rowIndex = 0;
                else if (startHour >= 14 && startHour < 22) rowIndex = 1;
                else rowIndex = 2;
                
                if (rowIndex != -1) {
                    String note = (ws.getNote() != null) ? ws.getNote() : "";
                    model.setValueAt("Đã phân công\n" + note, rowIndex, dayIndex);
                }
            }
        };
        
        prevBtn.addActionListener(e -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            loadSchedule.run();
        });
        
        nextBtn.addActionListener(e -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            loadSchedule.run();
        });
        
        loadSchedule.run();
        
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
            
            JMenuItem openItem = new JMenuItem("Mở máy");
            openItem.addActionListener(e -> updatePCStatus(pc, "Đang sử dụng"));
            
            JMenuItem lockItem = new JMenuItem("Khóa máy");
            lockItem.addActionListener(e -> updatePCStatus(pc, "Trống"));
            
            JMenuItem brokenItem = new JMenuItem("Báo hỏng");
            brokenItem.addActionListener(e -> updatePCStatus(pc, "Hỏng"));
            
            JMenuItem maintainItem = new JMenuItem("Bảo trì");
            maintainItem.addActionListener(e -> updatePCStatus(pc, "Bảo trì"));

            contextMenu.add(openItem);
            contextMenu.add(lockItem);
            contextMenu.addSeparator();
            contextMenu.add(brokenItem);
            contextMenu.add(maintainItem);
            
            if (currentUser.getRoleId() == 1) { // Admin
                contextMenu.addSeparator();
                JMenuItem editItem = new JMenuItem("Sửa thông tin");
                editItem.addActionListener(e -> showComputerDialog(pc));
                contextMenu.add(editItem);
                
                JMenuItem deleteItem = new JMenuItem("Xóa máy");
                deleteItem.addActionListener(e -> deleteComputer(pc));
                contextMenu.add(deleteItem);
            }
            
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

    private void updatePCStatus(Computer pc, String status) {
        // a3. Permission check for "Hỏng" (Broken) - Only Admin (RoleID 1) can mark as broken
        if ("Hỏng".equals(status) && currentUser.getRoleId() != 1) {
             javax.swing.JOptionPane.showMessageDialog(this, "Bạn không có quyền thực hiện thao tác này.");
             return;
        }

        try {
            boolean success = computerService.setComputerStatus(pc.getComputerId(), status);
            if (!success) {
                // a1. Device not found
                javax.swing.JOptionPane.showMessageDialog(this, "Thiết bị không tồn tại hoặc chưa được đăng ký.");
            }
            refreshComputers();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
            // a2. Connection error
            javax.swing.JOptionPane.showMessageDialog(this, "Mất kết nối với máy, không thể thực hiện thao tác.");
        } catch (Exception e) {
            // a4. General failure
             javax.swing.JOptionPane.showMessageDialog(this, "Thao tác không thành công do lỗi mạng hoặc phần mềm quản lý.");
        }
    }

    private void togglePC(Computer pc) {
        try {
            String newStatus = computerService.toggleComputerStatus(pc.getComputerId(), pc.getStatus());
            if (newStatus == null) {
                 // a1. Device not found
                 javax.swing.JOptionPane.showMessageDialog(this, "Thiết bị không tồn tại hoặc chưa được đăng ký.");
            }
            refreshComputers();
        } catch (java.sql.SQLException e) {
             // a2. Connection error
             javax.swing.JOptionPane.showMessageDialog(this, "Mất kết nối với máy, không thể thực hiện thao tác.");
        } catch (Exception e) {
             // a4. General failure
             javax.swing.JOptionPane.showMessageDialog(this, "Thao tác không thành công do lỗi mạng hoặc phần mềm quản lý.");
        }
    }

    private void deleteComputer(Computer pc) {
        int confirm = javax.swing.JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa máy " + pc.getComputerName() + "?", 
            "Xác nhận xóa", javax.swing.JOptionPane.YES_NO_OPTION);
            
        if (confirm == javax.swing.JOptionPane.YES_OPTION) {
            if (computerService.deleteComputer(pc.getComputerId())) {
                refreshComputers();
            } else {
                javax.swing.JOptionPane.showMessageDialog(this, "Không thể xóa máy này (có thể đang có phiên hoạt động).");
            }
        }
    }

    private void showComputerDialog(Computer pc) {
        javax.swing.JDialog dialog = new javax.swing.JDialog((java.awt.Frame) SwingUtilities.getWindowAncestor(this), 
            pc == null ? "Thêm máy mới" : "Sửa thông tin máy", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(350, 200);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new java.awt.Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        javax.swing.JTextField nameField = new javax.swing.JTextField(20);
        javax.swing.JTextField locationField = new javax.swing.JTextField(20);
        
        if (pc != null) {
            nameField.setText(pc.getComputerName());
            locationField.setText(pc.getLocation());
        }
        
        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Tên máy:"), gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Vị trí:"), gbc);
        gbc.gridx = 1; dialog.add(locationField, gbc);
        
        JButton saveBtn = new JButton("Lưu");
        saveBtn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String loc = locationField.getText().trim();
            
            if (name.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(dialog, "Tên máy không được để trống.");
                return;
            }
            
            Computer c = (pc == null) ? new Computer() : pc;
            c.setComputerName(name);
            c.setLocation(loc);
            
            boolean success;
            if (pc == null) {
                c.setStatus("Trống");
                success = computerService.addComputer(c);
            } else {
                success = computerService.updateComputer(c);
            }
            
            if (success) {
                dialog.dispose();
                refreshComputers();
            } else {
                javax.swing.JOptionPane.showMessageDialog(dialog, "Lưu thất bại.");
            }
        });
        
        gbc.gridx = 1; gbc.gridy = 2; dialog.add(saveBtn, gbc);
        
        dialog.setVisible(true);
    }

    private void logout() {
        Window win = SwingUtilities.getWindowAncestor(this);
        if (win != null) {
            win.dispose();
            new LoginWindow().setVisible(true);
        }
    }
}
