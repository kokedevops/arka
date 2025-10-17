package com.arka.arka.application.service;

import com.arka.arka.domain.model.Cart;
import com.arka.arka.domain.model.Order;
import com.arka.arka.domain.port.in.CartUseCase;
import com.arka.arka.domain.port.in.OrderUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Service for e-commerce analytics and statistics
 * Provides business intelligence for the e-commerce platform
 */
@Service
public class EcommerceAnalyticsService {
    
    private static final Logger logger = LoggerFactory.getLogger(EcommerceAnalyticsService.class);
    
    private final CartUseCase cartUseCase;
    private final OrderUseCase orderUseCase;
    
    public EcommerceAnalyticsService(CartUseCase cartUseCase, OrderUseCase orderUseCase) {
        this.cartUseCase = cartUseCase;
        this.orderUseCase = orderUseCase;
    }
    
    /**
     * Get comprehensive e-commerce statistics
     */
    public Map<String, Object> getEcommerceStatistics() {
        logger.info("📊 Generating e-commerce statistics...");
        
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Cart statistics
            List<Cart> allCarts = cartUseCase.getAllCarts();
            List<Cart> abandonedCarts = cartUseCase.getAbandonedCarts();
            
            stats.put("totalCarts", allCarts.size());
            stats.put("abandonedCarts", abandonedCarts.size());
            stats.put("activeCarts", allCarts.size() - abandonedCarts.size());
            stats.put("abandonmentRate", calculateAbandonmentRate(allCarts, abandonedCarts));
            
            // Order statistics
            List<Order> allOrders = orderUseCase.getAllOrders();
            stats.put("totalOrders", allOrders.size());
            stats.put("totalRevenue", calculateTotalRevenue(allOrders));
            stats.put("averageOrderValue", calculateAverageOrderValue(allOrders));
            
            // Conversion statistics
            stats.put("conversionRate", calculateConversionRate(allCarts, allOrders));
            
            // Time-based statistics
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
            
            stats.put("cartsCreatedToday", countCartsCreatedSince(allCarts, startOfDay));
            stats.put("ordersCreatedToday", countOrdersCreatedSince(allOrders, startOfDay));
            
            logger.info("✅ E-commerce statistics generated successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error generating e-commerce statistics: {}", e.getMessage(), e);
            stats.put("error", "Failed to generate statistics: " + e.getMessage());
        }
        
        return stats;
    }
    
    /**
     * Get abandoned cart insights
     */
    public Map<String, Object> getAbandonedCartInsights() {
        logger.info("🛒 Analyzing abandoned cart insights...");
        
        Map<String, Object> insights = new HashMap<>();
        
        try {
            List<Cart> abandonedCarts = cartUseCase.getAbandonedCarts();
            
            insights.put("totalAbandonedCarts", abandonedCarts.size());
            insights.put("potentialRevenueLoss", calculatePotentialRevenueLoss(abandonedCarts));
            insights.put("averageAbandonmentTime", calculateAverageAbandonmentTime(abandonedCarts));
            insights.put("topAbandonmentReasons", getTopAbandonmentReasons());
            
            // Recovery recommendations
            insights.put("recoveryRecommendations", generateRecoveryRecommendations(abandonedCarts));
            
            logger.info("✅ Abandoned cart insights generated successfully");
            
        } catch (Exception e) {
            logger.error("❌ Error generating abandoned cart insights: {}", e.getMessage(), e);
            insights.put("error", "Failed to generate insights: " + e.getMessage());
        }
        
        return insights;
    }
    
    private double calculateAbandonmentRate(List<Cart> allCarts, List<Cart> abandonedCarts) {
        if (allCarts.isEmpty()) return 0.0;
        return (double) abandonedCarts.size() / allCarts.size() * 100;
    }
    
    private BigDecimal calculateTotalRevenue(List<Order> orders) {
        return orders.stream()
                .map(Order::getTotal)
                .filter(total -> total != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    private BigDecimal calculateAverageOrderValue(List<Order> orders) {
        if (orders.isEmpty()) return BigDecimal.ZERO;
        
        BigDecimal total = calculateTotalRevenue(orders);
        return total.divide(BigDecimal.valueOf(orders.size()), 2, RoundingMode.HALF_UP);
    }
    
    private double calculateConversionRate(List<Cart> carts, List<Order> orders) {
        if (carts.isEmpty()) return 0.0;
        return (double) orders.size() / carts.size() * 100;
    }
    
    private long countCartsCreatedSince(List<Cart> carts, LocalDateTime since) {
        return carts.stream()
                .filter(cart -> cart.getFechaCreacion() != null && cart.getFechaCreacion().isAfter(since))
                .count();
    }
    
    private long countOrdersCreatedSince(List<Order> orders, LocalDateTime since) {
        return orders.stream()
                .filter(order -> order.getFecha() != null && order.getFecha().isAfter(since))
                .count();
    }
    
    private BigDecimal calculatePotentialRevenueLoss(List<Cart> abandonedCarts) {
        // Simplified calculation - in real implementation, 
        // you would calculate based on cart contents
        return BigDecimal.valueOf(abandonedCarts.size() * 150.0); // Average cart value estimate
    }
    
    private String calculateAverageAbandonmentTime(List<Cart> abandonedCarts) {
        // Simplified - in real implementation, calculate time since last activity
        return "2.5 hours";
    }
    
    private List<String> getTopAbandonmentReasons() {
        // In real implementation, this would come from user feedback or analytics
        return List.of(
                "Unexpected shipping costs",
                "Required account creation",
                "Complex checkout process",
                "Payment issues",
                "Just browsing"
        );
    }
    
    private List<String> generateRecoveryRecommendations(List<Cart> abandonedCarts) {
        List<String> recommendations = new ArrayList<>();
        
        if (abandonedCarts.size() > 5) {
            recommendations.add("🎯 Send personalized email campaigns");
            recommendations.add("💰 Offer limited-time discounts");
            recommendations.add("📱 Implement push notifications");
        }
        
        recommendations.add("✨ Simplify checkout process");
        recommendations.add("🚚 Show transparent shipping costs early");
        recommendations.add("⏰ Create urgency with limited stock alerts");
        
        return recommendations;
    }
}
