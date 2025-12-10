package com.gaminglounge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.gaminglounge.bll.InventoryService;
import com.gaminglounge.bll.StaffService;
import com.gaminglounge.model.Category;
import com.gaminglounge.model.InventoryReceipt;
import com.gaminglounge.model.InventoryReceiptDetail;
import com.gaminglounge.model.Product;
import com.gaminglounge.model.Staff;
import com.gaminglounge.model.User;

public class InventoryManagementPanel extends JPanel {
    private User currentUser;
    private Staff currentStaff;
    private InventoryService inventoryService;
    private StaffService staffService;

    // Product Tab Components
    private JTable productTable;
    private DefaultTableModel productModel;

    // Import/Export Tab Components
    private JComboBox<Product> productCombo;
    private JSpinner quantitySpinner;
    private JTextField priceField; // Only for Import
    private JTable receiptItemsTable;
    private DefaultTableModel receiptItemsModel;
    private List<InventoryReceiptDetail> currentReceiptDetails;
    private JComboBox<String> typeCombo;
    private JTextArea noteArea;

    // History Tab Components
    private JTable historyTable;
    private DefaultTableModel historyModel;

    public InventoryManagementPanel(User user) {
        this.currentUser = user;
        this.inventoryService = new InventoryService();
        this.staffService = new StaffService();
        this.currentStaff = staffService.getStaffByUserId(user.getUserId());
        this.currentReceiptDetails = new ArrayList<>();

        setLayout(new BorderLayout());
        
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        tabbedPane.addTab("Danh sách sản phẩm", createProductListPanel());
        
        // Allow both Admin and Staff to Import/Export
        tabbedPane.addTab("Nhập / Xuất kho", createImportExportPanel());
        
        tabbedPane.addTab("Lịch sử kho", createHistoryPanel());

        add(tabbedPane, BorderLayout.CENTER);
    }

    // ==================== PRODUCT LIST TAB ====================
    private JPanel createProductListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Thêm sản phẩm");
        JButton editBtn = new JButton("Sửa");
        JButton deleteBtn = new JButton("Xóa");
        JButton refreshBtn = new JButton("Làm mới");

        styleButton(addBtn, new Color(46, 204, 113));
        styleButton(editBtn, new Color(52, 152, 219));
        styleButton(deleteBtn, new Color(231, 76, 60));
        styleButton(refreshBtn, Color.GRAY);

        addBtn.addActionListener(e -> showProductDialog(null));
        editBtn.addActionListener(e -> editSelectedProduct());
        deleteBtn.addActionListener(e -> deleteSelectedProduct());
        refreshBtn.addActionListener(e -> loadProducts());

        toolbar.add(addBtn);
        toolbar.add(editBtn);
        toolbar.add(deleteBtn);
        toolbar.add(refreshBtn);
        panel.add(toolbar, BorderLayout.NORTH);

        // Table
        String[] columns = {"ID", "Tên sản phẩm", "Danh mục", "Giá bán", "Tồn kho", "Loại"};
        productModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productTable = new JTable(productModel);
        productTable.setRowHeight(25);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        panel.add(new JScrollPane(productTable), BorderLayout.CENTER);

        loadProducts();
        return panel;
    }

    private void loadProducts() {
        productModel.setRowCount(0);
        List<Product> products = inventoryService.getAllProducts();
        for (Product p : products) {
            productModel.addRow(new Object[]{
                p.getProductId(),
                p.getProductName(),
                p.getCategoryName(),
                String.format("%,.0f", p.getPrice()),
                p.getStockQuantity(),
                p.isService() ? "Dịch vụ" : "Sản phẩm"
            });
        }
    }

    private void showProductDialog(Product product) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), 
            product == null ? "Thêm sản phẩm" : "Sửa sản phẩm", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField nameField = new JTextField(20);
        JComboBox<Category> categoryCombo = new JComboBox<>(new Vector<>(inventoryService.getAllCategories()));
        JTextField priceField = new JTextField(10);
        JCheckBox serviceCheck = new JCheckBox("Là dịch vụ (Không quản lý tồn kho)");
        
        if (product != null) {
            nameField.setText(product.getProductName());
            priceField.setText(product.getPrice().toString());
            serviceCheck.setSelected(product.isService());
            // Select category
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                if (categoryCombo.getItemAt(i).getCategoryId() == product.getCategoryId()) {
                    categoryCombo.setSelectedIndex(i);
                    break;
                }
            }
        }

        // Add components
        gbc.gridx = 0; gbc.gridy = 0; dialog.add(new JLabel("Tên sản phẩm:"), gbc);
        gbc.gridx = 1; dialog.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; dialog.add(new JLabel("Danh mục:"), gbc);
        gbc.gridx = 1; dialog.add(categoryCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; dialog.add(new JLabel("Giá bán:"), gbc);
        gbc.gridx = 1; dialog.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; dialog.add(serviceCheck, gbc);

        JButton saveBtn = new JButton("Lưu");
        saveBtn.addActionListener(e -> {
            try {
                String name = nameField.getText();
                BigDecimal price = new BigDecimal(priceField.getText());
                Category cat = (Category) categoryCombo.getSelectedItem();
                
                Product p = (product == null) ? new Product() : product;
                p.setProductName(name);
                p.setCategoryId(cat.getCategoryId());
                p.setPrice(price);
                p.setService(serviceCheck.isSelected());
                if (product == null) p.setStockQuantity(0); // New product starts with 0 stock

                if (inventoryService.saveProduct(p)) {
                    JOptionPane.showMessageDialog(dialog, "Lưu thành công!");
                    dialog.dispose();
                    loadProducts();
                    updateProductCombo(); // Refresh combo in other tab
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lưu thất bại!");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Dữ liệu không hợp lệ: " + ex.getMessage());
            }
        });

        gbc.gridy = 4; dialog.add(saveBtn, gbc);
        dialog.setVisible(true);
    }

    private void editSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) productTable.getValueAt(row, 0);
        
        // Find product object (inefficient but simple for now)
        for (Product p : inventoryService.getAllProducts()) {
            if (p.getProductId() == id) {
                showProductDialog(p);
                return;
            }
        }
    }

    private void deleteSelectedProduct() {
        int row = productTable.getSelectedRow();
        if (row == -1) return;
        int id = (int) productTable.getValueAt(row, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa sản phẩm này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (inventoryService.deleteProduct(id)) {
                loadProducts();
                updateProductCombo();
            } else {
                JOptionPane.showMessageDialog(this, "Không thể xóa (có thể đang được sử dụng trong đơn hàng).");
            }
        }
    }

    // ==================== IMPORT/EXPORT TAB ====================
    private JPanel createImportExportPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top Form
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Thông tin phiếu"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        typeCombo = new JComboBox<>(new String[]{"Import", "Export"});
        productCombo = new JComboBox<>();
        updateProductCombo();
        quantitySpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
        priceField = new JTextField("0"); // Unit Price (Import only)
        
        // Auto-calculate import price (60% of selling price)
        productCombo.addActionListener(e -> updateImportPrice());
        typeCombo.addActionListener(e -> updateImportPrice());
        // Initial calculation
        updateImportPrice();

        noteArea = new JTextArea(3, 20);

        JButton addItemBtn = new JButton("Thêm vào phiếu");
        addItemBtn.addActionListener(e -> addReceiptItem());

        // Layout Form
        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("Loại phiếu:"), gbc);
        gbc.gridx = 1; formPanel.add(typeCombo, gbc);

        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Sản phẩm:"), gbc);
        gbc.gridx = 1; formPanel.add(productCombo, gbc);

        gbc.gridx = 2; gbc.gridy = 1; formPanel.add(new JLabel("Số lượng:"), gbc);
        gbc.gridx = 3; formPanel.add(quantitySpinner, gbc);

        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Đơn giá nhập (nếu có):"), gbc);
        gbc.gridx = 1; formPanel.add(priceField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; formPanel.add(new JScrollPane(noteArea), gbc);

        gbc.gridx = 4; gbc.gridy = 1; gbc.gridheight = 2;
        formPanel.add(addItemBtn, gbc);

        panel.add(formPanel, BorderLayout.NORTH);

        // Items Table
        String[] cols = {"ID SP", "Tên sản phẩm", "Số lượng", "Đơn giá"};
        receiptItemsModel = new DefaultTableModel(cols, 0);
        receiptItemsTable = new JTable(receiptItemsModel);
        panel.add(new JScrollPane(receiptItemsTable), BorderLayout.CENTER);

        // Bottom Actions
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton createReceiptBtn = new JButton("Tạo phiếu");
        styleButton(createReceiptBtn, new Color(46, 204, 113));
        createReceiptBtn.addActionListener(e -> createReceipt());
        
        JButton clearBtn = new JButton("Xóa danh sách");
        clearBtn.addActionListener(e -> {
            currentReceiptDetails.clear();
            receiptItemsModel.setRowCount(0);
        });

        bottomPanel.add(clearBtn);
        bottomPanel.add(createReceiptBtn);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void updateProductCombo() {
        productCombo.removeAllItems();
        for (Product p : inventoryService.getAllProducts()) {
            if (!p.isService()) { // Only physical products
                productCombo.addItem(p);
            }
        }
    }

    private void updateImportPrice() {
        if (typeCombo != null && "Import".equals(typeCombo.getSelectedItem())) {
            Product selectedProduct = (Product) productCombo.getSelectedItem();
            if (selectedProduct != null && priceField != null) {
                BigDecimal sellingPrice = selectedProduct.getPrice();
                // Import price is 40% less than selling price (i.e., 60% of selling price)
                BigDecimal importPrice = sellingPrice.multiply(new BigDecimal("0.6"));
                priceField.setText(importPrice.stripTrailingZeros().toPlainString());
            }
        } else if (priceField != null) {
            priceField.setText("0");
        }
    }

    private void addReceiptItem() {
        Product p = (Product) productCombo.getSelectedItem();
        if (p == null) return;

        int qty = (int) quantitySpinner.getValue();
        BigDecimal price = new BigDecimal(0);
        try {
            price = new BigDecimal(priceField.getText());
        } catch (Exception e) {}

        InventoryReceiptDetail detail = new InventoryReceiptDetail(p.getProductId(), qty, price);
        detail.setProductName(p.getProductName());
        
        currentReceiptDetails.add(detail);
        receiptItemsModel.addRow(new Object[]{p.getProductId(), p.getProductName(), qty, price});
    }

    private void createReceipt() {
        if (currentReceiptDetails.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Danh sách trống!");
            return;
        }
        if (currentStaff == null) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy thông tin nhân viên của bạn. (Admin cần có hồ sơ nhân viên)");
            return;
        }

        String type = (String) typeCombo.getSelectedItem();
        String note = noteArea.getText();

        if (inventoryService.createReceipt(currentStaff.getStaffId(), type, note, currentReceiptDetails)) {
            JOptionPane.showMessageDialog(this, "Tạo phiếu thành công!");
            currentReceiptDetails.clear();
            receiptItemsModel.setRowCount(0);
            noteArea.setText("");
            loadProducts(); // Refresh stock
            loadHistory(); // Refresh history
        } else {
            JOptionPane.showMessageDialog(this, "Tạo phiếu thất bại!");
        }
    }

    // ==================== HISTORY TAB ====================
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton refreshBtn = new JButton("Làm mới");
        refreshBtn.addActionListener(e -> loadHistory());
        panel.add(refreshBtn, BorderLayout.NORTH);

        String[] cols = {"ID", "Ngày tạo", "Loại", "Nhân viên", "Ghi chú"};
        historyModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyModel);
        historyTable.setRowHeight(25);
        
        // Double click to see details
        historyTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    int row = historyTable.getSelectedRow();
                    if (row != -1) {
                        int id = (int) historyTable.getValueAt(row, 0);
                        showReceiptDetails(id);
                    }
                }
            }
        });

        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        loadHistory();
        return panel;
    }

    private void loadHistory() {
        historyModel.setRowCount(0);
        List<InventoryReceipt> receipts = inventoryService.getAllReceipts();
        for (InventoryReceipt r : receipts) {
            historyModel.addRow(new Object[]{
                r.getReceiptId(),
                r.getReceiptDate(),
                r.getReceiptType(),
                r.getStaffName(),
                r.getNote()
            });
        }
    }

    private void showReceiptDetails(int receiptId) {
        List<InventoryReceiptDetail> details = inventoryService.getReceiptDetails(receiptId);
        StringBuilder sb = new StringBuilder("Chi tiết phiếu #" + receiptId + ":\n\n");
        for (InventoryReceiptDetail d : details) {
            sb.append("- ").append(d.getProductName())
              .append(": ").append(d.getQuantity())
              .append(" (Giá: ").append(d.getUnitPrice()).append(")\n");
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
    }
}
