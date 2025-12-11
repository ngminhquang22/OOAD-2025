package com.gaminglounge.gui;

import com.gaminglounge.bll.StaffService;
import com.gaminglounge.bll.TimekeepingService;
import com.gaminglounge.bll.WorkScheduleService;
import com.gaminglounge.model.Staff;
import com.gaminglounge.model.Timekeeping;
import com.gaminglounge.model.WorkSchedule;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class StaffManagementPanel extends JPanel {
    private StaffService staffService;
    private TimekeepingService timekeepingService;
    private WorkScheduleService workScheduleService;
    
    private JTable staffTable;
    private DefaultTableModel staffTableModel;
    
    private JTable timekeepingTable;
    private DefaultTableModel timekeepingTableModel;

    // New Shift Components (Weekly Timetable)
    private JTable timetable;
    private DefaultTableModel timetableModel;
    private Calendar currentWeekStart;
    private JLabel weekLabel;

    public StaffManagementPanel() {
        staffService = new StaffService();
        timekeepingService = new TimekeepingService();
        workScheduleService = new WorkScheduleService();
        
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        tabbedPane.addTab("Danh sách nhân viên", createStaffPanel());
        tabbedPane.addTab("Lịch làm việc (Tuần)", createShiftPanel());
        tabbedPane.addTab("Chấm công", createTimekeepingPanel());
        
        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createStaffPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton addButton = createToolbarButton("Thêm nhân viên", new Color(0, 122, 204));
        JButton editButton = createToolbarButton("Sửa", null);
        JButton deleteButton = createToolbarButton("Xóa", new Color(231, 76, 60));
        JButton refreshButton = createToolbarButton("Làm mới", null);

        addButton.addActionListener(e -> showAddDialog());
        editButton.addActionListener(e -> showEditDialog());
        deleteButton.addActionListener(e -> deleteStaff());
        refreshButton.addActionListener(e -> loadStaffData());

        toolBar.add(addButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(editButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(deleteButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(refreshButton);
        
        panel.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Họ tên", "Tài khoản", "Email", "Vị trí", "Lương"};
        staffTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        staffTable = new JTable(staffTableModel);
        staffTable.setRowHeight(25);
        staffTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        staffTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(staffTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadStaffData();
        return panel;
    }

    private JPanel createShiftPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Initialize Calendar to start of current week (Monday)
        currentWeekStart = Calendar.getInstance();
        currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        // If today is Sunday, Calendar.MONDAY might return next week depending on locale, 
        // but usually it's fine. Let's ensure we strip time.
        currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
        currentWeekStart.set(Calendar.MINUTE, 0);
        currentWeekStart.set(Calendar.SECOND, 0);
        currentWeekStart.set(Calendar.MILLISECOND, 0);

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton prevWeekBtn = createToolbarButton("< Tuần trước", null);
        JButton nextWeekBtn = createToolbarButton("Tuần sau >", null);
        JButton currentWeekBtn = createToolbarButton("Tuần hiện tại", null);
        JButton addShiftBtn = createToolbarButton("Thêm lịch", new Color(0, 122, 204));
        JButton deleteShiftBtn = createToolbarButton("Xóa lịch", new Color(231, 76, 60));
        
        weekLabel = new JLabel("", SwingConstants.CENTER);
        weekLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        weekLabel.setPreferredSize(new Dimension(250, 30));
        
        prevWeekBtn.addActionListener(e -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, -1);
            loadWeeklySchedule();
        });
        
        nextWeekBtn.addActionListener(e -> {
            currentWeekStart.add(Calendar.WEEK_OF_YEAR, 1);
            loadWeeklySchedule();
        });
        
        currentWeekBtn.addActionListener(e -> {
            currentWeekStart = Calendar.getInstance();
            currentWeekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            currentWeekStart.set(Calendar.HOUR_OF_DAY, 0);
            currentWeekStart.set(Calendar.MINUTE, 0);
            currentWeekStart.set(Calendar.SECOND, 0);
            currentWeekStart.set(Calendar.MILLISECOND, 0);
            loadWeeklySchedule();
        });

        addShiftBtn.addActionListener(e -> showAssignShiftDialog(null));
        deleteShiftBtn.addActionListener(e -> deleteSelectedShift());

        toolBar.add(prevWeekBtn);
        toolBar.add(currentWeekBtn);
        toolBar.add(nextWeekBtn);
        toolBar.addSeparator();
        toolBar.add(weekLabel);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(addShiftBtn);
        toolBar.addSeparator(new Dimension(5, 0));
        toolBar.add(deleteShiftBtn);
        
        panel.add(toolBar, BorderLayout.NORTH);
        
        // Timetable Grid
        // Columns: Shift Name, Mon, Tue, Wed, Thu, Fri, Sat, Sun
        String[] columnNames = {"Ca / Thứ", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        timetableModel = new DefaultTableModel(columnNames, 3) { // 3 Rows: Morning, Afternoon, Evening
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        
        timetable = new JTable(timetableModel);
        timetable.setRowHeight(120); // Taller rows
        timetable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        timetable.getTableHeader().setBackground(new Color(230, 240, 255));
        timetable.getTableHeader().setPreferredSize(new Dimension(0, 40));
        timetable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        timetable.setGridColor(new Color(200, 200, 200));
        timetable.setShowGrid(true);
        timetable.setIntercellSpacing(new Dimension(1, 1));
        
        // Custom Cell Renderer for Multi-line text
        timetable.setDefaultRenderer(Object.class, new MultiLineCellRenderer());
        
        // Double click to add shift
        timetable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = timetable.getSelectedRow();
                    int col = timetable.getSelectedColumn();
                    if (col > 0) { // Skip first column (Shift Name)
                        handleCellDoubleClick(row, col);
                    }
                }
            }
        });

        panel.add(new JScrollPane(timetable), BorderLayout.CENTER);
        
        loadWeeklySchedule();
        return panel;
    }

    private void handleCellDoubleClick(int row, int col) {
        // Calculate date based on column
        Calendar cal = (Calendar) currentWeekStart.clone();
        cal.add(Calendar.DAY_OF_YEAR, col - 1); // col 1 is Mon (start), col 2 is Tue...
        Date selectedDate = new Date(cal.getTimeInMillis());
        
        // Determine shift based on row
        String shiftType = "";
        if (row == 0) shiftType = "Ca Sáng";
        else if (row == 1) shiftType = "Ca Chiều";
        else if (row == 2) shiftType = "Ca Đêm";
        
        showAssignShiftDialog(selectedDate, shiftType);
    }

    private void deleteSelectedShift() {
        int row = timetable.getSelectedRow();
        int col = timetable.getSelectedColumn();
        
        if (row == -1 || col <= 0) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một ô lịch làm việc (không phải tiêu đề) để xóa.");
            return;
        }
        
        // Calculate Date
        Calendar cal = (Calendar) currentWeekStart.clone();
        cal.add(Calendar.DAY_OF_YEAR, col - 1);
        Date selectedDate = new Date(cal.getTimeInMillis());
        
        // Fetch schedules for this date
        List<WorkSchedule> dailySchedules = workScheduleService.getSchedulesByDate(selectedDate);
        java.util.List<WorkSchedule> slotSchedules = new java.util.ArrayList<>();
        
        // Filter by Row (Shift Slot)
        for (WorkSchedule ws : dailySchedules) {
            String startStr = ws.getShiftStart().toString();
            int startHour = Integer.parseInt(startStr.split(":")[0]);
            
            int targetRow = -1;
            if (startHour >= 6 && startHour < 14) targetRow = 0;
            else if (startHour >= 14 && startHour < 22) targetRow = 1;
            else targetRow = 2;
            
            if (targetRow == row) {
                slotSchedules.add(ws);
            }
        }
        
        if (slotSchedules.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không có lịch làm việc nào trong ô này.");
            return;
        }
        
        WorkSchedule selectedSchedule = null;
        if (slotSchedules.size() == 1) {
            selectedSchedule = slotSchedules.get(0);
        } else {
            // Multiple shifts in this slot, ask user to pick
            WorkSchedule[] choices = slotSchedules.toArray(new WorkSchedule[0]);
            selectedSchedule = (WorkSchedule) JOptionPane.showInputDialog(
                this, 
                "Chọn nhân viên để xóa lịch:", 
                "Xóa lịch làm việc", 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                choices, 
                choices[0]);
        }
        
        if (selectedSchedule != null) {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Xóa lịch làm của " + selectedSchedule.getStaffName() + " (" + selectedSchedule.getShiftStart() + "-" + selectedSchedule.getShiftEnd() + ")?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (workScheduleService.deleteSchedule(selectedSchedule.getScheduleId())) {
                    JOptionPane.showMessageDialog(this, "Đã xóa lịch làm việc.");
                    loadWeeklySchedule();
                } else {
                    JOptionPane.showMessageDialog(this, "Xóa thất bại.");
                }
            }
        }
    }

    private void loadWeeklySchedule() {
        // Update Week Label
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar endOfWeek = (Calendar) currentWeekStart.clone();
        endOfWeek.add(Calendar.DAY_OF_YEAR, 6);
        weekLabel.setText("Tuần: " + sdf.format(currentWeekStart.getTime()) + " - " + sdf.format(endOfWeek.getTime()));
        
        // Update Column Headers with Dates
        Calendar cal = (Calendar) currentWeekStart.clone();
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ Nhật"};
        SimpleDateFormat daySdf = new SimpleDateFormat("dd/MM");
        
        timetable.getColumnModel().getColumn(0).setHeaderValue("Ca / Thứ");
        for (int i = 0; i < 7; i++) {
            timetable.getColumnModel().getColumn(i + 1).setHeaderValue(days[i] + " (" + daySdf.format(cal.getTime()) + ")");
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        timetable.getTableHeader().repaint();
        
        // Clear Data
        for (int r = 0; r < 3; r++) {
            for (int c = 1; c <= 7; c++) {
                timetableModel.setValueAt("", r, c);
            }
        }
        timetableModel.setValueAt("Ca Sáng\n(07:00 - 15:00)", 0, 0);
        timetableModel.setValueAt("Ca Chiều\n(15:00 - 23:00)", 1, 0);
        timetableModel.setValueAt("Ca Đêm\n(23:00 - 07:00)", 2, 0);
        
        // Fetch Data
        Date startDate = new Date(currentWeekStart.getTimeInMillis());
        Date endDate = new Date(endOfWeek.getTimeInMillis());
        List<WorkSchedule> schedules = workScheduleService.getSchedulesByDateRange(startDate, endDate);
        
        // Map Data to Grid
        for (WorkSchedule ws : schedules) {
            // Determine Column (Day of Week)
            Calendar wsCal = Calendar.getInstance();
            wsCal.setTime(ws.getWorkDate());
            
            // Calculate day difference from start of week
            long diff = ws.getWorkDate().getTime() - startDate.getTime();
            int dayIndex = (int) (diff / (1000 * 60 * 60 * 24)) + 1; // +1 because col 0 is label
            
            if (dayIndex < 1 || dayIndex > 7) continue; // Should not happen if query is correct
            
            // Determine Row (Shift)
            int rowIndex = -1;
            String startStr = ws.getShiftStart().toString();
            // Simple logic: check hour
            int startHour = Integer.parseInt(startStr.split(":")[0]);
            
            if (startHour >= 6 && startHour < 14) rowIndex = 0; // Morning
            else if (startHour >= 14 && startHour < 22) rowIndex = 1; // Afternoon
            else rowIndex = 2; // Night (23 or later/early morning)
            
            if (rowIndex != -1) {
                String currentVal = (String) timetableModel.getValueAt(rowIndex, dayIndex);
                String newVal = (currentVal == null || currentVal.isEmpty()) ? 
                                ws.getStaffName() : currentVal + "\n" + ws.getStaffName();
                timetableModel.setValueAt(newVal, rowIndex, dayIndex);
            }
        }
    }

    // Helper class for multi-line cells
    class MultiLineCellRenderer extends JTextArea implements javax.swing.table.TableCellRenderer {
        public MultiLineCellRenderer() {
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(true);
            setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        }
      
        public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
            
            // Default Colors
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(Color.BLACK);
                setBackground(Color.WHITE);
                
                // Column 0: Header Style
                if (column == 0) {
                    setBackground(new Color(245, 245, 245));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    
                    // Highlight Today
                    Calendar cal = (Calendar) currentWeekStart.clone();
                    cal.add(Calendar.DAY_OF_YEAR, column - 1);
                    
                    Calendar today = Calendar.getInstance();
                    if (cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                        cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)) {
                        setBackground(new Color(255, 255, 224)); // Light Yellow for Today
                    }
                }
            }
            
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    private JPanel createTimekeepingPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(new EmptyBorder(0, 0, 10, 0));
        
        JButton refreshButton = createToolbarButton("Làm mới", null);
        refreshButton.addActionListener(e -> loadTimekeepingData());
        
        // Simulation buttons for testing
        JButton checkInBtn = createToolbarButton("Check In (Test)", new Color(46, 204, 113));
        JButton checkOutBtn = createToolbarButton("Check Out (Test)", new Color(241, 196, 15));
        
        checkInBtn.addActionListener(e -> simulateCheckIn());
        checkOutBtn.addActionListener(e -> simulateCheckOut());

        toolBar.add(refreshButton);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(checkInBtn);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(checkOutBtn);
        
        panel.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"ID", "Nhân viên", "Giờ vào", "Giờ ra", "Ghi chú"};
        timekeepingTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        timekeepingTable = new JTable(timekeepingTableModel);
        timekeepingTable.setRowHeight(25);
        timekeepingTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        timekeepingTable.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JScrollPane scrollPane = new JScrollPane(timekeepingTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        loadTimekeepingData();
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

    private void loadStaffData() {
        staffTableModel.setRowCount(0);
        List<Staff> staffList = staffService.getAllStaff();
        for (Staff s : staffList) {
            staffTableModel.addRow(new Object[]{
                s.getStaffId(),
                s.getFullName(),
                s.getUsername(),
                s.getEmail(),
                s.getPosition(),
                s.getSalary()
            });
        }
    }
    
    private void loadTimekeepingData() {
        timekeepingTableModel.setRowCount(0);
        List<Timekeeping> list = timekeepingService.getAllTimekeeping();
        for (Timekeeping t : list) {
            timekeepingTableModel.addRow(new Object[]{
                t.getTimekeepingId(),
                t.getStaffName(),
                t.getCheckInTime(),
                t.getCheckOutTime(),
                t.getNote()
            });
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm nhân viên", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 400);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(20);
        JTextField userField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JTextField emailField = new JTextField(20);
        JTextField posField = new JTextField(20);
        JTextField salaryField = new JTextField("0", 20);
        
        int row = 0;
        addFormRow(dialog, gbc, row++, "Họ tên:", nameField);
        addFormRow(dialog, gbc, row++, "Tài khoản:", userField);
        addFormRow(dialog, gbc, row++, "Mật khẩu:", passField);
        addFormRow(dialog, gbc, row++, "Email:", emailField);
        addFormRow(dialog, gbc, row++, "Vị trí:", posField);
        addFormRow(dialog, gbc, row++, "Lương:", salaryField);
        
        JButton saveBtn = new JButton("Lưu");
        saveBtn.addActionListener(e -> {
            try {
                Staff s = new Staff();
                s.setFullName(nameField.getText());
                s.setPosition(posField.getText());
                s.setSalary(new BigDecimal(salaryField.getText()));
                
                String username = userField.getText();
                String password = new String(passField.getPassword());
                String email = emailField.getText();
                
                if (staffService.addStaff(s, username, password, email)) {
                    JOptionPane.showMessageDialog(dialog, "Thêm thành công!");
                    dialog.dispose();
                    loadStaffData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Thêm thất bại. Kiểm tra lại thông tin.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi dữ liệu: " + ex.getMessage());
            }
        });
        
        gbc.gridx = 1; gbc.gridy = row;
        dialog.add(saveBtn, gbc);
        
        dialog.setVisible(true);
    }
    
    private void addFormRow(JDialog dialog, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0; gbc.gridy = row;
        dialog.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        dialog.add(comp, gbc);
    }

    private void showEditDialog() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để sửa.");
            return;
        }
        
        int staffId = (int) staffTableModel.getValueAt(selectedRow, 0);
        
        String currentName = (String) staffTableModel.getValueAt(selectedRow, 1);
        String currentPos = (String) staffTableModel.getValueAt(selectedRow, 4);
        BigDecimal currentSalary = (BigDecimal) staffTableModel.getValueAt(selectedRow, 5);
        
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Sửa nhân viên", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JTextField nameField = new JTextField(currentName, 20);
        JTextField posField = new JTextField(currentPos, 20);
        JTextField salaryField = new JTextField(currentSalary.toString(), 20);
        
        int row = 0;
        addFormRow(dialog, gbc, row++, "Họ tên:", nameField);
        addFormRow(dialog, gbc, row++, "Vị trí:", posField);
        addFormRow(dialog, gbc, row++, "Lương:", salaryField);
        
        JButton saveBtn = new JButton("Lưu");
        saveBtn.addActionListener(e -> {
            try {
                Staff s = new Staff();
                s.setStaffId(staffId);
                s.setFullName(nameField.getText());
                s.setPosition(posField.getText());
                s.setSalary(new BigDecimal(salaryField.getText()));
                
                if (staffService.updateStaff(s)) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                    dialog.dispose();
                    loadStaffData();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi dữ liệu: " + ex.getMessage());
            }
        });
        
        gbc.gridx = 1; gbc.gridy = row;
        dialog.add(saveBtn, gbc);
        
        dialog.setVisible(true);
    }

    // Overloaded method for double-click convenience
    private void showAssignShiftDialog(Date preSelectedDate) {
        showAssignShiftDialog(preSelectedDate, null);
    }

    private void showAssignShiftDialog(Date preSelectedDate, String preSelectedShift) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Thêm lịch làm việc", true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Staff Combo
        List<Staff> staffList = staffService.getAllStaff();
        JComboBox<Staff> staffCombo = new JComboBox<>(staffList.toArray(new Staff[0]));
        staffCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Staff) {
                    setText(((Staff) value).getFullName());
                }
                return this;
            }
        });
        
        String dateStr = (preSelectedDate != null) ? 
                         new SimpleDateFormat("yyyy-MM-dd").format(preSelectedDate) : 
                         new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        
        JTextField dateField = new JTextField(dateStr, 15);
        JComboBox<String> shiftCombo = new JComboBox<>(new String[]{"Tùy chỉnh", "Ca Sáng (07:00 - 15:00)", "Ca Chiều (15:00 - 23:00)", "Ca Đêm (23:00 - 07:00)"});
        JTextField startField = new JTextField("08:00:00", 15);
        JTextField endField = new JTextField("16:00:00", 15);
        JTextField noteField = new JTextField(15);
        
        if (preSelectedShift != null) {
            shiftCombo.setSelectedItem(preSelectedShift);
            if (preSelectedShift.contains("Ca Sáng")) {
                startField.setText("07:00:00");
                endField.setText("15:00:00");
            } else if (preSelectedShift.contains("Ca Chiều")) {
                startField.setText("15:00:00");
                endField.setText("23:00:00");
            } else if (preSelectedShift.contains("Ca Đêm")) {
                startField.setText("23:00:00");
                endField.setText("07:00:00");
            }
        }

        shiftCombo.addActionListener(e -> {
            String selected = (String) shiftCombo.getSelectedItem();
            if (selected.contains("Ca Sáng")) {
                startField.setText("07:00:00");
                endField.setText("15:00:00");
            } else if (selected.contains("Ca Chiều")) {
                startField.setText("15:00:00");
                endField.setText("23:00:00");
            } else if (selected.contains("Ca Đêm")) {
                startField.setText("23:00:00");
                endField.setText("07:00:00");
            }
        });
        
        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Nhân viên:"), gbc);
        gbc.gridx = 1; dialog.add(staffCombo, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Ngày (yyyy-MM-dd):"), gbc);
        gbc.gridx = 1; dialog.add(dateField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Chọn ca mẫu:"), gbc);
        gbc.gridx = 1; dialog.add(shiftCombo, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Giờ bắt đầu:"), gbc);
        gbc.gridx = 1; dialog.add(startField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Giờ kết thúc:"), gbc);
        gbc.gridx = 1; dialog.add(endField, gbc);
        
        row++;
        gbc.gridx = 0; gbc.gridy = row; dialog.add(new JLabel("Ghi chú:"), gbc);
        gbc.gridx = 1; dialog.add(noteField, gbc);
        
        JButton saveBtn = new JButton("Lưu");
        saveBtn.addActionListener(e -> {
            try {
                Staff selectedStaff = (Staff) staffCombo.getSelectedItem();
                Date workDate = Date.valueOf(dateField.getText());
                Time start = Time.valueOf(startField.getText());
                Time end = Time.valueOf(endField.getText());
                String note = noteField.getText();
                
                WorkSchedule ws = new WorkSchedule();
                ws.setStaffId(selectedStaff.getStaffId());
                ws.setWorkDate(workDate);
                ws.setShiftStart(start);
                ws.setShiftEnd(end);
                ws.setNote(note);
                
                try {
                    if(workScheduleService.addSchedule(ws)) {
                        JOptionPane.showMessageDialog(dialog, "Thêm lịch thành công!");
                        dialog.dispose();
                        loadWeeklySchedule();
                    } else {
                        JOptionPane.showMessageDialog(dialog, "Thêm thất bại.");
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Lỗi trùng lịch", JOptionPane.ERROR_MESSAGE);
                }
                
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi dữ liệu: " + ex.getMessage());
            }
        });
        
        row++;
        gbc.gridx = 1; gbc.gridy = row; dialog.add(saveBtn, gbc);
        
        dialog.setVisible(true);
    }

    private void deleteStaff() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên để xóa.");
            return;
        }
        
        int staffId = (int) staffTableModel.getValueAt(selectedRow, 0);
        String name = (String) staffTableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Bạn có chắc muốn xóa nhân viên " + name + "?\nTài khoản liên quan cũng sẽ bị vô hiệu hóa.", 
            "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            if (staffService.deleteStaff(staffId)) {
                loadStaffData();
                loadWeeklySchedule();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại.");
            }
        }
    }
    
    private void simulateCheckIn() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Chọn nhân viên từ danh sách để Check In (Test).");
            return;
        }
        int staffId = (int) staffTableModel.getValueAt(selectedRow, 0);
        if (timekeepingService.checkIn(staffId)) {
            JOptionPane.showMessageDialog(this, "Check In thành công!");
            loadTimekeepingData();
        } else {
            JOptionPane.showMessageDialog(this, "Check In thất bại (Có thể đã Check In rồi).");
        }
    }
    
    private void simulateCheckOut() {
        int selectedRow = staffTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Chọn nhân viên từ danh sách để Check Out (Test).");
            return;
        }
        int staffId = (int) staffTableModel.getValueAt(selectedRow, 0);
        if (timekeepingService.checkOut(staffId)) {
            JOptionPane.showMessageDialog(this, "Check Out thành công!");
            loadTimekeepingData();
        } else {
            JOptionPane.showMessageDialog(this, "Check Out thất bại (Chưa Check In?).");
        }
    }
}
