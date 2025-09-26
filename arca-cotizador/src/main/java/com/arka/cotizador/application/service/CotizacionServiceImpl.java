package com.arka.cotizador.application.service;

import com.arka.cotizador.domain.model.CotizacionRequest;
import com.arka.cotizador.domain.model.CotizacionResponse;
import com.arka.cotizador.domain.model.ItemCotizado;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class CotizacionServiceImpl implements CotizacionService {

    @Override
    public CotizacionResponse generarCotizacion(CotizacionRequest request) {
        // Simulación de lógica de cotización
        String cotizacionId = UUID.randomUUID().toString();

        // Por ahora simulamos precios fijos, en una implementación real
        // consultaríamos un servicio de precios o base de datos
        var itemsCotizados = new ArrayList<ItemCotizado>();
        BigDecimal total = BigDecimal.ZERO;

        for (var item : request.getItems()) {
            // Precio simulado
            BigDecimal precio = BigDecimal.valueOf(100.00);
            var itemCotizado = new ItemCotizado(
                item.getProductoId(),
                "Producto " + item.getProductoId(),
                item.getCantidad(),
                precio
            );
            itemsCotizados.add(itemCotizado);
            total = total.add(itemCotizado.getSubtotal());
        }

        return new CotizacionResponse(cotizacionId, request.getClienteId(), itemsCotizados, total);
    }

    @Override
    public CotizacionResponse obtenerCotizacion(String cotizacionId) {
        // Simulación de búsqueda de cotización
        // En una implementación real consultaríamos la base de datos
        return new CotizacionResponse(cotizacionId, "cliente-demo", new ArrayList<>(), BigDecimal.ZERO);
    }
}
