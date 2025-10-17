package com.arka.arka.application.service.impl;

import com.arka.arka.application.service.CotizacionService;
import com.arka.arka.domain.model.Cotizacion;
import com.arka.arka.domain.model.EstadoCotizacion;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación del servicio de cotizaciones usando programación reactiva
 * Esta implementación utiliza un repositorio en memoria para demostración
 */
@Service
public class CotizacionServiceImpl implements CotizacionService {
    
    private final Map<Long, Cotizacion> cotizaciones = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Mono<Cotizacion> generarCotizacion(Long solicitudId) {
        return Mono.fromCallable(() -> {
            Cotizacion cotizacion = new Cotizacion();
            cotizacion.setId(idGenerator.getAndIncrement());
            cotizacion.setSolicitudId(solicitudId);
            cotizacion.setMonto(new BigDecimal("1000.00")); // Monto base de ejemplo
            cotizacion.setDescuento(BigDecimal.ZERO);
            cotizacion.setEstado(EstadoCotizacion.PENDIENTE);
            cotizacion.setFechaCreacion(LocalDateTime.now());
            cotizacion.setFechaExpiracion(LocalDateTime.now().plusDays(30));
            
            cotizaciones.put(cotizacion.getId(), cotizacion);
            return cotizacion;
        })
        .doOnNext(cotizacion -> System.out.println("Cotización generada: " + cotizacion.getId()))
        .doOnError(error -> System.err.println("Error generando cotización: " + error.getMessage()));
    }

    @Override
    public Mono<Cotizacion> obtenerPorId(Long id) {
        return Mono.fromCallable(() -> cotizaciones.get(id))
                .switchIfEmpty(Mono.error(new RuntimeException("Cotización no encontrada: " + id)))
                .doOnNext(cotizacion -> System.out.println("Cotización encontrada: " + cotizacion.getId()));
    }

    @Override
    public Flux<Cotizacion> obtenerTodas() {
        return Flux.fromIterable(cotizaciones.values())
                .doOnNext(cotizacion -> System.out.println("Procesando cotización: " + cotizacion.getId()))
                .doOnComplete(() -> System.out.println("Todas las cotizaciones procesadas"));
    }

    @Override
    public Flux<Cotizacion> obtenerPorSolicitud(Long solicitudId) {
        return Flux.fromIterable(cotizaciones.values())
                .filter(cotizacion -> cotizacion.getSolicitudId().equals(solicitudId))
                .doOnNext(cotizacion -> System.out.println("Cotización de solicitud " + solicitudId + ": " + cotizacion.getId()));
    }

    @Override
    public Mono<Cotizacion> actualizar(Long id, Cotizacion cotizacionActualizada) {
        return obtenerPorId(id)
                .map(cotizacion -> {
                    cotizacion.setMonto(cotizacionActualizada.getMonto());
                    cotizacion.setDescuento(cotizacionActualizada.getDescuento());
                    cotizacion.setObservaciones(cotizacionActualizada.getObservaciones());
                    cotizaciones.put(id, cotizacion);
                    return cotizacion;
                })
                .doOnNext(cotizacion -> System.out.println("Cotización actualizada: " + cotizacion.getId()));
    }

    @Override
    public Mono<Cotizacion> aplicarDescuento(Long id, Double porcentajeDescuento) {
        return obtenerPorId(id)
                .map(cotizacion -> {
                    if (porcentajeDescuento < 0 || porcentajeDescuento > 100) {
                        throw new IllegalArgumentException("Porcentaje de descuento debe estar entre 0 y 100");
                    }
                    BigDecimal descuento = cotizacion.getMonto()
                            .multiply(new BigDecimal(porcentajeDescuento))
                            .divide(new BigDecimal("100"));
                    cotizacion.setDescuento(descuento);
                    cotizaciones.put(id, cotizacion);
                    return cotizacion;
                })
                .doOnNext(cotizacion -> System.out.println("Descuento aplicado a cotización: " + cotizacion.getId()));
    }

    @Override
    public Mono<Cotizacion> aceptar(Long id) {
        return obtenerPorId(id)
                .map(cotizacion -> {
                    if (cotizacion.getEstado() != EstadoCotizacion.PENDIENTE) {
                        throw new IllegalStateException("Solo se pueden aceptar cotizaciones pendientes");
                    }
                    if (cotizacion.getFechaExpiracion().isBefore(LocalDateTime.now())) {
                        throw new IllegalStateException("No se puede aceptar una cotización expirada");
                    }
                    cotizacion.setEstado(EstadoCotizacion.ACEPTADA);
                    cotizacion.setFechaAceptacion(LocalDateTime.now());
                    cotizaciones.put(id, cotizacion);
                    return cotizacion;
                })
                .doOnNext(cotizacion -> System.out.println("Cotización aceptada: " + cotizacion.getId()));
    }

    @Override
    public Mono<Cotizacion> expirar(Long id) {
        return obtenerPorId(id)
                .map(cotizacion -> {
                    cotizacion.setEstado(EstadoCotizacion.EXPIRADA);
                    cotizaciones.put(id, cotizacion);
                    return cotizacion;
                })
                .doOnNext(cotizacion -> System.out.println("Cotización expirada: " + cotizacion.getId()));
    }
}
