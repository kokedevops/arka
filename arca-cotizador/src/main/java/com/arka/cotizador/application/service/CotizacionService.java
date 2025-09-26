package com.arka.cotizador.application.service;

import com.arka.cotizador.domain.model.CotizacionRequest;
import com.arka.cotizador.domain.model.CotizacionResponse;
public interface CotizacionService {
    CotizacionResponse generarCotizacion(CotizacionRequest request);
    CotizacionResponse obtenerCotizacion(String cotizacionId);
}
