package com.gaminglounge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.gaminglounge.bll.CustomerService;
import com.gaminglounge.bll.InventoryService;
import com.gaminglounge.bll.OrderService;
import com.gaminglounge.bll.TransactionService;
import com.gaminglounge.model.Customer;
import com.gaminglounge.model.Order;
import com.gaminglounge.model.OrderDetail;
import com.gaminglounge.model.Product;
import com.gaminglounge.model.Transaction;

import java.awt.event.ItemEvent;
import java.util.ArrayList;

public class TransactionManagementPanel extends JPanel {
    // Transaction History Components
    private TransactionService transactionService;
    private CustomerService customerService;
    private InventoryService inventoryService;
    private OrderService orderService;
    private JTable transactionTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;

    public TransactionManagementPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Tab 1: Lịch sử giao dịch (The original content)
        JPanel historyPanel = createTransactionHistoryPanel();
        tabbedPane.addTab("Lịch sử giao dịch", historyPanel);
        
        // Tab 2: Danh sách hóa đơn (OrderManagementPanel)
        tabbedPane.addTab("Danh sách hóa đơn", new OrderManagementPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createTransactionHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 0, 0, 0));
        transactionService = new TransactionService();
        customerService = new CustomerService();
        inventoryService = new InventoryService();
        orderService = new OrderService();

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton createButton = createToolbarButton("Tạo giao dịch", new Color(46, 204, 113));
        JButton detailsButton = createToolbarButton("Xem chi tiết", null);
        JButton printButton = createToolbarButton("In hóa đơn", new Color(52, 152, 219));
        JButton refreshButton = createToolbarButton("Làm mới", null);
        
        createButton.addActionListener(e -> showCreateTransactionDialog());
        detailsButton.addActionListener(e -> showTransactionDetails());
        printButton.addActionListener(e -> printInvoice());
        refreshButton.addActionListener(e -> loadData());
        
        toolBar.add(createButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(detailsButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(printButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(refreshButton);
        
        // Search Bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchField = new JTextField(20);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JButton searchButton = new JButton("Tìm kiếm");
        searchButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchButton.addActionListener(e -> searchTransactions());
        
        searchPanel.add(new JLabel("Tìm kiếm: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(toolBar, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);
        
        panel.add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Khách hàng", "Nhân viên", "Số tiền", "Loại GD", "Ngày giờ", "Ghi chú"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        transactionTable = new JTable(tableModel);
        transactionTable.setRowHeight(25);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        panel.add(scrollPane, BorderLayout.CENTER);

        loadData();
        return panel;
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
        List<Transaction> list = transactionService.getAllTransactions();
        for (Transaction t : list) {
            tableModel.addRow(new Object[]{
                t.getTransactionId(),
                t.getCustomerName(),
                t.getStaffName(),
                t.getAmount(),
                t.getTransactionType(),
                t.getTransactionDate(),
                t.getNote()
            });
        }
    }
    
    private void searchTransactions() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadData();
            return;
        }
        tableModel.setRowCount(0);
        List<Transaction> list = transactionService.searchTransactions(keyword);
        for (Transaction t : list) {
            tableModel.addRow(new Object[]{
                t.getTransactionId(),
                t.getCustomerName(),
                t.getStaffName(),
                t.getAmount(),
                t.getTransactionType(),
                t.getTransactionDate(),
                t.getNote()
            });
        }
    }

    private void showCreateTransactionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Tạo giao dịch mới", true);
        dialog.setLayout(new BorderLayout());
        dialog.setSize(500, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 1.0;
        
        // Customer Dropdown
        List<Customer> customers = customerService.getAllCustomers();
        JComboBox<CustomerItem> customerBox = new JComboBox<>();
        for (Customer c : customers) {
            customerBox.addItem(new CustomerItem(c.getCustomerId(), c.getFullName() + " (" + c.getUsername() + ")"));
        }
        
        // Transaction Type
        String[] types = {"Nạp tiền", "Thanh toán dịch vụ", "Mua hàng"};
        JComboBox<String> typeBox = new JComboBox<>(types);
        
        // Amount Field (for Top Up / Service)
        JTextField amountField = new JTextField();
        JLabel amountLabel = new JLabel("Số tiền:");
        
        // Product Fields (for Buy Item)
        JLabel productLabel = new JLabel("Sản phẩm:");
        JComboBox<ProductItem> productBox = new JComboBox<>();
        List<Product> products = inventoryService.getAllProducts();
        for (Product p : products) {
            productBox.addItem(new ProductItem(p.getProductId(), p.getProductName() + " (Kho: " + p.getStockQuantity() + ") - " + p.getPrice()));
        }
        
        JLabel quantityLabel = new JLabel("Số lượng:");
        JTextField quantityField = new JTextField("1");
        
        JTextArea noteArea = new JTextArea(3, 20);
        noteArea.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // Add fields
        addFormField(formPanel, gbc, 0, "Khách hàng:", customerBox);
        addFormField(formPanel, gbc, 1, "Loại giao dịch:", typeBox);
        
        // Dynamic Fields
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(amountLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(amountField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(productLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(productBox, gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(quantityLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(quantityField, gbc);
        
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1;
        formPanel.add(new JScrollPane(noteArea), gbc);

        // Logic to show/hide fields
        Runnable updateFields = () -> {
            String type = (String) typeBox.getSelectedItem();
            boolean isBuyItem = "Mua hàng".equals(type);
            
            amountLabel.setVisible(!isBuyItem);
            amountField.setVisible(!isBuyItem);
            
            productLabel.setVisible(isBuyItem);
            productBox.setVisible(isBuyItem);
            quantityLabel.setVisible(isBuyItem);
            quantityField.setVisible(isBuyItem);
            
            dialog.revalidate();
            dialog.repaint();
        };
        
        typeBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) updateFields.run();
        });
        updateFields.run(); // Initial state
        
        dialog.add(formPanel, BorderLayout.CENTER);
        
        JButton saveButton = new JButton("Xác nhận");
        saveButton.setBackground(new Color(46, 204, 113));
        saveButton.setForeground(Color.WHITE);
        saveButton.addActionListener(e -> {
            try {
                CustomerItem selectedCustomer = (CustomerItem) customerBox.getSelectedItem();
                if (selectedCustomer == null) return;
                
                String type = (String) typeBox.getSelectedItem();
                String note = noteArea.getText();
                boolean success = false;
                
                if ("Mua hàng".equals(type)) {
                    // Buy Item Logic
                    ProductItem selectedProduct = (ProductItem) productBox.getSelectedItem();
                    if (selectedProduct == null) {
                        JOptionPane.showMessageDialog(dialog, "Vui lòng chọn sản phẩm.");
                        return;
                    }
                    int quantity = Integer.parseInt(quantityField.getText());
                    if (quantity <= 0) throw new NumberFormatException();
                    
                    // Find product to get price
                    Product product = products.stream().filter(p -> p.getProductId() == selectedProduct.id).findFirst().orElse(null);
                    if (product == null) return;
                    
                    BigDecimal price = product.getPrice();
                    BigDecimal total = price.multiply(new BigDecimal(quantity));
                    
                    Order order = new Order();
                    order.setCustomerId(selectedCustomer.id);
                    order.setStaffId(1); // Default Staff ID 1
                    order.setTotalAmount(total);
                    
                    OrderDetail detail = new OrderDetail();
                    detail.setProductId(product.getProductId());
                    detail.setQuantity(quantity);
                    detail.setUnitPrice(price);
                    detail.setSubTotal(total);
                    
                    List<OrderDetail> details = new ArrayList<>();
                    details.add(detail);
                    
                    success = orderService.createOrder(order, details);
                    
                } else {
                    // Top Up / Charge Logic
                    BigDecimal amount = new BigDecimal(amountField.getText());
                    if ("Nạp tiền".equals(type)) {
                        success = customerService.topUp(selectedCustomer.id, amount, 1, note);
                    } else {
                        success = customerService.chargeCustomer(selectedCustomer.id, amount, 1, note);
                    }
                }
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog, "Giao dịch thành công!");
                    dialog.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Giao dịch thất bại (Kiểm tra số dư hoặc kho hàng).");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Dữ liệu nhập không hợp lệ.");
            }
        });
        
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(saveButton);
        dialog.add(btnPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void showTransactionDetails() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giao dịch để xem.");
            return;
        }
        
        String id = tableModel.getValueAt(selectedRow, 0).toString();
        String customer = tableModel.getValueAt(selectedRow, 1).toString();
        String staff = tableModel.getValueAt(selectedRow, 2).toString();
        String amount = tableModel.getValueAt(selectedRow, 3).toString();
        String type = tableModel.getValueAt(selectedRow, 4).toString();
        String date = tableModel.getValueAt(selectedRow, 5).toString();
        String note = tableModel.getValueAt(selectedRow, 6) != null ? tableModel.getValueAt(selectedRow, 6).toString() : "";
        
        String message = String.format(
            "Mã GD: %s\n" +
            "Loại: %s\n" +
            "Khách hàng: %s\n" +
            "Nhân viên: %s\n" +
            "Số tiền: %s\n" +
            "Thời gian: %s\n" +
            "Ghi chú: %s\n",
            id, type, customer, staff, amount, date, note
        );
        
        JOptionPane.showMessageDialog(this, message, "Chi tiết giao dịch", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void printInvoice() {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn giao dịch để in hóa đơn.");
            return;
        }
        // Mock printing
        JOptionPane.showMessageDialog(this, "Đang in hóa đơn ra máy in... (Mô phỏng)", "In hóa đơn", JOptionPane.INFORMATION_MESSAGE);
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
    
    private static class CustomerItem {
        int id;
        String label;
        public CustomerItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }

    private static class ProductItem {
        int id;
        String label;
        public ProductItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}
