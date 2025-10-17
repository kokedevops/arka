package com.arka.arka.domain.model;

import java.util.List;

public class Category {
    private Long id;
    private String nombre;

    public Category() {}

    public Category(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    // Métodos de negocio
    public boolean isValidName() {
        return nombre != null && !nombre.trim().isEmpty();
    }

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
