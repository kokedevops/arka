package com.arka.arka.infrastructure.adapter.in.bff.web;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.model.Order;
import com.arka.arka.domain.model.Product;
import com.arka.arka.domain.port.in.CartUseCase;
import com.arka.arka.domain.port.in.OrderUseCase;
import com.arka.arka.domain.port.in.ProductUseCase;
import com.arka.arka.infrastructure.adapter.in.bff.web.dto.WebDashboardDto;
import com.arka.arka.infrastructure.adapter.in.bff.web.dto.WebProductDetailDto;
import com.arka.arka.infrastructure.adapter.in.bff.web.mapper.WebBffMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BFF Controller for Web Applications
 * Rich data responses optimized for desktop/web clients
 */
@RestController
@RequestMapping("/web/api")
@CrossOrigin(origins = "*")
public class WebBffController {
    
    private final ProductUseCase productUseCase;
    private final CartUseCase cartUseCase;
    private final OrderUseCase orderUseCase;
    private final WebBffMapper mapper;
    
    public WebBffController(ProductUseCase productUseCase, 
                           CartUseCase cartUseCase,
                           OrderUseCase orderUseCase,
                           WebBffMapper mapper) {
        this.productUseCase = productUseCase;
        this.cartUseCase = cartUseCase;
        this.orderUseCase = orderUseCase;
        this.mapper = mapper;
    }
    
    /**
     * Get dashboard data for web admin
     */
    @GetMapping("/dashboard")
    public ResponseEntity<WebDashboardDto> getDashboard() {
        List<Product> allProducts = productUseCase.getAllProducts();
        List<Cart> allCarts = cartUseCase.getAllCarts();
        List<Order> allOrders = orderUseCase.getAllOrders();
        List<Cart> abandonedCarts = cartUseCase.getAbandonedCarts();
        
        WebDashboardDto dashboard = mapper.toWebDashboardDto(
                allProducts, allCarts, allOrders, abandonedCarts
        );
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Get detailed product information for web
     */
    @GetMapping("/productos/{id}/detalle")
    public ResponseEntity<WebProductDetailDto> getProductDetail(@PathVariable Long id) {
        try {
            Product product = productUseCase.getProductById(id);
            WebProductDetailDto productDetail = mapper.toWebProductDetailDto(product);
            return ResponseEntity.ok(productDetail);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get all products with full web details
     */
    @GetMapping("/productos/completo")
    public ResponseEntity<List<WebProductDetailDto>> getAllProductsWeb() {
        List<Product> products = productUseCase.getAllProducts();
        List<WebProductDetailDto> webProducts = products.stream()
                .map(mapper::toWebProductDetailDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(webProducts);
    }
    
    /**
     * Get products by price range with web details
     */
    @GetMapping("/productos/rango-precio")
    public ResponseEntity<List<WebProductDetailDto>> getProductsByPriceRangeWeb(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        try {
            List<Product> products = productUseCase.getProductsByPriceRange(min, max);
            List<WebProductDetailDto> webProducts = products.stream()
                    .map(mapper::toWebProductDetailDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(webProducts);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Health check for web app
     */
    @GetMapping("/health")
    public ResponseEntity<String> webHealthCheck() {
        return ResponseEntity.ok("💻 Web BFF API is running!");
    }
}
