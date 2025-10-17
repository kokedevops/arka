package com.arka.arka.domain.model;

public class Customer {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String pais;
    private String ciudad;

    public Customer() {}

    public Customer(Long id, String nombre, String email, String telefono, String pais, String ciudad) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.pais = pais;
        this.ciudad = ciudad;
    }

    // Métodos de negocio
    public boolean isValidEmail() {
        return email != null && email.contains("@") && email.contains(".");
    }

    public boolean hasCompleteProfile() {
        return nombre != null && !nombre.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               isValidEmail();
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
}
