package com.arka.arka.infrastructure.adapter.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador principal para la página de inicio de la aplicación ARKA
 */
@RestController
@RequestMapping("/")
public class HomeController {

    /**
     * Página de inicio de la aplicación
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> home() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("application", "ARKA E-commerce Platform");
        response.put("version", "1.0.0");
        response.put("status", "Running");
        response.put("port", "8888");
        response.put("environment", "AWS");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("Health Check", "/health");
        endpoints.put("Authentication Status", "/api/auth/status");
        endpoints.put("Demo Users", "/api/auth/demo-users");
        endpoints.put("Products", "/api/products");
        endpoints.put("Categories", "/api/categories");
        endpoints.put("Customers", "/api/customers");
        endpoints.put("Orders", "/api/orders");
        endpoints.put("Cart", "/api/cart");
        endpoints.put("Analytics", "/api/analytics");
        endpoints.put("Cotización", "/api/cotizacion");
        
        response.put("availableEndpoints", endpoints);
        
        Map<String, String> authentication = new HashMap<>();
        authentication.put("method", "HTTP Basic Auth");
        authentication.put("users", "admin/admin123, user/user123, demo/demo123");
        authentication.put("example", "curl -u admin:admin123 http://localhost:8888/api/auth/status");
        
        response.put("authentication", authentication);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint de salud
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("status", "UP");
        response.put("application", "arka");
        response.put("port", "8888");
        response.put("timestamp", System.currentTimeMillis());
        
        Map<String, String> database = new HashMap<>();
        database.put("mysql", "Connected to AWS RDS");
        database.put("mongodb", "Connected to AWS DocumentDB");
        
        response.put("database", database);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Información de login
     */
    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> loginInfo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("message", "Login Information");
        response.put("endpoint", "POST /api/auth/login");
        response.put("method", "JSON POST or HTTP Basic Auth");
        
        Map<String, Object> jsonExample = new HashMap<>();
        jsonExample.put("identifier", "admin");
        jsonExample.put("password", "admin123");
        
        response.put("jsonExample", jsonExample);
        response.put("basicAuthExample", "Authorization: Basic YWRtaW46YWRtaW4xMjM=");
        
        Map<String, String> users = new HashMap<>();
        users.put("admin", "admin123 (ADMIN, USER roles)");
        users.put("user", "user123 (USER role)");
        users.put("demo", "demo123 (USER role)");
        
        response.put("availableUsers", users);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Información de la API
     */
    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("api", "ARKA E-commerce API");
        response.put("version", "v1");
        response.put("documentation", "Available endpoints listed in root path /");
        response.put("authentication", "HTTP Basic Auth required for most endpoints");
        
        return ResponseEntity.ok(response);
    }
}
