package com.arka.gestorsolicitudes.infrastructure.adapter.in.web;

import com.arka.gestorsolicitudes.application.service.GestorSolicitudesService;
import com.arka.gestorsolicitudes.domain.model.RespuestaProveedor;
import com.arka.gestorsolicitudes.domain.model.SolicitudProveedor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/solicitudes")
@CrossOrigin(origins = "*")
public class GestorSolicitudesController {

    private final GestorSolicitudesService gestorService;

    @Autowired
    public GestorSolicitudesController(GestorSolicitudesService gestorService) {
        this.gestorService = gestorService;
    }

    @PostMapping
    public ResponseEntity<SolicitudProveedor> crearSolicitud(@RequestBody SolicitudProveedor solicitud) {
        SolicitudProveedor creada = gestorService.crearSolicitud(solicitud);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PostMapping("/{solicitudId}/enviar/{proveedorId}")
    public ResponseEntity<SolicitudProveedor> enviarSolicitudAProveedor(
            @PathVariable String solicitudId,
            @PathVariable String proveedorId) {
        SolicitudProveedor solicitud = gestorService.enviarSolicitudAProveedor(solicitudId, proveedorId);
        return ResponseEntity.ok(solicitud);
    }

    @GetMapping("/{solicitudId}/respuestas")
    public ResponseEntity<List<RespuestaProveedor>> obtenerRespuestasProveedor(@PathVariable String solicitudId) {
        List<RespuestaProveedor> respuestas = gestorService.obtenerRespuestasProveedor(solicitudId);
        return ResponseEntity.ok(respuestas);
    }

    @PostMapping("/respuestas")
    public ResponseEntity<RespuestaProveedor> procesarRespuestaProveedor(@RequestBody RespuestaProveedor respuesta) {
        RespuestaProveedor procesada = gestorService.procesarRespuestaProveedor(respuesta);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(procesada);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Arca Gestor de Solicitudes está funcionando correctamente");
    }
}
