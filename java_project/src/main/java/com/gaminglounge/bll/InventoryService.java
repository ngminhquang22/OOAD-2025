package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.InventoryDAL;
import com.gaminglounge.dal.ProductDAL;
import com.gaminglounge.model.Category;
import com.gaminglounge.model.InventoryReceipt;
import com.gaminglounge.model.InventoryReceiptDetail;
import com.gaminglounge.model.Product;

public class InventoryService {
    private ProductDAL productDAL;
    private InventoryDAL inventoryDAL;

    public InventoryService() {
        this.productDAL = new ProductDAL();
        this.inventoryDAL = new InventoryDAL();
    }

    // Product Management
    public List<Product> getAllProducts() {
        return productDAL.getAllProducts();
    }

    public List<Category> getAllCategories() {
        return productDAL.getAllCategories();
    }

    public boolean saveProduct(Product p) {
        if (p.getProductId() == 0) {
            return productDAL.addProduct(p);
        } else {
            return productDAL.updateProduct(p);
        }
    }

    public boolean deleteProduct(int productId) {
        return productDAL.deleteProduct(productId);
    }

    // Inventory Management
    public boolean createReceipt(int staffId, String type, String note, List<InventoryReceiptDetail> details) {
        InventoryReceipt receipt = new InventoryReceipt();
        receipt.setStaffId(staffId);
        receipt.setReceiptType(type);
        receipt.setNote(note);
        receipt.setDetails(details);
        
        return inventoryDAL.createReceipt(receipt);
    }

    public List<InventoryReceipt> getAllReceipts() {
        return inventoryDAL.getAllReceipts();
    }

    public List<InventoryReceiptDetail> getReceiptDetails(int receiptId) {
        return inventoryDAL.getReceiptDetails(receiptId);
    }
}
