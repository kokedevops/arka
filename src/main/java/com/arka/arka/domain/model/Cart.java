package com.arka.arka.domain.model;

import java.time.LocalDateTime;

public class Cart {
    private Long id;
    private Customer cliente;
    private LocalDateTime fechaCreacion;
    private String estado;

    public Cart() {}

    public Cart(Long id, Customer cliente, LocalDateTime fechaCreacion, String estado) {
        this.id = id;
        this.cliente = cliente;
        this.fechaCreacion = fechaCreacion;
        this.estado = estado;
    }

    // Métodos de negocio
    public boolean isAbandoned() {
        return "ABANDONED".equals(estado);
    }

    public boolean isActive() {
        return "ACTIVE".equals(estado);
    }

    public void abandon() {
        this.estado = "ABANDONED";
    }

    public void activate() {
        this.estado = "ACTIVE";
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Customer getCliente() {
        return cliente;
    }

    public void setCliente(Customer cliente) {
        this.cliente = cliente;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
