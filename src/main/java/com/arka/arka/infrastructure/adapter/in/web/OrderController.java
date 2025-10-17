package com.arka.arka.infrastructure.adapter.in.web;

import com.arka.arka.domain.model.Order;
import com.arka.arka.domain.model.Product;
import com.arka.arka.domain.model.Customer;
import com.arka.arka.domain.port.in.OrderUseCase;
import com.arka.arka.domain.port.in.ProductUseCase;
import com.arka.arka.domain.port.in.CustomerUseCase;
import com.arka.arka.infrastructure.adapter.in.web.dto.OrderDto;
import com.arka.arka.infrastructure.adapter.in.web.mapper.OrderWebMapper;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Order operations
 * Implements e-commerce order management functionality
 */
@RestController
@RequestMapping("/pedidos")
@CrossOrigin(origins = "*")
public class OrderController {

    private final OrderUseCase orderUseCase;
    private final ProductUseCase productUseCase;
    private final CustomerUseCase customerUseCase;
    private final OrderWebMapper mapper;

    public OrderController(OrderUseCase orderUseCase, 
                          ProductUseCase productUseCase,
                          CustomerUseCase customerUseCase,
                          OrderWebMapper mapper) {
        this.orderUseCase = orderUseCase;
        this.productUseCase = productUseCase;
        this.customerUseCase = customerUseCase;
        this.mapper = mapper;
    }

    /**
     * Get all orders
     */
    @GetMapping
    public ResponseEntity<List<OrderDto>> getAllOrders() {
        List<Order> orders = orderUseCase.getAllOrders();
        List<OrderDto> orderDtos = orders.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDtos);
    }

    /**
     * Get order by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOrderById(@PathVariable Long id) {
        try {
            Order order = orderUseCase.getOrderById(id);
            return ResponseEntity.ok(mapper.toDto(order));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create new order
     */
    @PostMapping
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderDto orderDto) {
        try {
            Order order = mapper.toDomain(orderDto);
            Order savedOrder = orderUseCase.createOrder(order);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapper.toDto(savedOrder));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update existing order
     */
    @PutMapping("/{id}")
    public ResponseEntity<OrderDto> updateOrder(@PathVariable Long id, @RequestBody OrderDto orderDto) {
        try {
            Order order = mapper.toDomain(orderDto);
            Order updatedOrder = orderUseCase.updateOrder(id, order);
            return ResponseEntity.ok(mapper.toDto(updatedOrder));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete order
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long id) {
        try {
            orderUseCase.deleteOrder(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get orders by customer ID
     */
    @GetMapping("/cliente/{customerId}")
    public ResponseEntity<List<OrderDto>> getOrdersByCustomer(@PathVariable Long customerId) {
        try {
            Customer customer = customerUseCase.getCustomerById(customerId);
            List<Order> orders = orderUseCase.getOrdersByCustomer(customer);
            List<OrderDto> orderDtos = orders.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get orders by product ID
     */
    @GetMapping("/producto/{productId}")
    public ResponseEntity<List<OrderDto>> getOrdersByProduct(@PathVariable Long productId) {
        try {
            Product product = productUseCase.getProductById(productId);
            List<Order> orders = orderUseCase.getOrdersByProduct(product);
            List<OrderDto> orderDtos = orders.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDtos);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get orders by date range
     */
    @GetMapping("/rango-fechas")
    public ResponseEntity<List<OrderDto>> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        try {
            List<Order> orders = orderUseCase.getOrdersByDateRange(start, end);
            List<OrderDto> orderDtos = orders.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(orderDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
