package com.arka.gestorsolicitudes.controller;

import com.arka.gestorsolicitudes.application.service.CalculoEnvioService;
import com.arka.gestorsolicitudes.cli.CircuitBreakerCLIUtils;
import com.arka.gestorsolicitudes.domain.model.CalculoEnvio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Controlador REST para el cálculo de envíos con Circuit Breaker
 * Aplicando seguridad basada en roles
 */
@RestController
@RequestMapping("/api/calculos")
@CrossOrigin(origins = "*", maxAge = 3600)
public class CalculoEnvioController {
    
    private static final Logger logger = LoggerFactory.getLogger(CalculoEnvioController.class);
    
    private final CalculoEnvioService calculoEnvioService;
    private final CircuitBreakerCLIUtils cliUtils;
    
    public CalculoEnvioController(CalculoEnvioService calculoEnvioService, CircuitBreakerCLIUtils cliUtils) {
        this.calculoEnvioService = calculoEnvioService;
        this.cliUtils = cliUtils;
    }
    
    /**
     * Endpoint principal para calcular envío - Todos los roles autenticados
     */
    @PostMapping("/envio")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'GESTOR', 'OPERADOR', 'USUARIO')")
    public ResponseEntity<CalculoEnvio> calcularEnvio(
            @RequestBody Map<String, Object> request,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Name") String username) {
        
        logger.info("Usuario {} (ID: {}) solicita cálculo de envío: {}", username, userId, request);
        
        String origen = (String) request.get("origen");
        String destino = (String) request.get("destino");
        BigDecimal peso = new BigDecimal(request.get("peso").toString());
        String dimensiones = (String) request.getOrDefault("dimensiones", "50x30x20");
        
        try {
            CalculoEnvio resultado = calculoEnvioService.calcularEnvio(origen, destino, peso, dimensiones);
            return ResponseEntity.ok(resultado);
        } catch (Exception ex) {
            logger.error("Error al calcular envío", ex);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint CLI para ejecutar pruebas de carga
     */
    @PostMapping("/cli/prueba-carga")
    public ResponseEntity<String> ejecutarPruebaDeCarga(@RequestBody Map<String, Object> request) {
        logger.info("Ejecutando prueba de carga CLI: {}", request);
        
        int numLlamadas = Integer.parseInt(request.getOrDefault("llamadas", "10").toString());
        String escenario = (String) request.getOrDefault("escenario", "externo");
        
        try {
            String resultado = cliUtils.ejecutarPruebaDeCarga(numLlamadas, escenario);
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(resultado);
        } catch (Exception ex) {
            logger.error("Error ejecutando prueba de carga CLI", ex);
            return ResponseEntity.internalServerError()
                .contentType(MediaType.TEXT_PLAIN)
                .body("Error ejecutando prueba de carga");
        }
    }
    
    /**
     * Endpoint CLI para generar reporte de estado
     */
    @GetMapping("/cli/reporte-estado")
    public ResponseEntity<String> generarReporteEstado() {
        try {
            String reporte = cliUtils.generarReporteEstado();
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(reporte);
        } catch (Exception ex) {
            logger.error("Error generando reporte de estado CLI", ex);
            return ResponseEntity.internalServerError()
                .contentType(MediaType.TEXT_PLAIN)
                .body("Error generando reporte");
        }
    }
    
    /**
     * Endpoint CLI para ejecutar demostración completa
     */
    @PostMapping("/cli/demostracion")
    public ResponseEntity<String> ejecutarDemostracion() {
        logger.info("Ejecutando demostración completa de Circuit Breaker");
        
        try {
            String demo = cliUtils.ejecutarDemostracion();
            return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(demo);
        } catch (Exception ex) {
            logger.error("Error ejecutando demostración CLI", ex);
            return ResponseEntity.internalServerError()
                .contentType(MediaType.TEXT_PLAIN)
                .body("Error ejecutando demostración");
        }
    }
    
    /**
     * Endpoint para obtener el estado del servicio
     */
    @GetMapping("/estado")
    public ResponseEntity<Map<String, String>> obtenerEstado() {
        try {
            String estado = calculoEnvioService.obtenerEstadoCalculos();
            return ResponseEntity.ok(Map.of("estado", estado, "servicio", "calculo-envio"));
        } catch (Exception ex) {
            logger.error("Error obteniendo estado del servicio", ex);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint para probar diferentes escenarios de Circuit Breaker
     */
    @PostMapping("/probar-circuit-breaker")
    public ResponseEntity<CalculoEnvio> probarCircuitBreaker(@RequestBody Map<String, Object> request) {
        logger.info("Probando Circuit Breaker con request: {}", request);
        
        String escenario = (String) request.get("escenario");
        String origen = (String) request.getOrDefault("origen", "Lima");
        String destino = (String) request.getOrDefault("destino", "Arequipa");
        BigDecimal peso = new BigDecimal(request.getOrDefault("peso", "1.5").toString());

        try {
            CalculoEnvio resultado = calculoEnvioService.probarCircuitBreaker(escenario, origen, destino, peso);
            return ResponseEntity.ok(resultado);
        } catch (Exception ex) {
            logger.error("Error probando Circuit Breaker", ex);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Endpoint simple para pruebas rápidas
     */
    @GetMapping("/prueba-rapida")
    public ResponseEntity<CalculoEnvio> pruebaRapida(
            @RequestParam(defaultValue = "Lima") String origen,
            @RequestParam(defaultValue = "Cusco") String destino,
            @RequestParam(defaultValue = "2.0") BigDecimal peso) {
        
        logger.info("Prueba rápida: {} -> {}, peso: {}", origen, destino, peso);
        
        try {
            CalculoEnvio resultado = calculoEnvioService.calcularEnvio(origen, destino, peso, "40x30x20");
            return ResponseEntity.ok(resultado);
        } catch (Exception ex) {
            logger.error("Error en prueba rápida de cálculo", ex);
            return ResponseEntity.internalServerError().build();
        }
    }
}
