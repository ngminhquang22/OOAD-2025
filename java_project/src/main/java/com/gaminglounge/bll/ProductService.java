package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.ProductDAL;
import com.gaminglounge.model.Category;
import com.gaminglounge.model.Product;

public class ProductService {
    private ProductDAL productDAL;

    public ProductService() {
        this.productDAL = new ProductDAL();
    }

    // Lấy tất cả sản phẩm (Dùng cho cả Menu khách hàng và Quản lý kho)
    public List<Product> getAllProducts() {
        return productDAL.getAllProducts();
    }

    // Lấy danh mục (Đồ ăn, Đồ uống...)
    public List<Category> getAllCategories() {
        return productDAL.getAllCategories();
    }

    // Thêm sản phẩm mới (Dùng cho Admin)
    public boolean addProduct(Product p) {
        if (p.getProductName() == null || p.getProductName().isEmpty()) {
            return false;
        }
        if (p.getPrice() == null || p.getPrice().doubleValue() < 0) {
            return false;
        }
        return productDAL.addProduct(p);
    }

    // Cập nhật sản phẩm (Dùng cho Admin)
    public boolean updateProduct(Product p) {
        return productDAL.updateProduct(p);
    }

    // Xóa sản phẩm (Dùng cho Admin)
    public boolean deleteProduct(int productId) {
        return productDAL.deleteProduct(productId);
    }
}