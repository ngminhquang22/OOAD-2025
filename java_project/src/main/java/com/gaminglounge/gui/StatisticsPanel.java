package com.gaminglounge.gui;

import com.gaminglounge.bll.StatisticsService;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StatisticsPanel extends JPanel {
    private StatisticsService statisticsService;
    private JTextField startDateField;
    private JTextField endDateField;
    
    // Revenue Components
    private JLabel lblDepositRevenue;
    private JLabel lblOrderRevenue;
    private JLabel lblSessionRevenue;
    private JLabel lblTotalRevenue;
    private JTable revenueTable;
    private DefaultTableModel revenueModel;

    // Inventory Components
    private JLabel lblImportCount;
    private JLabel lblExportCount;
    private JLabel lblImportValue;
    
    public StatisticsPanel() {
        statisticsService = new StatisticsService();
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. Top Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBorder(new TitledBorder("Bộ lọc thời gian"));
        
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        
        startDateField = new JTextField(firstDay.toString(), 10);
        endDateField = new JTextField(now.toString(), 10);
        JButton btnFilter = new JButton("Xem báo cáo");
        btnFilter.setBackground(new Color(0, 122, 204));
        btnFilter.setForeground(Color.WHITE);
        
        JButton btnPrint = new JButton("In báo cáo");
        
        filterPanel.add(new JLabel("Từ ngày (yyyy-MM-dd):"));
        filterPanel.add(startDateField);
        filterPanel.add(new JLabel("Đến ngày:"));
        filterPanel.add(endDateField);
        filterPanel.add(btnFilter);
        filterPanel.add(btnPrint);
        
        add(filterPanel, BorderLayout.NORTH);

        // 2. Main Content (Tabs)
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("Báo cáo Doanh thu", createRevenuePanel());
        tabbedPane.addTab("Báo cáo Kho hàng", createInventoryPanel());
        
        add(tabbedPane, BorderLayout.CENTER);

        // Actions
        btnFilter.addActionListener(e -> loadStatistics());
        btnPrint.addActionListener(e -> printReport());
        
        // Initial Load
        loadStatistics();
    }

    private JPanel createRevenuePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Summary Cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        lblDepositRevenue = createSummaryCard(summaryPanel, "Doanh thu nạp tiền (Dịch vụ)", new Color(46, 204, 113));
        lblSessionRevenue = createSummaryCard(summaryPanel, "Doanh thu bán giờ chơi", new Color(155, 89, 182));
        lblOrderRevenue = createSummaryCard(summaryPanel, "Giá trị dịch vụ đã dùng", new Color(52, 152, 219)); // Consumption
        lblTotalRevenue = createSummaryCard(summaryPanel, "Tổng doanh thu thực tế", new Color(230, 126, 34));
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        
        // Table
        String[] columnNames = {"Ngày", "Doanh thu nạp tiền", "Doanh thu bán giờ"};
        revenueModel = new DefaultTableModel(columnNames, 0);
        revenueTable = new JTable(revenueModel);
        revenueTable.setRowHeight(25);
        revenueTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        JScrollPane scrollPane = new JScrollPane(revenueTable);
        scrollPane.setBorder(new TitledBorder("Chi tiết doanh thu theo ngày"));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }

    private JPanel createInventoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        // Summary Cards
        JPanel summaryPanel = new JPanel(new GridLayout(1, 3, 10, 10));
        lblImportCount = createSummaryCard(summaryPanel, "Số phiếu nhập", new Color(46, 204, 113));
        lblExportCount = createSummaryCard(summaryPanel, "Số phiếu xuất", new Color(231, 76, 60));
        lblImportValue = createSummaryCard(summaryPanel, "Tổng giá trị nhập", new Color(52, 152, 219));
        
        panel.add(summaryPanel, BorderLayout.NORTH);
        
        // Placeholder for chart or list
        JLabel infoLabel = new JLabel("<html><center><h2>Thống kê Nhập - Xuất Kho</h2><p>Chọn khoảng thời gian để xem tổng hợp số liệu.</p></center></html>");
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(infoLabel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JLabel createSummaryCard(JPanel parent, String title, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel lblValue = new JLabel("0");
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        
        parent.add(card);
        return lblValue;
    }

    private void loadStatistics() {
        try {
            LocalDate startDate = LocalDate.parse(startDateField.getText());
            LocalDate endDate = LocalDate.parse(endDateField.getText());
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            
            // --- Revenue Tab ---
            double deposit = statisticsService.getTotalDepositRevenue(startDate, endDate);
            double timeSales = statisticsService.getTotalTimeSalesRevenue(startDate, endDate);
            double orderConsumption = statisticsService.getTotalOrderRevenue(startDate, endDate);
            
            // Total Revenue = Deposit (Services) + Time Sales
            double totalRevenue = deposit + timeSales;
            
            lblDepositRevenue.setText(nf.format(deposit));
            lblSessionRevenue.setText(nf.format(timeSales));
            lblOrderRevenue.setText(nf.format(orderConsumption));
            lblTotalRevenue.setText(nf.format(totalRevenue));
            
            // Daily Deposit Table
            Map<String, Double> dailyData = statisticsService.getDailyDepositRevenue(startDate, endDate);
            Map<String, Double> sortedData = new TreeMap<>(dailyData);
            
            revenueModel.setRowCount(0);
            for (Map.Entry<String, Double> entry : sortedData.entrySet()) {
                revenueModel.addRow(new Object[]{entry.getKey(), nf.format(entry.getValue()), "-"});
            }
            
            // --- Inventory Tab ---
            int importCount = statisticsService.getImportCount(startDate, endDate);
            int exportCount = statisticsService.getExportCount(startDate, endDate);
            double importVal = statisticsService.getImportValue(startDate, endDate);
            
            lblImportCount.setText(String.valueOf(importCount));
            lblExportCount.setText(String.valueOf(exportCount));
            lblImportValue.setText(nf.format(importVal));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi định dạng ngày hoặc dữ liệu: " + e.getMessage());
        }
    }
    
    private void printReport() {
        try {
            boolean complete = revenueTable.print();
            if (complete) {
                JOptionPane.showMessageDialog(this, "In thành công!");
            } else {
                JOptionPane.showMessageDialog(this, "Đã hủy in.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi khi in: " + e.getMessage());
        }
    }
}
