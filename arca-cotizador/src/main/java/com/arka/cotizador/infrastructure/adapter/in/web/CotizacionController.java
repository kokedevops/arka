package com.arka.cotizador.infrastructure.adapter.in.web;

import com.arka.cotizador.application.service.CotizacionService;
import com.arka.cotizador.domain.model.CotizacionRequest;
import com.arka.cotizador.domain.model.CotizacionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cotizaciones")
@CrossOrigin(origins = "*")
public class CotizacionController {

    private final CotizacionService cotizacionService;

    @Autowired
    public CotizacionController(CotizacionService cotizacionService) {
        this.cotizacionService = cotizacionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CotizacionResponse generarCotizacion(@RequestBody CotizacionRequest request) {
        return cotizacionService.generarCotizacion(request);
    }

    @GetMapping("/{cotizacionId}")
    public CotizacionResponse obtenerCotizacion(@PathVariable String cotizacionId) {
        return cotizacionService.obtenerCotizacion(cotizacionId);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Arca Cotizador está funcionando correctamente");
    }
}
