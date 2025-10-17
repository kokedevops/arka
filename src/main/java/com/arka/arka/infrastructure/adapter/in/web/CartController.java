package com.arka.arka.infrastructure.adapter.in.web;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.port.in.CartUseCase;
import com.arka.arka.infrastructure.adapter.in.web.dto.CartDto;
import com.arka.arka.infrastructure.adapter.in.web.mapper.CartWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for Cart operations
 * Implements e-commerce shopping cart functionality
 */
@RestController
@RequestMapping("/carritos")
@CrossOrigin(origins = "*")
public class CartController {

    private final CartUseCase cartUseCase;
    private final CartWebMapper mapper;

    public CartController(CartUseCase cartUseCase, CartWebMapper mapper) {
        this.cartUseCase = cartUseCase;
        this.mapper = mapper;
    }

    /**
     * Get all shopping carts
     */
    @GetMapping
    public ResponseEntity<List<CartDto>> getAllCarts() {
        List<Cart> carts = cartUseCase.getAllCarts();
        List<CartDto> cartDtos = carts.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cartDtos);
    }

    /**
     * Get cart by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CartDto> getCartById(@PathVariable Long id) {
        try {
            Cart cart = cartUseCase.getCartById(id);
            return ResponseEntity.ok(mapper.toDto(cart));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create new shopping cart
     */
    @PostMapping
    public ResponseEntity<CartDto> createCart(@RequestBody CartDto cartDto) {
        try {
            Cart cart = mapper.toDomain(cartDto);
            Cart savedCart = cartUseCase.createCart(cart);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(mapper.toDto(savedCart));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Update existing cart
     */
    @PutMapping("/{id}")
    public ResponseEntity<CartDto> updateCart(@PathVariable Long id, @RequestBody CartDto cartDto) {
        try {
            Cart cart = mapper.toDomain(cartDto);
            Cart updatedCart = cartUseCase.updateCart(id, cart);
            return ResponseEntity.ok(mapper.toDto(updatedCart));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete cart
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCart(@PathVariable Long id) {
        try {
            cartUseCase.deleteCart(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get abandoned carts - Key e-commerce feature
     */
    @GetMapping("/abandonados")
    public ResponseEntity<List<CartDto>> getAbandonedCarts() {
        List<Cart> abandonedCarts = cartUseCase.getAbandonedCarts();
        List<CartDto> cartDtos = abandonedCarts.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cartDtos);
    }

    /**
     * Activate cart
     */
    @PutMapping("/{id}/activar")
    public ResponseEntity<CartDto> activateCart(@PathVariable Long id) {
        try {
            Cart cart = cartUseCase.getCartById(id);
            cart.activate();
            Cart updatedCart = cartUseCase.updateCart(id, cart);
            return ResponseEntity.ok(mapper.toDto(updatedCart));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Abandon cart
     */
    @PutMapping("/{id}/abandonar")
    public ResponseEntity<CartDto> abandonCart(@PathVariable Long id) {
        try {
            Cart cart = cartUseCase.getCartById(id);
            cart.abandon();
            Cart updatedCart = cartUseCase.updateCart(id, cart);
            return ResponseEntity.ok(mapper.toDto(updatedCart));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
