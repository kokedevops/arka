package com.arka.gateway.config;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Filtro global de autenticación JWT para el API Gateway
 * VERSIÓN SIMPLIFICADA - Sin dependencias externas
 * TODO: Integrar con servicio JWT cuando esté disponible
 */
// @Component - Desactivado temporalmente
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    
    // Rutas que no requieren autenticación
    private static final List<String> OPEN_API_ENDPOINTS = List.of(
            "/auth/register",
            "/auth/login",
            "/auth/refresh",
            "/actuator/health",
            "/actuator/info",
            "/eureka",
            "/cotizador", // Temporalmente abierto para testing
            "/gestor"     // Temporalmente abierto para testing
    );
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        // Por ahora, permitir todas las rutas
        // TODO: Implementar validación JWT cuando el servicio esté disponible
        
        // Log de la request (para debugging)
        System.out.println("API Gateway - Request: " + request.getMethod() + " " + request.getPath().value());
        
        // Continuar con la cadena de filtros sin validación
        return chain.filter(exchange);
    }
    
    /**
     * Verifica si un endpoint está en la lista de endpoints abiertos
     */
    private boolean isOpenEndpoint(String path) {
        return OPEN_API_ENDPOINTS.stream()
                .anyMatch(openPath -> path.contains(openPath));
    }
    
    /**
     * Maneja errores de autenticación
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().add("Content-Type", "application/json");
        
        String errorResponse = String.format(
                "{\"error\":\"%s\",\"message\":\"%s\",\"status\":%d}", 
                httpStatus.getReasonPhrase(), 
                err, 
                httpStatus.value()
        );
        
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse().bufferFactory().wrap(errorResponse.getBytes()))
        );
    }
    
    @Override
    public int getOrder() {
        return -1; // Ejecutar antes que otros filtros
    }
}
