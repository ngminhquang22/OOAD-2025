package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.OrderDAL;
import com.gaminglounge.model.Order;
import com.gaminglounge.model.OrderDetail;

public class OrderService {
    // Khởi tạo DAL
    private OrderDAL orderDAL = new OrderDAL();

    public List<Order> getAllOrders() {
        return orderDAL.getAllOrders();
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        return orderDAL.getOrderDetails(orderId);
    }

    // --- Cập nhật logic tại đây ---
    public boolean createOrder(Order order, List<OrderDetail> details) {
        // 1. Kiểm tra dữ liệu đầu vào (Validation)
        if (order == null) {
            return false;
        }
        if (details == null || details.isEmpty()) {
            // Không thể tạo đơn hàng mà không có món nào
            return false;
        }
        if (order.getCustomerId() <= 0) {
            // Phải có khách hàng
            return false;
        }

        // 2. Nếu ổn thì mới gọi xuống Database
        return orderDAL.createOrder(order, details);
    }
}