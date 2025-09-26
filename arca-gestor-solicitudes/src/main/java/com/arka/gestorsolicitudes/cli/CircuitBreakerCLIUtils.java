package com.arka.gestorsolicitudes.cli;

import com.arka.gestorsolicitudes.application.service.CalculoEnvioService;
import com.arka.gestorsolicitudes.domain.model.CalculoEnvio;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidades CLI para Circuit Breaker que pueden ser llamadas desde REST
 */
@Component
public class CircuitBreakerCLIUtils {

    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String SEPARADOR_MEDIO = "────────────────────────────────";
    private static final String SEPARADOR_LARGO = "════════════════════════════════════════";
    private static final String SEPARADOR_EXTRA_LARGO = "════════════════════════════════════════════════════════════════";
    private static final DateTimeFormatter FECHA_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final CalculoEnvioService calculoEnvioService;

    public CircuitBreakerCLIUtils(CalculoEnvioService calculoEnvioService) {
        this.calculoEnvioService = calculoEnvioService;
    }

    /**
     * Ejecuta una prueba de carga programática
     */
    public String ejecutarPruebaDeCarga(int numLlamadas, String escenario) {
        StringBuilder resultado = new StringBuilder();
        appendLine(resultado, "🔄 PRUEBA DE CARGA - Circuit Breaker");
        appendLine(resultado, SEPARADOR_LARGO);
        appendLine(resultado, String.format("📊 Llamadas: %d | Escenario: %s", numLlamadas, escenario));
        appendLine(resultado, String.format("🕐 Inicio: %s", LocalDateTime.now().format(FECHA_FORMATTER)));
        resultado.append(LINE_SEPARATOR);

        int exitosos = 0;
        int fallbacks = 0;
        int errores = 0;
        long tiempoInicio = System.currentTimeMillis();

        for (int i = 1; i <= numLlamadas; i++) {
            try {
                CalculoEnvio calculo = calculoEnvioService
                    .probarCircuitBreaker(escenario, "Lima", "Arequipa", BigDecimal.valueOf(1.0));

                if (calculo != null) {
                    switch (calculo.getEstado()) {
                        case COMPLETADO -> {
                            exitosos++;
                            appendLine(resultado, String.format("✅ %02d: %s", i, calculo.getProveedorUtilizado()));
                        }
                        case FALLBACK -> {
                            fallbacks++;
                            appendLine(resultado, String.format("🔄 %02d: %s (Fallback)", i, calculo.getProveedorUtilizado()));
                        }
                        default -> {
                            errores++;
                            appendLine(resultado, String.format("❌ %02d: %s", i, calculo.getEstado()));
                        }
                    }
                } else {
                    errores++;
                    appendLine(resultado, String.format("❌ %02d: Sin respuesta", i));
                }

                Thread.sleep(100); // Pausa entre llamadas
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrumpido durante la prueba de carga", ie);
            } catch (Exception e) {
                errores++;
                appendLine(resultado, String.format("❌ %02d: %s", i, e.getMessage()));
            }
        }

        long tiempoTotal = System.currentTimeMillis() - tiempoInicio;

        appendLine(resultado, "");
        appendLine(resultado, "📈 RESUMEN DE RESULTADOS:");
        appendLine(resultado, SEPARADOR_LARGO);
        appendLine(resultado, String.format("✅ Exitosos: %d (%.1f%%)", exitosos, (double) exitosos / numLlamadas * 100));
        appendLine(resultado, String.format("🔄 Fallbacks: %d (%.1f%%)", fallbacks, (double) fallbacks / numLlamadas * 100));
        appendLine(resultado, String.format("❌ Errores: %d (%.1f%%)", errores, (double) errores / numLlamadas * 100));
        appendLine(resultado, String.format("⏱️  Tiempo total: %d ms", tiempoTotal));
        appendLine(resultado, String.format("📊 Promedio por llamada: %.1f ms", (double) tiempoTotal / numLlamadas));

        appendLine(resultado, "");
        appendLine(resultado, "🔍 ANÁLISIS CIRCUIT BREAKER:");
        appendLine(resultado, SEPARADOR_LARGO);
        if (fallbacks > 0) {
            appendLine(resultado, "🔴 Circuit Breaker ACTIVADO durante la prueba");
            appendLine(resultado, "🛡️  Fallbacks protegieron el sistema de fallos en cascada");
        } else {
            appendLine(resultado, "🟢 Circuit Breaker en estado NORMAL");
            appendLine(resultado, "✨ Todos los servicios funcionaron correctamente");
        }

        return resultado.toString();
    }

    /**
     * Genera un reporte de estado del Circuit Breaker
     */
    public String generarReporteEstado() {
        StringBuilder reporte = new StringBuilder();

        appendLine(reporte, "📊 REPORTE DE ESTADO - CIRCUIT BREAKER");
        appendLine(reporte, SEPARADOR_EXTRA_LARGO);
        appendLine(reporte, String.format("🕐 Fecha: %s", LocalDateTime.now().format(FECHA_FORMATTER)));
        appendLine(reporte, "🏢 Servicio: Arca Gestor Solicitudes");
        appendLine(reporte, "🔒 Componente: Circuit Breaker para Cálculo de Envío");
        reporte.append(LINE_SEPARATOR);

        try {
            String estadoServicio = calculoEnvioService.obtenerEstadoCalculos();
            appendLine(reporte, "✅ Estado del Servicio: " + estadoServicio);
        } catch (Exception e) {
            appendLine(reporte, "❌ Error al obtener estado: " + e.getMessage());
        }

        appendLine(reporte, "");
        appendLine(reporte, "🛠️  CONFIGURACIÓN ACTUAL:");
        appendLine(reporte, SEPARADOR_MEDIO);
        appendLine(reporte, "• Proveedor Externo Service:");
        appendLine(reporte, "  - Sliding Window: 8 llamadas");
        appendLine(reporte, "  - Failure Rate Threshold: 60%");
        appendLine(reporte, "  - Wait Duration: 15 segundos");
        reporte.append(LINE_SEPARATOR);
        appendLine(reporte, "• Calculo Envio Service:");
        appendLine(reporte, "  - Sliding Window: 10 llamadas");
        appendLine(reporte, "  - Failure Rate Threshold: 50%");
        appendLine(reporte, "  - Wait Duration: 10 segundos");
        appendLine(reporte, "  - Retry: 3 intentos");
        appendLine(reporte, "  - Timeout: 5 segundos");

        appendLine(reporte, "");
        appendLine(reporte, "💡 RECOMENDACIONES:");
        appendLine(reporte, SEPARADOR_MEDIO);
        appendLine(reporte, "• Monitoree las métricas regularmente");
        appendLine(reporte, "• Ejecute pruebas de carga periódicamente");
        appendLine(reporte, "• Verifique los logs para detectar patrones");
        appendLine(reporte, "• Ajuste los umbrales según el comportamiento observado");

        return reporte.toString();
    }

    /**
     * Ejecuta una demostración completa del Circuit Breaker
     */
    public String ejecutarDemostracion() {
        StringBuilder demo = new StringBuilder();
        appendLine(demo, "🎭 DEMOSTRACIÓN CIRCUIT BREAKER");
        appendLine(demo, SEPARADOR_EXTRA_LARGO);
        demo.append(LINE_SEPARATOR);

        appendLine(demo, "🔹 FASE 1: Funcionamiento Normal");
        appendLine(demo, SEPARADOR_MEDIO);
        try {
            CalculoEnvio resultado1 = calculoEnvioService
                .calcularEnvio("Lima", "Arequipa", BigDecimal.valueOf(2.0), "50x30x20");
            if (resultado1 != null) {
                appendLine(demo, String.format("✅ Resultado: %s - %s",
                    resultado1.getEstado(), resultado1.getProveedorUtilizado()));
            }
        } catch (Exception e) {
            appendLine(demo, "❌ Error: " + e.getMessage());
        }

        appendLine(demo, "");
        appendLine(demo, "🔹 FASE 2: Simulación de Fallos");
        appendLine(demo, SEPARADOR_MEDIO);
        for (int i = 1; i <= 5; i++) {
            try {
                CalculoEnvio resultado = calculoEnvioService
                    .probarCircuitBreaker("externo", "Lima", "Cusco", BigDecimal.valueOf(1.0));
                if (resultado != null) {
                    appendLine(demo, String.format("🔄 Intento %d: %s", i, resultado.getEstado()));
                }
                Thread.sleep(500);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Demostración interrumpida", ie);
            } catch (Exception e) {
                appendLine(demo, String.format("❌ Intento %d: %s", i, e.getMessage()));
            }
        }

        appendLine(demo, "");
        appendLine(demo, "🔹 FASE 3: Activación de Fallbacks");
        appendLine(demo, SEPARADOR_MEDIO + "──");
        try {
            CalculoEnvio resultado3 = calculoEnvioService
                .probarCircuitBreaker("completo", "Lima", "Trujillo", BigDecimal.valueOf(3.0));
            if (resultado3 != null) {
                appendLine(demo, String.format("🛡️  Fallback activado: %s - %s",
                    resultado3.getEstado(), resultado3.getProveedorUtilizado()));
                if (resultado3.getMensajeError() != null) {
                    appendLine(demo, "💬 Mensaje: " + resultado3.getMensajeError());
                }
            }
        } catch (Exception e) {
            appendLine(demo, "❌ Error: " + e.getMessage());
        }

        appendLine(demo, "");
        appendLine(demo, "🎯 CONCLUSIÓN:");
        appendLine(demo, SEPARADOR_MEDIO);
        appendLine(demo, "✨ El Circuit Breaker protege el sistema contra fallos en cascada");
        appendLine(demo, "🔄 Los fallbacks garantizan la disponibilidad del servicio");
        appendLine(demo, "📊 El sistema mantiene funcionalidad básica incluso con fallos");

        return demo.toString();
    }

    private StringBuilder appendLine(StringBuilder builder, String value) {
        return builder.append(value).append(LINE_SEPARATOR);
    }
}
