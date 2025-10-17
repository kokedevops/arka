package com.arka.arka.infrastructure.adapter.web;

import com.arka.arka.application.service.CotizacionService;
import com.arka.arka.domain.model.Cotizacion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST reactivo para gestión de cotizaciones
 * Implementa endpoints reactivos usando WebFlux
 */
@RestController
@RequestMapping("/api/cotizaciones")
@CrossOrigin(origins = "*")
public class CotizacionController {

    @Autowired
    private CotizacionService cotizacionService;

    /**
     * Generar nueva cotización para una solicitud
     */
    @PostMapping("/generar/{solicitudId}")
    public Mono<ResponseEntity<Cotizacion>> generarCotizacion(@PathVariable Long solicitudId) {
        return cotizacionService.generarCotizacion(solicitudId)
                .map(cotizacion -> ResponseEntity.ok(cotizacion))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Obtener cotización por ID
     */
    @GetMapping("/{id}")
    public Mono<ResponseEntity<Cotizacion>> obtenerCotizacion(@PathVariable Long id) {
        return cotizacionService.obtenerPorId(id)
                .map(cotizacion -> ResponseEntity.ok(cotizacion))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    /**
     * Obtener todas las cotizaciones (stream reactivo)
     */
    @GetMapping(value = "/stream", produces = "application/stream+json")
    public Flux<Cotizacion> obtenerCotizacionesStream() {
        return cotizacionService.obtenerTodas();
    }

    /**
     * Obtener cotizaciones por solicitud
     */
    @GetMapping("/solicitud/{solicitudId}")
    public Flux<Cotizacion> obtenerCotizacionesPorSolicitud(@PathVariable Long solicitudId) {
        return cotizacionService.obtenerPorSolicitud(solicitudId);
    }

    /**
     * Aplicar descuento a cotización
     */
    @PutMapping("/{id}/descuento")
    public Mono<ResponseEntity<Cotizacion>> aplicarDescuento(
            @PathVariable Long id, 
            @RequestParam Double porcentaje) {
        return cotizacionService.aplicarDescuento(id, porcentaje)
                .map(cotizacion -> ResponseEntity.ok(cotizacion))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Aceptar cotización
     */
    @PutMapping("/{id}/aceptar")
    public Mono<ResponseEntity<Cotizacion>> aceptarCotizacion(@PathVariable Long id) {
        return cotizacionService.aceptar(id)
                .map(cotizacion -> ResponseEntity.ok(cotizacion))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    /**
     * Expirar cotización
     */
    @PutMapping("/{id}/expirar")
    public Mono<ResponseEntity<Cotizacion>> expirarCotizacion(@PathVariable Long id) {
        return cotizacionService.expirar(id)
                .map(cotizacion -> ResponseEntity.ok(cotizacion))
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
