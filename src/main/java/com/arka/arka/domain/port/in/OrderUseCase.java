package com.arka.arka.domain.port.in;

import com.arka.arka.domain.model.Order;
import com.arka.arka.domain.model.Product;
import com.arka.arka.domain.model.Customer;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderUseCase {
    List<Order> getAllOrders();
    Order getOrderById(Long id);
    Order createOrder(Order order);
    Order updateOrder(Long id, Order order);
    void deleteOrder(Long id);
    List<Order> getOrdersByProduct(Product product);
    List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end);
    List<Order> getOrdersByCustomer(Customer customer);
}
