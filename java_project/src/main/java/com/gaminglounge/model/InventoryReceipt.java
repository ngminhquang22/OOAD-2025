package com.gaminglounge.model;

import java.sql.Timestamp;
import java.util.List;

public class InventoryReceipt {
    private int receiptId;
    private int staffId;
    private String staffName; // Joined
    private Timestamp receiptDate;
    private String receiptType; // 'Import', 'Export'
    private String note;
    private List<InventoryReceiptDetail> details;

    public InventoryReceipt() {}

    public int getReceiptId() { return receiptId; }
    public void setReceiptId(int receiptId) { this.receiptId = receiptId; }

    public int getStaffId() { return staffId; }
    public void setStaffId(int staffId) { this.staffId = staffId; }

    public String getStaffName() { return staffName; }
    public void setStaffName(String staffName) { this.staffName = staffName; }

    public Timestamp getReceiptDate() { return receiptDate; }
    public void setReceiptDate(Timestamp receiptDate) { this.receiptDate = receiptDate; }

    public String getReceiptType() { return receiptType; }
    public void setReceiptType(String receiptType) { this.receiptType = receiptType; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<InventoryReceiptDetail> getDetails() { return details; }
    public void setDetails(List<InventoryReceiptDetail> details) { this.details = details; }
}
