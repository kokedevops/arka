package com.arka.arka.infrastructure.adapter.in.bff.mobile;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.model.Product;
import com.arka.arka.domain.port.in.CartUseCase;
import com.arka.arka.domain.port.in.ProductUseCase;
import com.arka.arka.infrastructure.adapter.in.bff.mobile.dto.MobileCartDto;
import com.arka.arka.infrastructure.adapter.in.bff.mobile.dto.MobileProductDto;
import com.arka.arka.infrastructure.adapter.in.bff.mobile.mapper.MobileBffMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * BFF Controller for Mobile Applications
 * Optimized responses for mobile clients with reduced data payload
 */
@RestController
@RequestMapping("/mobile/api")
@CrossOrigin(origins = "*")
public class MobileBffController {
    
    private final ProductUseCase productUseCase;
    private final CartUseCase cartUseCase;
    private final MobileBffMapper mapper;
    
    public MobileBffController(ProductUseCase productUseCase, 
                              CartUseCase cartUseCase,
                              MobileBffMapper mapper) {
        this.productUseCase = productUseCase;
        this.cartUseCase = cartUseCase;
        this.mapper = mapper;
    }
    
    /**
     * Get featured products for mobile home screen
     * Limited to essential data only
     */
    @GetMapping("/productos/destacados")
    public ResponseEntity<List<MobileProductDto>> getFeaturedProducts() {
        List<Product> products = productUseCase.getAllProducts();
        List<MobileProductDto> mobileProducts = products.stream()
                .limit(10) // Limit for mobile performance
                .map(mapper::toMobileProductDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mobileProducts);
    }
    
    /**
     * Search products with mobile-optimized response
     */
    @GetMapping("/productos/buscar")
    public ResponseEntity<List<MobileProductDto>> searchProducts(@RequestParam String query) {
        List<Product> products = productUseCase.searchProductsByName(query);
        List<MobileProductDto> mobileProducts = products.stream()
                .limit(20) // Limit for mobile
                .map(mapper::toMobileProductDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(mobileProducts);
    }
    
    /**
     * Get cart summary for mobile
     */
    @GetMapping("/carrito/{cartId}/resumen")
    public ResponseEntity<MobileCartDto> getCartSummary(@PathVariable Long cartId) {
        try {
            Cart cart = cartUseCase.getCartById(cartId);
            MobileCartDto mobileCart = mapper.toMobileCartDto(cart);
            return ResponseEntity.ok(mobileCart);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Quick product info for mobile scanning/QR codes
     */
    @GetMapping("/productos/{id}/quick")
    public ResponseEntity<MobileProductDto> getQuickProductInfo(@PathVariable Long id) {
        try {
            Product product = productUseCase.getProductById(id);
            MobileProductDto mobileProduct = mapper.toMobileProductDto(product);
            return ResponseEntity.ok(mobileProduct);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Health check for mobile app
     */
    @GetMapping("/health")
    public ResponseEntity<String> mobileHealthCheck() {
        return ResponseEntity.ok("📱 Mobile BFF API is running!");
    }
}
