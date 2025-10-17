package com.arka.arka.domain.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entidad de dominio Solicitud
 * Representa una solicitud en el sistema ARKA
 */
@Entity
@Table(name = "solicitudes")
public class Solicitud {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String descripcion;
    
    @Column(nullable = false)
    private String estado;
    
    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;
    
    @Column(name = "cliente_id")
    private Long clienteId;
    
    @Column(name = "monto_total")
    private Double montoTotal;
    
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    // Constructores
    public Solicitud() {
        this.fechaCreacion = LocalDateTime.now();
        this.estado = "PENDIENTE";
    }

    public Solicitud(String descripcion, Long clienteId) {
        this();
        this.descripcion = descripcion;
        this.clienteId = clienteId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
        this.fechaActualizacion = LocalDateTime.now();
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public Double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(Double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    // Métodos de dominio
    public void confirmar() {
        this.estado = "CONFIRMADA";
        this.fechaActualizacion = LocalDateTime.now();
    }

    public void cancelar() {
        this.estado = "CANCELADA";
        this.fechaActualizacion = LocalDateTime.now();
    }

    public boolean esPendiente() {
        return "PENDIENTE".equals(this.estado);
    }

    public boolean estaConfirmada() {
        return "CONFIRMADA".equals(this.estado);
    }

    @Override
    public String toString() {
        return "Solicitud{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", estado='" + estado + '\'' +
                ", fechaCreacion=" + fechaCreacion +
                ", clienteId=" + clienteId +
                ", montoTotal=" + montoTotal +
                '}';
    }
}
