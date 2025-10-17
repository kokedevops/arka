package com.arka.arka.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Order
 * Used for REST API communication
 */
public class OrderDto {
    
    private Long id;
    private Long customerId;
    private String customerName;
    private List<Long> productIds;
    private List<String> productNames;
    private BigDecimal total;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaPedido;
    
    private String estado;

    public OrderDto() {}

    public OrderDto(Long id, Long customerId, String customerName, 
                   List<Long> productIds, List<String> productNames,
                   BigDecimal total, LocalDateTime fechaPedido, String estado) {
        this.id = id;
        this.customerId = customerId;
        this.customerName = customerName;
        this.productIds = productIds;
        this.productNames = productNames;
        this.total = total;
        this.fechaPedido = fechaPedido;
        this.estado = estado;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public List<Long> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<Long> productIds) {
        this.productIds = productIds;
    }

    public List<String> getProductNames() {
        return productNames;
    }

    public void setProductNames(List<String> productNames) {
        this.productNames = productNames;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getFechaPedido() {
        return fechaPedido;
    }

    public void setFechaPedido(LocalDateTime fechaPedido) {
        this.fechaPedido = fechaPedido;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "OrderDto{" +
                "id=" + id +
                ", customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", productIds=" + productIds +
                ", productNames=" + productNames +
                ", total=" + total +
                ", fechaPedido=" + fechaPedido +
                ", estado='" + estado + '\'' +
                '}';
    }
}
