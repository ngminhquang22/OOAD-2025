package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.OrderDAL;
import com.gaminglounge.model.Order;
import com.gaminglounge.model.OrderDetail;

public class OrderService {
    private OrderDAL orderDAL = new OrderDAL();

    public List<Order> getAllOrders() {
        return orderDAL.getAllOrders();
    }

    public List<OrderDetail> getOrderDetails(int orderId) {
        return orderDAL.getOrderDetails(orderId);
    }

    public boolean createOrder(Order order, List<OrderDetail> details) {
        return orderDAL.createOrder(order, details);
    }
}
