package com.gaminglounge.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import com.gaminglounge.bll.CustomerService;
import com.gaminglounge.bll.OrderService;
import com.gaminglounge.bll.ProductService;
import com.gaminglounge.bll.ServiceRequestService;
import com.gaminglounge.bll.TransactionService; // Mới thêm
import com.gaminglounge.model.Customer;
import com.gaminglounge.model.Order;
import com.gaminglounge.model.OrderDetail;
import com.gaminglounge.model.Product;
import com.gaminglounge.model.ServiceRequest;
import com.gaminglounge.model.Transaction; // Mới thêm
import com.gaminglounge.model.User;

public class ClientDashboardWindow extends JFrame {
    // --- CẤU HÌNH GIÁ ---
    private static final BigDecimal PRICE_PER_HOUR = new BigDecimal("10000");
    private static final BigDecimal PRICE_PER_MINUTE = PRICE_PER_HOUR.divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP);

    private User currentUser;
    private Customer currentCustomer;
    
    // Services
    private CustomerService customerService;
    private ServiceRequestService requestService;
    private ProductService productService;
    private OrderService orderService;
    private TransactionService transactionService; // 1. Khai báo Service
    
    // UI Components
    private JLabel timeLabel;
    private JLabel balanceLabel;
    private JTextArea chatArea;
    private JTextField chatInput;
    private JTable productTable;
    private DefaultTableModel productModel;
    private DefaultTableModel historyModel; // Model cho bảng lịch sử
    
    // Timers
    private Timer clockTimer;
    private Timer syncTimer;
    private Timer chatPollTimer;
    
    private int remainingSeconds = 0;

    public ClientDashboardWindow(User user) {
        this.currentUser = user;
        initServices();
        
        this.currentCustomer = customerService.getCustomerByUserId(user.getUserId());
        if (currentCustomer == null) {
            JOptionPane.showMessageDialog(this, "Lỗi dữ liệu: Không tìm thấy thông tin khách hàng!");
            dispose();
            return;
        }

        // Frame Settings
        setTitle("Gaming Lounge Client - " + currentCustomer.getFullName());
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        createHeader();
        createMainTabs();
        createSidebar();

        recalculateTimeFromBalance();
        startTimers();
    }

    private void initServices() {
        this.customerService = new CustomerService();
        this.requestService = new ServiceRequestService();
        this.productService = new ProductService();
        this.orderService = new OrderService();
        this.transactionService = new TransactionService(); // 2. Khởi tạo Service
    }

    // --- GUI CONSTRUCTION (GIỮ NGUYÊN CODE CŨ CỦA BẠN) ---
    private void createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(30, 30, 30));
        header.setBorder(new EmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("CYBER GAMING");
        title.setForeground(new Color(46, 204, 113));
        title.setFont(new Font("Segoe UI", Font.BOLD, 24));
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        infoPanel.setOpaque(false);
        
        JLabel userLabel = new JLabel("Khách hàng: " + currentCustomer.getFullName());
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        
        balanceLabel = new JLabel(formatCurrency(currentCustomer.getBalance()));
        balanceLabel.setForeground(new Color(241, 196, 15));
        balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        JButton logoutBtn = new JButton("Đăng xuất");
        styleButton(logoutBtn, new Color(231, 76, 60));
        logoutBtn.setPreferredSize(new Dimension(100, 30));
        logoutBtn.addActionListener(e -> logout());

        infoPanel.add(userLabel);
        infoPanel.add(balanceLabel);
        infoPanel.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(infoPanel, BorderLayout.EAST);
        
        add(header, BorderLayout.NORTH);
    }

    private void createMainTabs() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        // Tab 1: Trang chủ
        JPanel homePanel = new JPanel(new GridBagLayout());
        homePanel.setBackground(new Color(44, 62, 80));
        
        timeLabel = new JLabel("Loading...");
        timeLabel.setFont(new Font("Consolas", Font.BOLD, 100));
        timeLabel.setForeground(Color.WHITE);
        
        JLabel subLabel = new JLabel("THỜI GIAN CÒN LẠI");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        subLabel.setForeground(new Color(189, 195, 199));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        homePanel.add(timeLabel, gbc);
        gbc.gridy = 1;
        gbc.insets = new Insets(10, 0, 0, 0);
        homePanel.add(subLabel, gbc);

        // Tab 2: Menu
        JPanel menuPanel = createOrderPanel();

        // Tab 3: Lịch sử (MỚI THÊM)
        JPanel historyPanel = createHistoryPanel();

        tabbedPane.addTab("Trang chủ", homePanel);
        tabbedPane.addTab("Gọi đồ ăn / Dịch vụ", menuPanel);
        tabbedPane.addTab("Lịch sử giao dịch", historyPanel); // Add vào Tab

        add(tabbedPane, BorderLayout.CENTER);
    }
    
    // --- HÀM MỚI: TẠO BẢNG LỊCH SỬ ---
private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Tạo bảng với 4 cột
        // Index: 0 (Loại), 1 (Số tiền), 2 (Thời gian), 3 (Trạng thái)
        String[] columns = {"Loại giao dịch", "Số tiền", "Thời gian", "Trạng thái"};
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable historyTable = new JTable(historyModel);
        // Lưu ý: Bỏ chữ "JTable" ở đầu để dùng biến toàn cục, tránh lỗi null pointer ở hàm khác
        
        // Gọi hàm trang trí bảng (nếu bạn có hàm styleTable)
        
        // --- SỬA LỖI Ở ĐÂY ---
        // Chỉnh độ rộng cột
        historyTable.getColumnModel().getColumn(0).setPreferredWidth(100); // Loại
        historyTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Số tiền
        // Cột 2 (Thời gian) để tự động
        historyTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Trạng thái (Sửa 4 thành 3)

        JScrollPane scrollPane = new JScrollPane(historyTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Nút làm mới
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton refreshBtn = new JButton("Cập nhật lịch sử");
        styleButton(refreshBtn, new Color(52, 152, 219));
        refreshBtn.addActionListener(e -> loadHistory());
        
        btnPanel.add(refreshBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Load dữ liệu lần đầu
        loadHistory();

        return panel;
    }
    private JPanel createOrderPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        String[] columns = {"ID", "Tên món", "Danh mục", "Đơn giá", "Tình trạng"};
        productModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        productTable = new JTable(productModel);
        productTable.setRowHeight(30);
        productTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loadProducts();

        JScrollPane scrollPane = new JScrollPane(productTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton refreshBtn = new JButton("Làm mới Menu");
        refreshBtn.addActionListener(e -> loadProducts());
        
        JButton orderBtn = new JButton("Đặt món này");
        styleButton(orderBtn, new Color(46, 204, 113));
        orderBtn.setPreferredSize(new Dimension(150, 40));
        orderBtn.addActionListener(e -> processOrder());

        btnPanel.add(refreshBtn);
        btnPanel.add(orderBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

 private void createSidebar() {
        // 1. Định nghĩa màu sắc cho Dark Mode
        Color darkBackground = new Color(30, 30, 30);      // Màu nền chính (Tối hẳn)
        Color inputBackground = new Color(50, 50, 50);     // Màu nền ô nhập liệu (Sáng hơn chút)
        Color textColor = Color.WHITE;                     // Màu chữ trắng

        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(320, 0));
        sidebar.setBackground(darkBackground); // Set nền tối
        sidebar.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- BUTTON PANEL ---
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        btnPanel.setOpaque(false);
        
        // Tạo Border với tiêu đề màu trắng
        TitledBorder utilBorder = BorderFactory.createTitledBorder("Tiện ích");
        utilBorder.setTitleColor(textColor); // Chữ tiêu đề màu trắng
        utilBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPanel.setBorder(utilBorder);
        
        JButton depositBtn = new JButton("Nạp tiền");
        styleButton(depositBtn, new Color(52, 152, 219));
        depositBtn.addActionListener(e -> requestDeposit());
        
        JButton helpBtn = new JButton("Gọi nhân viên");
        styleButton(helpBtn, new Color(155, 89, 182));
        helpBtn.addActionListener(e -> requestHelp());

        btnPanel.add(depositBtn);
        btnPanel.add(helpBtn);

        // --- CHAT PANEL ---
        JPanel chatPanel = new JPanel(new BorderLayout(0, 5));
        chatPanel.setOpaque(false);
        
        // Tạo Border Chat với tiêu đề màu trắng
        TitledBorder chatBorder = BorderFactory.createTitledBorder("Chat với nhân viên");
        chatBorder.setTitleColor(textColor); // Chữ tiêu đề màu trắng
        chatBorder.setTitleFont(new Font("Segoe UI", Font.BOLD, 14));
        chatPanel.setBorder(chatBorder);
        
        chatArea = new JTextArea();
        chatArea.setBackground(inputBackground); // Nền tối cho khung chat
        chatArea.setForeground(textColor);       // Chữ trắng
        chatArea.setCaretColor(textColor);       // Con trỏ nhấp nháy màu trắng
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // Đệm lề cho dễ nhìn
        chatArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); 
        
        JPanel inputPanel = new JPanel(new BorderLayout(5, 0));
        inputPanel.setOpaque(false); // Để lộ nền tối phía sau

        chatInput = new JTextField();
        chatInput.setBackground(inputBackground); // Nền tối cho ô nhập
        chatInput.setForeground(textColor);       // Chữ trắng
        chatInput.setCaretColor(textColor);       // Con trỏ trắng
        chatInput.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        // Đệm lề trong ô nhập
        chatInput.setBorder(BorderFactory.createCompoundBorder(
            chatInput.getBorder(), 
            BorderFactory.createEmptyBorder(2, 5, 2, 5)));

        JButton sendBtn = new JButton("Gửi");
        // Style nút gửi cho hợp theme tối (Ví dụ màu xanh đậm hoặc xám)
        styleButton(sendBtn, new Color(46, 204, 113)); 
        sendBtn.setPreferredSize(new Dimension(60, 0)); // Chỉnh lại kích thước nút gửi chút
        sendBtn.addActionListener(e -> sendChat());
        chatInput.addActionListener(e -> sendChat());
        
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        
        // JScrollPane cũng cần trong suốt hoặc nền tối để không bị viền trắng
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80))); // Viền xám mờ
        
        chatPanel.add(scrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // --- COMBINE ---
        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(btnPanel, BorderLayout.NORTH);
        topContainer.add(Box.createVerticalStrut(20), BorderLayout.CENTER);

        sidebar.add(topContainer, BorderLayout.NORTH);
        sidebar.add(chatPanel, BorderLayout.CENTER);

        add(sidebar, BorderLayout.EAST);
    }
    // --- LOGIC ---

    private void recalculateTimeFromBalance() {
        BigDecimal balance = currentCustomer.getBalance();
        BigDecimal hours = balance.divide(PRICE_PER_HOUR, 2, RoundingMode.FLOOR);
        BigDecimal minutes = hours.multiply(new BigDecimal("60"));
        
        int totalMinutes = minutes.intValue();
        
        balanceLabel.setText(formatCurrency(balance));
        timeLabel.setText(formatTime(totalMinutes, remainingSeconds));
        
        if (totalMinutes <= 0 && remainingSeconds <= 0) {
            timeLabel.setText("HẾT GIỜ");
            timeLabel.setForeground(Color.RED);
        } else {
            timeLabel.setForeground(Color.WHITE);
        }
    }

    private void processOrder() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một món để đặt!");
            return;
        }

        int productId = (int) productModel.getValueAt(selectedRow, 0);
        String productName = (String) productModel.getValueAt(selectedRow, 1);
        
        Product selectedProduct = null;
        for(Product p : productService.getAllProducts()) {
            if(p.getProductId() == productId) {
                selectedProduct = p; 
                break;
            }
        }

        if (selectedProduct == null) return;

        String qtyStr = JOptionPane.showInputDialog(this, "Nhập số lượng cho " + productName + ":", "1");
        if (qtyStr == null) return;
        
        int quantity;
        try {
            quantity = Integer.parseInt(qtyStr);
            if (quantity <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Số lượng không hợp lệ!");
            return;
        }

        BigDecimal totalCost = selectedProduct.getPrice().multiply(new BigDecimal(quantity));
        if (currentCustomer.getBalance().compareTo(totalCost) < 0) {
            JOptionPane.showMessageDialog(this, "Số dư không đủ! Cần: " + formatCurrency(totalCost), "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, 
                "Mua " + quantity + " x " + productName + "\nTổng tiền: " + formatCurrency(totalCost),
                "Xác nhận trừ tiền", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            Order order = new Order();
            order.setCustomerId(currentCustomer.getCustomerId());
            order.setStaffId(1);
            order.setTotalAmount(totalCost);
            
            List<OrderDetail> details = new ArrayList<>();
            OrderDetail detail = new OrderDetail();
            detail.setProductId(productId);
            detail.setQuantity(quantity);
            detail.setUnitPrice(selectedProduct.getPrice());
            details.add(detail);

            if (orderService.createOrder(order, details)) {
                JOptionPane.showMessageDialog(this, "Đặt hàng thành công!");
                currentCustomer.setBalance(currentCustomer.getBalance().subtract(totalCost));
                recalculateTimeFromBalance();
                loadProducts();
                loadHistory(); // Tự động load lại lịch sử
            } else {
                JOptionPane.showMessageDialog(this, "Đặt hàng thất bại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void startTimers() {
        clockTimer = new Timer(1000, e -> {
            if (currentCustomer.getBalance().compareTo(BigDecimal.ZERO) <= 0) {
                timeLabel.setText("HẾT GIỜ");
                return;
            }
            if (remainingSeconds > 0) {
                remainingSeconds--;
            } else {
                remainingSeconds = 59;
            }
            BigDecimal hours = currentCustomer.getBalance().divide(PRICE_PER_HOUR, 2, RoundingMode.FLOOR);
            int minutes = hours.multiply(new BigDecimal("60")).intValue();
            timeLabel.setText(formatTime(minutes, remainingSeconds));
        });
        clockTimer.start();

        syncTimer = new Timer(60000, e -> performMinuteDeductionAndSync());
        syncTimer.start();
        
        chatPollTimer = new Timer(3000, e -> refreshChat());
        chatPollTimer.start();
        refreshChat();
    }

private void performMinuteDeductionAndSync() {
        if (currentCustomer.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            // 1. Trừ tiền cho 1 phút chơi (Local)
            BigDecimal newBalance = currentCustomer.getBalance().subtract(PRICE_PER_MINUTE);
            
            // Xử lý số âm
            if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                newBalance = BigDecimal.ZERO;
            }
            currentCustomer.setBalance(newBalance);

            // 2. Tính toán số phút còn lại thực tế từ số tiền mới
            // Công thức: (Tiền / 10.000) * 60
            BigDecimal hours = newBalance.divide(PRICE_PER_HOUR, 2, RoundingMode.FLOOR);
            int newRemainingMinutes = hours.multiply(new BigDecimal("60")).intValue();
            currentCustomer.setRemainingTimeMinutes(newRemainingMinutes); // Cập nhật local object

            // 3. GHI XUỐNG DATABASE (Cả Tiền và Giờ)
            customerService.updateBalance(currentCustomer.getCustomerId(), newBalance);
            customerService.updateTime(currentCustomer.getCustomerId(), newRemainingMinutes); // <--- DÒNG QUAN TRỌNG MỚI THÊM
            
            // 4. (Optional) Sync ngược từ Server về (đề phòng Admin nạp tiền)
            Customer svCustomer = customerService.getCustomerById(currentCustomer.getCustomerId());
            if (svCustomer != null) {
                // Nếu Server nhiều tiền hơn Client > 1000đ -> Admin vừa nạp
                if (svCustomer.getBalance().subtract(currentCustomer.getBalance()).compareTo(new BigDecimal("1000")) > 0) {
                    currentCustomer.setBalance(svCustomer.getBalance());
                    // Nếu nạp tiền thì tính lại giờ luôn
                    recalculateTimeFromBalance(); 
                }
            }

            // 5. Cập nhật giao diện
            recalculateTimeFromBalance();
        }
    }
    // --- OTHER METHODS ---

    private void requestDeposit() {
        String input = JOptionPane.showInputDialog(this, "Nhập số tiền muốn nạp (VNĐ):");
        if (input != null && !input.trim().isEmpty()) {
            requestService.sendRequest(currentCustomer.getCustomerId(), "Nạp tiền", "Yêu cầu nạp: " + input);
            appendChat("Hệ thống: Đã gửi yêu cầu nạp " + input);
        }
    }
    
    private void requestHelp() {
         requestService.sendRequest(currentCustomer.getCustomerId(), "Hỗ trợ", "Khách hàng cần trợ giúp tại máy!");
         appendChat("Hệ thống: Đã gọi nhân viên.");
    }

    private void sendChat() {
        String msg = chatInput.getText().trim();
        if (!msg.isEmpty()) {
            requestService.sendRequest(currentCustomer.getCustomerId(), "Chat", msg);
            refreshChat();
            chatInput.setText("");
        }
    }
    
    private void refreshChat() {
        List<ServiceRequest> history = requestService.getCustomerHistory(currentCustomer.getCustomerId());
        StringBuilder sb = new StringBuilder();
        for (ServiceRequest req : history) {
            if ("Chat".equals(req.getRequestType())) {
                String sender = req.getSender() == null ? "Client" : req.getSender();
                String prefix = "Admin".equals(sender) ? "QUẢN LÝ: " : "BẠN: ";
                sb.append(prefix).append(req.getContent()).append("\n");
            }
        }
        if (!chatArea.getText().equals(sb.toString())) {
            chatArea.setText(sb.toString());
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    private void appendChat(String msg) {
        chatArea.append(msg + "\n");
    }

    private void loadProducts() {
        productModel.setRowCount(0);
        List<Product> products = productService.getAllProducts();
        for (Product p : products) {
            if (p.getStockQuantity() > 0 || p.isService()) {
                productModel.addRow(new Object[]{
                    p.getProductId(),
                    p.getProductName(),
                    p.getCategoryName(),
                    formatCurrency(p.getPrice()),
                    p.isService() ? "Dịch vụ" : ("Kho: " + p.getStockQuantity())
                });
            }
        }
    }
    
    // --- HÀM LOAD DỮ LIỆU LỊCH SỬ ---
// --- HÀM LOAD LỊCH SỬ (GỘP GIAO DỊCH + YÊU CẦU NẠP) ---
    private void loadHistory() {
        // Kiểm tra null để tránh lỗi khi khởi tạo
        if (historyModel == null) return;
        
        // Xóa dữ liệu cũ trên bảng
        historyModel.setRowCount(0);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        
        // Tạo danh sách chung để chứa cả 2 loại dữ liệu rồi sắp xếp
        // Object[] gồm: {Loại, Nội dung, Thời gian hiển thị, Trạng thái, DateObject(để sort)}
        List<Object[]> combinedList = new ArrayList<>();

        // 1. Lấy Lịch sử Giao dịch (Transaction) - Tiền thật đã biến động
        List<Transaction> transactions = transactionService.getHistoryByCustomer(currentCustomer.getCustomerId());
        for (Transaction t : transactions) {
            String type = t.getTransactionType();
            // Thêm dấu + hoặc - trước số tiền cho trực quan
            String prefix = (type.contains("Nạp") || type.contains("Deposit")) ? "+" : "-";
            
            combinedList.add(new Object[]{
                t.getTransactionType(),                 // Cột 1: Loại
                prefix + formatCurrency(t.getAmount().abs()), // Cột 2: Số tiền
                sdf.format(t.getTransactionDate()),     // Cột 3: Thời gian
                "Hoàn thành",                           // Cột 4: Trạng thái (Giao dịch xong luôn là hoàn thành)
                t.getTransactionDate()                  // (Dùng để sort, không hiện lên bảng)
            });
        }

        // 2. Lấy Lịch sử Yêu cầu (ServiceRequest) - Chỉ lấy yêu cầu Nạp tiền
        List<ServiceRequest> requests = requestService.getCustomerHistory(currentCustomer.getCustomerId());
        for (ServiceRequest r : requests) {
            if ("Nạp tiền".equals(r.getRequestType())) {
                // Xử lý hiển thị trạng thái: Pending -> Đang chờ
                String statusDisplay;
                if ("Pending".equalsIgnoreCase(r.getStatus())) {
                    statusDisplay = "Đang chờ xử lý";
                } else if ("Completed".equalsIgnoreCase(r.getStatus())) {
                    statusDisplay = "Đã duyệt";
                } else {
                    statusDisplay = r.getStatus();
                }
                
                // Format nội dung cho gọn (Bỏ chữ "Yêu cầu nạp: " đi nếu có)
                String content = r.getContent().replace("Yêu cầu nạp: ", "") + " (Yêu cầu)";

                combinedList.add(new Object[]{
                    "Yêu cầu nạp",                      // Cột 1
                    content,                            // Cột 2
                    sdf.format(r.getCreatedAt()),       // Cột 3
                    statusDisplay,                      // Cột 4: Trạng thái
                    r.getCreatedAt()                    // (Dùng để sort)
                });
            }
        }

        // 3. Sắp xếp danh sách gộp theo thời gian giảm dần (Mới nhất lên đầu)
        combinedList.sort((o1, o2) -> ((java.util.Date)o2[4]).compareTo((java.util.Date)o1[4]));

        // 4. Đẩy dữ liệu đã sắp xếp vào bảng (Bỏ phần tử cuối cùng dùng để sort)
        for (Object[] row : combinedList) {
            historyModel.addRow(new Object[]{
                row[0], // Loại
                row[1], // Nội dung/Số tiền
                row[2], // Thời gian
                row[3]  // Trạng thái
            });
        }
    }

    private String formatTime(int minutes, int seconds) {
        int h = minutes / 60;
        int m = minutes % 60;
        return String.format("%02d:%02d:%02d", h, m, seconds);
    }
    
    private String formatCurrency(BigDecimal amount) {
        return String.format("%,.0f VNĐ", amount);
    }

    private void styleButton(JButton btn, Color bg) {
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setFocusPainted(false);
    }

    private void logout() {
        performMinuteDeductionAndSync(); 
        clockTimer.stop();
        syncTimer.stop();
        chatPollTimer.stop();
        dispose();
        new LoginWindow().setVisible(true);
    }
}