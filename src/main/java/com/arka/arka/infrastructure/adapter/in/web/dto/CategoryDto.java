package com.arka.arka.infrastructure.adapter.in.web.dto;

public class CategoryDto {
    private Long id;
    private String nombre;

    public CategoryDto() {}

    // Getters y Setters
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
}
