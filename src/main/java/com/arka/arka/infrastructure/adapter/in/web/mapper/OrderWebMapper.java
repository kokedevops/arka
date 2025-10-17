package com.arka.arka.infrastructure.adapter.in.web.mapper;

import com.arka.arka.domain.model.Order;
import com.arka.arka.domain.model.Customer;
import com.arka.arka.domain.model.Product;
import com.arka.arka.infrastructure.adapter.in.web.dto.OrderDto;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Mapper between Order domain model and OrderDto
 * Converts between domain and web layer representations
 */
@Component
public class OrderWebMapper {

    /**
     * Convert Order domain model to OrderDto
     */
    public OrderDto toDto(Order order) {
        if (order == null) {
            return null;
        }

        List<Long> productIds = null;
        List<String> productNames = null;
        
        if (order.getProductos() != null) {
            productIds = order.getProductos().stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());
            
            productNames = order.getProductos().stream()
                    .map(Product::getNombre)
                    .collect(Collectors.toList());
        }

        return new OrderDto(
                order.getId(),
                order.getCliente() != null ? order.getCliente().getId() : null,
                order.getCliente() != null ? order.getCliente().getNombre() : null,
                productIds,
                productNames,
                order.getTotal(),
                order.getFecha(),
                "PENDING" // Default status, could be extended with actual status field
        );
    }

    /**
     * Convert OrderDto to Order domain model
     */
    public Order toDomain(OrderDto orderDto) {
        if (orderDto == null) {
            return null;
        }

        Order order = new Order();
        order.setId(orderDto.getId());
        order.setFecha(orderDto.getFechaPedido());
        order.setTotal(orderDto.getTotal());

        // For simplicity, we create a minimal Customer object
        // In a real application, you might need to fetch the full Customer from a repository
        if (orderDto.getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setId(orderDto.getCustomerId());
            customer.setNombre(orderDto.getCustomerName());
            order.setCliente(customer);
        }

        // For simplicity, we create minimal Product objects
        // In a real application, you might need to fetch the full Products from a repository
        if (orderDto.getProductIds() != null && !orderDto.getProductIds().isEmpty()) {
            Set<Product> products = new HashSet<>();
            for (int i = 0; i < orderDto.getProductIds().size(); i++) {
                Product product = new Product();
                product.setId(orderDto.getProductIds().get(i));
                if (orderDto.getProductNames() != null && i < orderDto.getProductNames().size()) {
                    product.setNombre(orderDto.getProductNames().get(i));
                }
                products.add(product);
            }
            order.setProductos(products);
        }

        return order;
    }
}
