package com.arka.arka.domain.model;

/**
 * Enumeración que define los estados posibles de una cotización
 * Parte del modelo de dominio para el bounded context de cotizaciones
 */
public enum EstadoCotizacion {
    
    /**
     * Cotización recién creada, esperando respuesta del cliente
     */
    PENDIENTE("Pendiente de respuesta"),
    
    /**
     * Cotización aceptada por el cliente
     */
    ACEPTADA("Aceptada por el cliente"),
    
    /**
     * Cotización rechazada por el cliente
     */
    RECHAZADA("Rechazada por el cliente"),
    
    /**
     * Cotización que ha expirado por tiempo
     */
    EXPIRADA("Expirada por tiempo"),
    
    /**
     * Cotización cancelada por motivos internos
     */
    CANCELADA("Cancelada por motivos internos"),
    
    /**
     * Cotización en proceso de revisión
     */
    EN_REVISION("En proceso de revisión");
    
    private final String descripcion;
    
    EstadoCotizacion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    /**
     * Verifica si el estado permite transición a otro estado
     */
    public boolean puedeTransicionarA(EstadoCotizacion nuevoEstado) {
        return switch (this) {
            case PENDIENTE -> nuevoEstado == ACEPTADA || nuevoEstado == RECHAZADA || 
                           nuevoEstado == EXPIRADA || nuevoEstado == CANCELADA || 
                           nuevoEstado == EN_REVISION;
            case EN_REVISION -> nuevoEstado == ACEPTADA || nuevoEstado == RECHAZADA || 
                              nuevoEstado == CANCELADA || nuevoEstado == PENDIENTE;
            case ACEPTADA, RECHAZADA, EXPIRADA, CANCELADA -> false; // Estados finales
        };
    }
    
    /**
     * Verifica si es un estado final (no permite más transiciones)
     */
    public boolean esFinal() {
        return this == ACEPTADA || this == RECHAZADA || this == EXPIRADA || this == CANCELADA;
    }
}
