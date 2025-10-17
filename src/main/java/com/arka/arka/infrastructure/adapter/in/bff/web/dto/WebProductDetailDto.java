package com.arka.arka.infrastructure.adapter.in.bff.web.dto;

import java.math.BigDecimal;

/**
 * Web-optimized Product Detail DTO
 * Contains rich data for web applications
 */
public class WebProductDetailDto {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precioUnitario;
    private Integer stock;
    private String categoria;
    private boolean disponible;
    private String[] imagenes;
    private String sku;
    private String dimensiones;
    private String peso;
    
    public WebProductDetailDto() {}
    
    public WebProductDetailDto(Long id, String nombre, String descripcion, 
                              BigDecimal precioUnitario, Integer stock, String categoria, 
                              boolean disponible, String[] imagenes, String sku,
                              String dimensiones, String peso) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precioUnitario = precioUnitario;
        this.stock = stock;
        this.categoria = categoria;
        this.disponible = disponible;
        this.imagenes = imagenes;
        this.sku = sku;
        this.dimensiones = dimensiones;
        this.peso = peso;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    
    public String[] getImagenes() { return imagenes; }
    public void setImagenes(String[] imagenes) { this.imagenes = imagenes; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getDimensiones() { return dimensiones; }
    public void setDimensiones(String dimensiones) { this.dimensiones = dimensiones; }
    
    public String getPeso() { return peso; }
    public void setPeso(String peso) { this.peso = peso; }
}
