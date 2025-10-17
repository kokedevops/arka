package com.arka.arka.infrastructure.adapter.in.bff.mobile.mapper;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.model.Product;
import com.arka.arka.infrastructure.adapter.in.bff.mobile.dto.MobileCartDto;
import com.arka.arka.infrastructure.adapter.in.bff.mobile.dto.MobileProductDto;
import org.springframework.stereotype.Component;

/**
 * Mapper for Mobile BFF
 * Converts domain models to mobile-optimized DTOs
 */
@Component
public class MobileBffMapper {
    
    /**
     * Convert Product to mobile-optimized DTO
     */
    public MobileProductDto toMobileProductDto(Product product) {
        if (product == null) {
            return null;
        }
        
        return new MobileProductDto(
                product.getId(),
                product.getNombre(),
                product.getPrecioUnitario(),
                product.getCategoria() != null ? product.getCategoria().getNombre() : null,
                product.isAvailable(),
                generateImageUrl(product.getId()) // Generate placeholder image URL
        );
    }
    
    /**
     * Convert Cart to mobile-optimized DTO
     */
    public MobileCartDto toMobileCartDto(Cart cart) {
        if (cart == null) {
            return null;
        }
        
        return new MobileCartDto(
                cart.getId(),
                cart.getEstado(),
                cart.getFechaCreacion(),
                0, // In a real implementation, count items in cart
                cart.getCliente() != null ? cart.getCliente().getNombre() : null
        );
    }
    
    /**
     * Generate placeholder image URL for mobile
     */
    private String generateImageUrl(Long productId) {
        return "https://via.placeholder.com/200x200?text=Product+" + productId;
    }
}
