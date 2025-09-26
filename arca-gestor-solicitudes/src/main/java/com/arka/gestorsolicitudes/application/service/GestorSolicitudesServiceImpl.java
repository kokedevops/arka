package com.arka.gestorsolicitudes.application.service;

import com.arka.gestorsolicitudes.domain.model.RespuestaProveedor;
import com.arka.gestorsolicitudes.domain.model.SolicitudProveedor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class GestorSolicitudesServiceImpl implements GestorSolicitudesService {
    @Override
    public SolicitudProveedor crearSolicitud(SolicitudProveedor solicitud) {
        if (solicitud.getSolicitudId() == null) {
            solicitud.setSolicitudId(UUID.randomUUID().toString());
        }

        if (solicitud.getEstado() == null) {
            solicitud.setEstado("CREADA");
        }

        return solicitud;
    }

    @Override
    public SolicitudProveedor enviarSolicitudAProveedor(String solicitudId, String proveedorId) {
        var solicitud = new SolicitudProveedor(solicitudId, proveedorId, "cliente-demo", new ArrayList<>());
        solicitud.setEstado("ENVIADA");
        return solicitud;
    }

    @Override
    public List<RespuestaProveedor> obtenerRespuestasProveedor(String solicitudId) {
        return Collections.emptyList();
    }

    @Override
    public RespuestaProveedor procesarRespuestaProveedor(RespuestaProveedor respuesta) {
        respuesta.setEstado("PROCESADA");
        return respuesta;
    }
}
