package com.arka.gestorsolicitudes.application.service;

import com.arka.gestorsolicitudes.domain.model.RespuestaProveedor;
import com.arka.gestorsolicitudes.domain.model.SolicitudProveedor;

import java.util.List;

public interface GestorSolicitudesService {
    SolicitudProveedor crearSolicitud(SolicitudProveedor solicitud);
    SolicitudProveedor enviarSolicitudAProveedor(String solicitudId, String proveedorId);
    List<RespuestaProveedor> obtenerRespuestasProveedor(String solicitudId);
    RespuestaProveedor procesarRespuestaProveedor(RespuestaProveedor respuesta);
}
