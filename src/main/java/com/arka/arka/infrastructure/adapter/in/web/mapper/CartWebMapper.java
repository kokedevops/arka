package com.arka.arka.infrastructure.adapter.in.web.mapper;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.model.Customer;
import com.arka.arka.infrastructure.adapter.in.web.dto.CartDto;
import org.springframework.stereotype.Component;

/**
 * Mapper between Cart domain model and CartDto
 * Converts between domain and web layer representations
 */
@Component
public class CartWebMapper {

    /**
     * Convert Cart domain model to CartDto
     */
    public CartDto toDto(Cart cart) {
        if (cart == null) {
            return null;
        }

        return new CartDto(
                cart.getId(),
                cart.getCliente() != null ? cart.getCliente().getId() : null,
                cart.getCliente() != null ? cart.getCliente().getNombre() : null,
                cart.getFechaCreacion(),
                cart.getEstado()
        );
    }

    /**
     * Convert CartDto to Cart domain model
     */
    public Cart toDomain(CartDto cartDto) {
        if (cartDto == null) {
            return null;
        }

        Cart cart = new Cart();
        cart.setId(cartDto.getId());
        cart.setFechaCreacion(cartDto.getFechaCreacion());
        cart.setEstado(cartDto.getEstado());

        // For simplicity, we create a minimal Customer object
        // In a real application, you might need to fetch the full Customer from a repository
        if (cartDto.getClienteId() != null) {
            Customer customer = new Customer();
            customer.setId(cartDto.getClienteId());
            customer.setNombre(cartDto.getClienteNombre());
            cart.setCliente(customer);
        }

        return cart;
    }
}
