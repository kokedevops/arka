package com.arka.arka.infrastructure.adapter.in.web;

import com.arka.arka.application.service.EcommerceAnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST Controller for E-commerce Analytics
 * Provides business intelligence endpoints for the dashboard
 */
@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "*")
public class AnalyticsController {
    
    private final EcommerceAnalyticsService analyticsService;
    
    public AnalyticsController(EcommerceAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }
    
    /**
     * Get comprehensive e-commerce statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getEcommerceStatistics() {
        Map<String, Object> statistics = analyticsService.getEcommerceStatistics();
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * Get abandoned cart insights
     */
    @GetMapping("/abandoned-carts")
    public ResponseEntity<Map<String, Object>> getAbandonedCartInsights() {
        Map<String, Object> insights = analyticsService.getAbandonedCartInsights();
        return ResponseEntity.ok(insights);
    }
    
    /**
     * Health check for analytics
     */
    @GetMapping("/health")
    public ResponseEntity<String> analyticsHealthCheck() {
        return ResponseEntity.ok("📊 Analytics API is running!");
    }
}
