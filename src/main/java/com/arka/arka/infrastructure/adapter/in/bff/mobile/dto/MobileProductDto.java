package com.arka.arka.infrastructure.adapter.in.bff.mobile.dto;

import java.math.BigDecimal;

/**
 * Mobile-optimized Product DTO
 * Contains minimal data for mobile performance
 */
public class MobileProductDto {
    
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private String categoria;
    private boolean disponible;
    private String imagenUrl;
    
    public MobileProductDto() {}
    
    public MobileProductDto(Long id, String nombre, BigDecimal precio, 
                           String categoria, boolean disponible, String imagenUrl) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.categoria = categoria;
        this.disponible = disponible;
        this.imagenUrl = imagenUrl;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public BigDecimal getPrecio() {
        return precio;
    }
    
    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }
    
    public String getCategoria() {
        return categoria;
    }
    
    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public String getImagenUrl() {
        return imagenUrl;
    }
    
    public void setImagenUrl(String imagenUrl) {
        this.imagenUrl = imagenUrl;
    }
}
