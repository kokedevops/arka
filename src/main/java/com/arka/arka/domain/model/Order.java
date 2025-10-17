package com.arka.arka.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public class Order {
    private Long id;
    private Customer cliente;
    private LocalDateTime fecha;
    private BigDecimal total;
    private Set<Product> productos;

    public Order() {}

    public Order(Long id, Customer cliente, LocalDateTime fecha, BigDecimal total, Set<Product> productos) {
        this.id = id;
        this.cliente = cliente;
        this.fecha = fecha;
        this.total = total;
        this.productos = productos;
    }

    // Métodos de negocio
    public BigDecimal calculateTotal() {
        if (productos == null || productos.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return productos.stream()
                .map(Product::getPrecioUnitario)
                .filter(precio -> precio != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean isValidOrder() {
        return cliente != null && 
               productos != null && 
               !productos.isEmpty() && 
               total != null && 
               total.compareTo(BigDecimal.ZERO) > 0;
    }

    public void addProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        if (!product.isAvailable()) {
            throw new IllegalArgumentException("Product is not available");
        }
        productos.add(product);
        updateTotal();
    }

    public void removeProduct(Product product) {
        productos.remove(product);
        updateTotal();
    }

    private void updateTotal() {
        this.total = calculateTotal();
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

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public Set<Product> getProductos() {
        return productos;
    }

    public void setProductos(Set<Product> productos) {
        this.productos = productos;
    }
}
