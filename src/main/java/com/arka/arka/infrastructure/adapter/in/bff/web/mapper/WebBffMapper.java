package com.arka.arka.infrastructure.adapter.in.bff.web.mapper;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.model.Order;
import com.arka.arka.domain.model.Product;
import com.arka.arka.infrastructure.adapter.in.bff.web.dto.WebDashboardDto;
import com.arka.arka.infrastructure.adapter.in.bff.web.dto.WebProductDetailDto;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper for Web BFF
 * Converts domain models to web-optimized DTOs
 */
@Component
public class WebBffMapper {
    
    /**
     * Convert Product to web-optimized detailed DTO
     */
    public WebProductDetailDto toWebProductDetailDto(Product product) {
        if (product == null) {
            return null;
        }
        
        return new WebProductDetailDto(
                product.getId(),
                product.getNombre(),
                product.getDescripcion(),
                product.getPrecioUnitario(),
                product.getStock(),
                product.getCategoria() != null ? product.getCategoria().getNombre() : null,
                product.isAvailable(),
                generateImageUrls(product.getId()), // Generate multiple image URLs
                "SKU-" + product.getId(), // Generate SKU
                generateDimensions(), // Generate placeholder dimensions
                generateWeight() // Generate placeholder weight
        );
    }
    
    /**
     * Convert aggregated data to dashboard DTO
     */
    public WebDashboardDto toWebDashboardDto(List<Product> products, List<Cart> carts, 
                                           List<Order> orders, List<Cart> abandonedCarts) {
        
        BigDecimal totalVentas = orders.stream()
                .map(Order::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        int productosConBajoStock = (int) products.stream()
                .filter(product -> product.getStock() != null && product.getStock() < 10)
                .count();
        
        String[] productosPopulares = products.stream()
                .limit(5)
                .map(Product::getNombre)
                .toArray(String[]::new);
        
        WebDashboardDto.DashboardStats stats = new WebDashboardDto.DashboardStats(
                carts.size() > 0 ? (double) orders.size() / carts.size() * 100 : 0.0, // Tasa conversión
                0.25, // Tiempo respuesta promedio (placeholder)
                abandonedCarts.size() * 2 // Notificaciones enviadas (placeholder)
        );
        
        return new WebDashboardDto(
                products.size(),
                carts.size(),
                orders.size(),
                abandonedCarts.size(),
                totalVentas,
                productosConBajoStock,
                productosPopulares,
                stats
        );
    }
    
    /**
     * Generate multiple image URLs for web gallery
     */
    private String[] generateImageUrls(Long productId) {
        return new String[] {
                "https://via.placeholder.com/800x600?text=Product+" + productId + "+Main",
                "https://via.placeholder.com/800x600?text=Product+" + productId + "+Detail1",
                "https://via.placeholder.com/800x600?text=Product+" + productId + "+Detail2"
        };
    }
    
    /**
     * Generate placeholder dimensions
     */
    private String generateDimensions() {
        return "25cm x 15cm x 10cm";
    }
    
    /**
     * Generate placeholder weight
     */
    private String generateWeight() {
        return "500g";
    }
}
