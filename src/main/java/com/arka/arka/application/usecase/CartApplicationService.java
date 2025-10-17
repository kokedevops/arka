package com.arka.arka.application.usecase;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.port.in.CartUseCase;
import com.arka.arka.domain.port.out.CartRepositoryPort;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application Service for Cart Use Cases
 * This class orchestrates the business logic for Cart operations
 * Following Hexagonal Architecture - Application Layer
 */
public class CartApplicationService implements CartUseCase {
    
    private final CartRepositoryPort cartRepository;

    public CartApplicationService(CartRepositoryPort cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Override
    public List<Cart> getAllCarts() {
        return cartRepository.findAll();
    }

    @Override
    public Cart getCartById(Long id) {
        return cartRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cart not found with id: " + id));
    }

    @Override
    public Cart createCart(Cart cart) {
        validateCart(cart);
        cart.setFechaCreacion(LocalDateTime.now());
        if (cart.getEstado() == null) {
            cart.setEstado("ACTIVE");
        }
        return cartRepository.save(cart);
    }

    @Override
    public Cart updateCart(Long id, Cart cart) {
        if (!cartRepository.existsById(id)) {
            throw new RuntimeException("Cart not found with id: " + id);
        }
        validateCart(cart);
        cart.setId(id);
        return cartRepository.save(cart);
    }

    @Override
    public void deleteCart(Long id) {
        if (!cartRepository.existsById(id)) {
            throw new RuntimeException("Cart not found with id: " + id);
        }
        cartRepository.deleteById(id);
    }

    @Override
    public List<Cart> getAbandonedCarts() {
        return cartRepository.findByEstado("ABANDONED");
    }

    /**
     * Domain validation logic for Cart
     */
    private void validateCart(Cart cart) {
        if (cart.getCliente() == null) {
            throw new IllegalArgumentException("Cart must have a customer");
        }
    }
}
