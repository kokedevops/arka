package com.arka.arka.infrastructure.adapter.in.bff.mobile.dto;

import java.time.LocalDateTime;

/**
 * Mobile-optimized Cart DTO
 * Contains minimal data for mobile performance
 */
public class MobileCartDto {
    
    private Long id;
    private String estado;
    private LocalDateTime fechaCreacion;
    private Integer cantidadItems;
    private String clienteNombre;
    
    public MobileCartDto() {}
    
    public MobileCartDto(Long id, String estado, LocalDateTime fechaCreacion, 
                        Integer cantidadItems, String clienteNombre) {
        this.id = id;
        this.estado = estado;
        this.fechaCreacion = fechaCreacion;
        this.cantidadItems = cantidadItems;
        this.clienteNombre = clienteNombre;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getEstado() {
        return estado;
    }
    
    public void setEstado(String estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public Integer getCantidadItems() {
        return cantidadItems;
    }
    
    public void setCantidadItems(Integer cantidadItems) {
        this.cantidadItems = cantidadItems;
    }
    
    public String getClienteNombre() {
        return clienteNombre;
    }
    
    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }
}
