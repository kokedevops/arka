package com.arka.arka.application.service;

import com.arka.arka.domain.model.Cotizacion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interfaz de servicio para gestión de cotizaciones
 * Define las operaciones reactivas para el dominio de cotizaciones
 */
public interface CotizacionService {
    
    /**
     * Generar cotización para una solicitud
     */
    Mono<Cotizacion> generarCotizacion(Long solicitudId);
    
    /**
     * Obtener cotización por ID
     */
    Mono<Cotizacion> obtenerPorId(Long id);
    
    /**
     * Obtener todas las cotizaciones
     */
    Flux<Cotizacion> obtenerTodas();
    
    /**
     * Obtener cotizaciones por solicitud
     */
    Flux<Cotizacion> obtenerPorSolicitud(Long solicitudId);
    
    /**
     * Actualizar cotización
     */
    Mono<Cotizacion> actualizar(Long id, Cotizacion cotizacion);
    
    /**
     * Aplicar descuento a cotización
     */
    Mono<Cotizacion> aplicarDescuento(Long id, Double porcentajeDescuento);
    
    /**
     * Aceptar cotización
     */
    Mono<Cotizacion> aceptar(Long id);
    
    /**
     * Expirar cotización
     */
    Mono<Cotizacion> expirar(Long id);
}
