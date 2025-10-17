package com.arka.arka.infrastructure.adapter.in.bff.web.dto;

import java.math.BigDecimal;

/**
 * Web Dashboard DTO
 * Contains aggregated data for web admin dashboard
 */
public class WebDashboardDto {
    
    private int totalProductos;
    private int totalCarritos;
    private int totalPedidos;
    private int carritosAbandonados;
    private BigDecimal ventasTotales;
    private int productosConBajoStock;
    private String[] productosPopulares;
    private DashboardStats stats;
    
    public WebDashboardDto() {}
    
    public WebDashboardDto(int totalProductos, int totalCarritos, int totalPedidos,
                          int carritosAbandonados, BigDecimal ventasTotales,
                          int productosConBajoStock, String[] productosPopulares,
                          DashboardStats stats) {
        this.totalProductos = totalProductos;
        this.totalCarritos = totalCarritos;
        this.totalPedidos = totalPedidos;
        this.carritosAbandonados = carritosAbandonados;
        this.ventasTotales = ventasTotales;
        this.productosConBajoStock = productosConBajoStock;
        this.productosPopulares = productosPopulares;
        this.stats = stats;
    }
    
    // Getters and Setters
    public int getTotalProductos() { return totalProductos; }
    public void setTotalProductos(int totalProductos) { this.totalProductos = totalProductos; }
    
    public int getTotalCarritos() { return totalCarritos; }
    public void setTotalCarritos(int totalCarritos) { this.totalCarritos = totalCarritos; }
    
    public int getTotalPedidos() { return totalPedidos; }
    public void setTotalPedidos(int totalPedidos) { this.totalPedidos = totalPedidos; }
    
    public int getCarritosAbandonados() { return carritosAbandonados; }
    public void setCarritosAbandonados(int carritosAbandonados) { this.carritosAbandonados = carritosAbandonados; }
    
    public BigDecimal getVentasTotales() { return ventasTotales; }
    public void setVentasTotales(BigDecimal ventasTotales) { this.ventasTotales = ventasTotales; }
    
    public int getProductosConBajoStock() { return productosConBajoStock; }
    public void setProductosConBajoStock(int productosConBajoStock) { this.productosConBajoStock = productosConBajoStock; }
    
    public String[] getProductosPopulares() { return productosPopulares; }
    public void setProductosPopulares(String[] productosPopulares) { this.productosPopulares = productosPopulares; }
    
    public DashboardStats getStats() { return stats; }
    public void setStats(DashboardStats stats) { this.stats = stats; }
    
    /**
     * Inner class for additional dashboard statistics
     */
    public static class DashboardStats {
        private double tasaConversionCarritos;
        private double promedioTiempoRespuesta;
        private int notificacionesEnviadas;
        
        public DashboardStats() {}
        
        public DashboardStats(double tasaConversionCarritos, double promedioTiempoRespuesta, int notificacionesEnviadas) {
            this.tasaConversionCarritos = tasaConversionCarritos;
            this.promedioTiempoRespuesta = promedioTiempoRespuesta;
            this.notificacionesEnviadas = notificacionesEnviadas;
        }
        
        public double getTasaConversionCarritos() { return tasaConversionCarritos; }
        public void setTasaConversionCarritos(double tasaConversionCarritos) { this.tasaConversionCarritos = tasaConversionCarritos; }
        
        public double getPromedioTiempoRespuesta() { return promedioTiempoRespuesta; }
        public void setPromedioTiempoRespuesta(double promedioTiempoRespuesta) { this.promedioTiempoRespuesta = promedioTiempoRespuesta; }
        
        public int getNotificacionesEnviadas() { return notificacionesEnviadas; }
        public void setNotificacionesEnviadas(int notificacionesEnviadas) { this.notificacionesEnviadas = notificacionesEnviadas; }
    }
}
