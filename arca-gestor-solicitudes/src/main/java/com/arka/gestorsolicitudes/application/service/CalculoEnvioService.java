package com.arka.gestorsolicitudes.application.service;

import com.arka.gestorsolicitudes.domain.model.CalculoEnvio;
import com.arka.gestorsolicitudes.infrastructure.adapter.external.ProveedorEnvioExternoService;
import com.arka.gestorsolicitudes.messaging.EnvioEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Servicio de aplicación para gestionar el cálculo de envíos
 * Orquesta las llamadas a diferentes proveedores con fallbacks
 */
@Service
public class CalculoEnvioService {
    
    private static final Logger logger = LoggerFactory.getLogger(CalculoEnvioService.class);
    
    private static final String DIMENSIONES_DEFAULT = "100x50x30";

    private final ProveedorEnvioExternoService proveedorExternoService;
    private final EnvioEventPublisher eventPublisher;

    public CalculoEnvioService(ProveedorEnvioExternoService proveedorExternoService,
                               EnvioEventPublisher eventPublisher) {
        this.proveedorExternoService = proveedorExternoService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Calcula el envío intentando primero el proveedor externo
     * Si falla, usa el servicio interno simulado
     */
    public CalculoEnvio calcularEnvio(String origen, String destino, BigDecimal peso, String dimensiones) {
        logger.info("Iniciando cálculo de envío de {} a {} con peso {}", origen, destino, peso);

        try {
            CalculoEnvio calculo = proveedorExternoService.calcularEnvioProveedorExterno(origen, destino, peso);
            return procesarResultado(origen, destino, peso, dimensiones, calculo);
        } catch (RuntimeException ex) {
            logger.warn("Proveedor externo falló, intentando con servicio interno: {}", ex.getMessage());
            eventPublisher.publicarEventoCircuitBreakerActivado("proveedor-externo-service", ex.getMessage());

            CalculoEnvio fallback = proveedorExternoService.calcularEnvioSimulado(origen, destino, peso);
            if (fallback.getMensajeError() == null) {
                fallback.setMensajeError(ex.getMessage());
            }
            return procesarResultado(origen, destino, peso, dimensiones, fallback);
        }
    }
    
    /**
     * Obtiene el estado del cálculo de envío
     */
    public String obtenerEstadoCalculos() {
        return "Servicio de cálculo de envíos operativo";
    }
    
    /**
     * Método para probar diferentes escenarios de fallback
     */
    public CalculoEnvio probarCircuitBreaker(String escenario, String origen, String destino, BigDecimal peso) {
        logger.info("Probando Circuit Breaker con escenario: {}", escenario);

        return switch (escenario.toLowerCase()) {
            case "externo" -> procesarResultado(origen, destino, peso, DIMENSIONES_DEFAULT,
                proveedorExternoService.calcularEnvioProveedorExterno(origen, destino, peso));
            case "interno" -> procesarResultado(origen, destino, peso, DIMENSIONES_DEFAULT,
                proveedorExternoService.calcularEnvioSimulado(origen, destino, peso));
            case "completo" -> calcularEnvio(origen, destino, peso, DIMENSIONES_DEFAULT);
            default -> {
                CalculoEnvio resultado = CalculoEnvio.error("Escenario no válido: " + escenario);
                resultado.setOrigen(origen);
                resultado.setDestino(destino);
                resultado.setPeso(peso);
                resultado.setDimensiones(DIMENSIONES_DEFAULT);
                yield resultado;
            }
        };
    }

    private CalculoEnvio procesarResultado(String origen, String destino, BigDecimal peso, String dimensiones, CalculoEnvio calculo) {
        calculo.setOrigen(origen);
        calculo.setDestino(destino);
        calculo.setPeso(peso);
        calculo.setDimensiones(dimensiones);

        logger.info("Cálculo de envío completado. Estado: {}, Proveedor: {}",
            calculo.getEstado(), calculo.getProveedorUtilizado());

        eventPublisher.publicarEventoCalculoCompletado(calculo);
        logger.debug("AWS integration disabled for testing - skipping S3 backup and SQS notification");

        return calculo;
    }
}
