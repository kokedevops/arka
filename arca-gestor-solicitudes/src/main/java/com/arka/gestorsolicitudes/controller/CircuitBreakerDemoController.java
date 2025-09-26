package com.arka.gestorsolicitudes.controller;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para monitorear y controlar el estado de los Circuit Breakers
 */
@RestController
@RequestMapping("/api/circuit-breaker")
public class CircuitBreakerDemoController {
    
    private static final String KEY_MENSAJE = "mensaje";
    private static final String KEY_NOMBRE = "nombre";
    private static final String KEY_ESTADO_ACTUAL = "estadoActual";
    private static final String KEY_ERROR = "error";
    private static final String KEY_DETALLE = "detalle";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    public CircuitBreakerDemoController(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }
    
    /**
     * Obtiene el estado de todos los Circuit Breakers
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, Object>> obtenerEstadoCircuitBreakers() {
        Map<String, Object> estados = new HashMap<>();
        
        circuitBreakerRegistry.getAllCircuitBreakers().forEach(cb -> {
            Map<String, Object> info = new HashMap<>();
            info.put("estado", cb.getState().toString());
            info.put("metricas", cb.getMetrics());
            info.put("configuracion", Map.of(
                "failureRateThreshold", cb.getCircuitBreakerConfig().getFailureRateThreshold(),
                "slidingWindowSize", cb.getCircuitBreakerConfig().getSlidingWindowSize(),
                "minimumNumberOfCalls", cb.getCircuitBreakerConfig().getMinimumNumberOfCalls()
            ));
            estados.put(cb.getName(), info);
        });
        
    return ResponseEntity.ok(estados);
    }
    
    /**
     * Obtiene el estado de un Circuit Breaker específico
     */
    @GetMapping("/estado/{nombre}")
    public ResponseEntity<Map<String, Object>> obtenerEstadoCircuitBreaker(@PathVariable String nombre) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(nombre);
            
            Map<String, Object> info = new HashMap<>();
            info.put(KEY_NOMBRE, nombre);
            info.put("estado", circuitBreaker.getState().toString());
            info.put("metricas", circuitBreaker.getMetrics());
            info.put("configuracion", Map.of(
                "failureRateThreshold", circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold(),
                "slidingWindowSize", circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize(),
                "minimumNumberOfCalls", circuitBreaker.getCircuitBreakerConfig().getMinimumNumberOfCalls()
            ));
            
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Fuerza la transición del Circuit Breaker a estado OPEN (para pruebas)
     */
    @PostMapping("/forzar-apertura/{nombre}")
    public ResponseEntity<Map<String, String>> forzarApertura(@PathVariable String nombre) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(nombre);
            circuitBreaker.transitionToOpenState();
            
            return ResponseEntity.ok(Map.of(
                KEY_MENSAJE, "Circuit Breaker forzado a estado OPEN",
                KEY_NOMBRE, nombre,
                KEY_ESTADO_ACTUAL, circuitBreaker.getState().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of(KEY_ERROR, "No se pudo forzar apertura", KEY_DETALLE, e.getMessage()));
        }
    }
    
    /**
     * Fuerza la transición del Circuit Breaker a estado CLOSED (para pruebas)
     */
    @PostMapping("/forzar-cierre/{nombre}")
    public ResponseEntity<Map<String, String>> forzarCierre(@PathVariable String nombre) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(nombre);
            circuitBreaker.transitionToClosedState();
            
            return ResponseEntity.ok(Map.of(
                KEY_MENSAJE, "Circuit Breaker forzado a estado CLOSED",
                KEY_NOMBRE, nombre,
                KEY_ESTADO_ACTUAL, circuitBreaker.getState().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of(KEY_ERROR, "No se pudo forzar cierre", KEY_DETALLE, e.getMessage()));
        }
    }
    
    /**
     * Reinicia las métricas del Circuit Breaker
     */
    @PostMapping("/reiniciar-metricas/{nombre}")
    public ResponseEntity<Map<String, String>> reiniciarMetricas(@PathVariable String nombre) {
        try {
            CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(nombre);
            circuitBreaker.reset();
            
            return ResponseEntity.ok(Map.of(
                KEY_MENSAJE, "Métricas del Circuit Breaker reiniciadas",
                KEY_NOMBRE, nombre,
                KEY_ESTADO_ACTUAL, circuitBreaker.getState().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of(KEY_ERROR, "No se pudieron reiniciar las métricas", KEY_DETALLE, e.getMessage()));
        }
    }
}
