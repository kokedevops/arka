package com.arka.arka.application.usecase;

import com.arka.arka.domain.model.Order;
import com.arka.arka.domain.model.Product;
import com.arka.arka.domain.model.Customer;
import com.arka.arka.domain.port.in.OrderUseCase;
import com.arka.arka.domain.port.out.OrderRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application Service for Order Use Cases
 * This class orchestrates the business logic for Order operations
 * Following Hexagonal Architecture - Application Layer
 */
public class OrderApplicationService implements OrderUseCase {
    
    private final OrderRepositoryPort orderRepository;

    public OrderApplicationService(OrderRepositoryPort orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    @Override
    public Order createOrder(Order order) {
        validateOrder(order);
        order.setFecha(LocalDateTime.now());
        order.setTotal(order.calculateTotal());
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrder(Long id, Order order) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        validateOrder(order);
        order.setId(id);
        order.setTotal(order.calculateTotal());
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Long id) {
        if (!orderRepository.existsById(id)) {
            throw new RuntimeException("Order not found with id: " + id);
        }
        orderRepository.deleteById(id);
    }

    @Override
    public List<Order> getOrdersByProduct(Product product) {
        return orderRepository.findByProductosContaining(product);
    }

    @Override
    public List<Order> getOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        return orderRepository.findByFechaBetween(start, end);
    }

    @Override
    public List<Order> getOrdersByCustomer(Customer customer) {
        return orderRepository.findByCliente(customer);
    }

    /**
     * Domain validation logic for Order
     */
    private void validateOrder(Order order) {
        if (!order.isValidOrder()) {
            throw new IllegalArgumentException("Order is not valid");
        }
        if (order.getCliente() == null) {
            throw new IllegalArgumentException("Order must have a customer");
        }
        if (order.getProductos() == null || order.getProductos().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one product");
        }
    }
}
