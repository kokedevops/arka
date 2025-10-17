package com.arka.arka.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

/**
 * Entidad de dominio Cotización
 * Representa una cotización de productos en el sistema ARKA
 */
@Entity
@Table(name = "cotizaciones")
public class Cotizacion {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "solicitud_id", nullable = false)
    private Long solicitudId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "fecha_vencimiento")
    private LocalDateTime fechaVencimiento;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCotizacion estado;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @Column(name = "descuento_aplicado")
    private Double descuentoAplicado;
    
    @Column(name = "descuento", precision = 10, scale = 2)
    private BigDecimal descuento = BigDecimal.ZERO;

    @Column(name = "impuesto_aplicado")
    private Double impuestoAplicado;
    
    @Column(name = "fecha_expiracion")
    private LocalDateTime fechaExpiracion;
    
    @Column(name = "fecha_aceptacion")
    private LocalDateTime fechaAceptacion;    // Constructores
    public Cotizacion() {
        this.fechaCreacion = LocalDateTime.now();
        this.fechaVencimiento = LocalDateTime.now().plusDays(30);
        this.estado = EstadoCotizacion.PENDIENTE;
        this.descuentoAplicado = 0.0;
        this.impuestoAplicado = 0.19; // 19% IVA
    }

    public Cotizacion(Long solicitudId, BigDecimal monto) {
        this();
        this.solicitudId = solicitudId;
        this.monto = monto;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSolicitudId() {
        return solicitudId;
    }

    public void setSolicitudId(Long solicitudId) {
        this.solicitudId = solicitudId;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaVencimiento() {
        return fechaVencimiento;
    }

    public void setFechaVencimiento(LocalDateTime fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public EstadoCotizacion getEstado() {
        return estado;
    }

    public void setEstado(EstadoCotizacion estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Double getDescuentoAplicado() {
        return descuentoAplicado;
    }

    public void setDescuentoAplicado(Double descuentoAplicado) {
        this.descuentoAplicado = descuentoAplicado;
    }

    public Double getImpuestoAplicado() {
        return impuestoAplicado;
    }

    public void setImpuestoAplicado(Double impuestoAplicado) {
        this.impuestoAplicado = impuestoAplicado;
    }
    
    public BigDecimal getDescuento() {
        return descuento;
    }

    public void setDescuento(BigDecimal descuento) {
        this.descuento = descuento;
    }
    
    public LocalDateTime getFechaExpiracion() {
        return fechaExpiracion;
    }

    public void setFechaExpiracion(LocalDateTime fechaExpiracion) {
        this.fechaExpiracion = fechaExpiracion;
    }
    
    public LocalDateTime getFechaAceptacion() {
        return fechaAceptacion;
    }

    public void setFechaAceptacion(LocalDateTime fechaAceptacion) {
        this.fechaAceptacion = fechaAceptacion;
    }

    // Métodos de dominio
    public BigDecimal calcularMontoFinal() {
        BigDecimal montoConDescuento = monto.multiply(BigDecimal.ONE.subtract(BigDecimal.valueOf(descuentoAplicado)));
        return montoConDescuento.multiply(BigDecimal.ONE.add(BigDecimal.valueOf(impuestoAplicado)));
    }

    public void aplicarDescuento(Double porcentajeDescuento) {
        this.descuentoAplicado = porcentajeDescuento;
    }

    public boolean estaVigente() {
        return EstadoCotizacion.PENDIENTE.equals(this.estado) && 
               this.fechaVencimiento.isAfter(LocalDateTime.now());
    }

    public void expirar() {
        this.estado = EstadoCotizacion.EXPIRADA;
    }

    public void aceptar() {
        this.estado = EstadoCotizacion.ACEPTADA;
    }

    @Override
    public String toString() {
        return "Cotizacion{" +
                "id=" + id +
                ", solicitudId=" + solicitudId +
                ", monto=" + monto +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaVencimiento=" + fechaVencimiento +
                ", estado='" + estado + '\'' +
                '}';
    }
}
