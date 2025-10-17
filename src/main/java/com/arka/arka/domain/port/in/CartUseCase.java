package com.arka.arka.domain.port.in;

import com.arka.arka.domain.model.Cart;
import java.util.List;

public interface CartUseCase {
    List<Cart> getAllCarts();
    Cart getCartById(Long id);
    Cart createCart(Cart cart);
    Cart updateCart(Long id, Cart cart);
    void deleteCart(Long id);
    List<Cart> getAbandonedCarts();
}
