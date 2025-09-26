package com.arka.gestorsolicitudes.cli;

import com.arka.gestorsolicitudes.application.service.CalculoEnvioService;
import com.arka.gestorsolicitudes.domain.model.CalculoEnvio;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * CLI interactiva para gestión de Circuit Breaker y cálculo de envíos
 */
@Component
@ConditionalOnProperty(name = "arka.cli.enabled", havingValue = "true")
public class CircuitBreakerCLI implements CommandLineRunner {

    private final CalculoEnvioService calculoEnvioService;
    private Scanner scanner;

    public CircuitBreakerCLI(CalculoEnvioService calculoEnvioService) {
        this.calculoEnvioService = calculoEnvioService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Solo iniciar CLI si se pasa el parámetro --cli
        for (String arg : args) {
            if ("--cli".equals(arg)) {
                iniciarCLIInteractiva();
                break;
            }
        }
    }

    /**
     * Inicia la CLI interactiva
     */
    private void iniciarCLIInteractiva() {
        scanner = new Scanner(System.in);
        mostrarBienvenida();
        
        boolean continuar = true;
        while (continuar) {
            mostrarMenu();
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1" -> calcularEnvioInteractivo();
                case "2" -> probarCircuitBreakerInteractivo();
                case "3" -> mostrarEstadoServicio();
                case "4" -> ejecutarPruebasDeCarga();
                case "5" -> mostrarAyuda();
                case "0" -> {
                    System.out.println("👋 ¡Hasta luego!");
                    continuar = false;
                }
                default -> System.out.println("❌ Opción no válida. Intente nuevamente.");
            }
            
            if (continuar) {
                System.out.println("\nPresiona Enter para continuar...");
                scanner.nextLine();
            }
        }
        scanner.close();
    }

    private void mostrarBienvenida() {
        System.out.println("""
                ╔══════════════════════════════════════════════════════════════╗
                ║                🔒 ARKA CIRCUIT BREAKER CLI                  ║
                ║              Gestión de Cálculo de Envíos                   ║
                ╚══════════════════════════════════════════════════════════════╝
                """);
    }

    private void mostrarMenu() {
        System.out.println("""
                
                📋 MENÚ PRINCIPAL:
                ─────────────────────────────────────────────────────────────
                1️⃣  Calcular Envío
                2️⃣  Probar Circuit Breaker
                3️⃣  Estado del Servicio
                4️⃣  Pruebas de Carga
                5️⃣  Ayuda
                0️⃣  Salir
                ─────────────────────────────────────────────────────────────
                Seleccione una opción: """);
    }

    private void calcularEnvioInteractivo() {
        System.out.println("\n📦 CÁLCULO DE ENVÍO");
        System.out.println("─────────────────────");
        
        System.out.print("🏠 Ciudad de origen (default: Lima): ");
        String origen = obtenerInputConDefault("Lima");
        
        System.out.print("🎯 Ciudad de destino (default: Arequipa): ");
        String destino = obtenerInputConDefault("Arequipa");
        
        System.out.print("⚖️  Peso en kg (default: 2.0): ");
        BigDecimal peso = obtenerPesoConDefault();
        
        System.out.print("📏 Dimensiones (default: 50x30x20): ");
        String dimensiones = obtenerInputConDefault("50x30x20");
        
        System.out.println("\n🔄 Calculando envío...");
        
        try {
        CalculoEnvio resultado = calculoEnvioService
            .calcularEnvio(origen, destino, peso, dimensiones);
            
            if (resultado != null) {
                mostrarResultadoCalculo(resultado);
            } else {
                System.out.println("❌ Error: No se pudo calcular el envío");
            }
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
        }
    }

    private void probarCircuitBreakerInteractivo() {
        System.out.println("\n🧪 PRUEBA DE CIRCUIT BREAKER");
        System.out.println("─────────────────────────────");
        
        System.out.println("Escenarios disponibles:");
        System.out.println("1. externo  - Proveedor externo");
        System.out.println("2. interno  - Servicio interno");
        System.out.println("3. completo - Flujo completo");
        
        System.out.print("Seleccione escenario (1-3, default: 3): ");
        String escenario = obtenerEscenario();
        
        System.out.print("🏠 Ciudad de origen (default: Lima): ");
        String origen = obtenerInputConDefault("Lima");
        
        System.out.print("🎯 Ciudad de destino (default: Cusco): ");
        String destino = obtenerInputConDefault("Cusco");
        
        System.out.print("⚖️  Peso en kg (default: 1.5): ");
        BigDecimal peso = obtenerPesoConDefault("1.5");
        
        System.out.println("\n🔄 Ejecutando prueba con escenario: " + escenario);
        
        try {
        CalculoEnvio resultado = calculoEnvioService
            .probarCircuitBreaker(escenario, origen, destino, peso);
            
            if (resultado != null) {
                mostrarResultadoPrueba(resultado, escenario);
            } else {
                System.out.println("❌ Error: No se pudo ejecutar la prueba");
            }
        } catch (Exception e) {
            System.out.println("❌ Error en prueba: " + e.getMessage());
        }
    }

    private void mostrarEstadoServicio() {
        System.out.println("\n📊 ESTADO DEL SERVICIO");
        System.out.println("─────────────────────");
        
        try {
            String estado = calculoEnvioService.obtenerEstadoCalculos();
            System.out.println("✅ Estado: " + estado);
            System.out.println("🕐 Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        } catch (Exception e) {
            System.out.println("❌ Error al obtener estado: " + e.getMessage());
        }
    }

    private void ejecutarPruebasDeCarga() {
        System.out.println("\n⚡ PRUEBAS DE CARGA");
        System.out.println("─────────────────────");
        
        System.out.print("📊 Número de llamadas (default: 10): ");
        int numLlamadas = obtenerNumeroConDefault(10);
        
        System.out.print("🎯 Escenario (1=externo, 2=interno, 3=completo, default: 1): ");
        String escenario = obtenerEscenario();
        
        System.out.println("\n🔄 Ejecutando " + numLlamadas + " llamadas...");
        
        int exitosos = 0, fallbacks = 0, errores = 0;
        
        for (int i = 1; i <= numLlamadas; i++) {
            try {
                System.out.print("Llamada " + i + ": ");
                
        CalculoEnvio calculo = calculoEnvioService
            .probarCircuitBreaker(escenario, "Lima", "Arequipa", BigDecimal.valueOf(1.0));
                
                if (calculo != null) {
                    switch (calculo.getEstado()) {
                        case COMPLETADO -> {
                            exitosos++;
                            System.out.println("✅ " + calculo.getProveedorUtilizado());
                        }
                        case FALLBACK -> {
                            fallbacks++;
                            System.out.println("🔄 " + calculo.getProveedorUtilizado());
                        }
                        default -> {
                            errores++;
                            System.out.println("❌ " + calculo.getEstado());
                        }
                    }
                } else {
                    errores++;
                    System.out.println("❌ No response");
                }
                
                Thread.sleep(300);
            } catch (Exception e) {
                errores++;
                System.out.println("❌ " + e.getMessage());
            }
        }
        
        System.out.println("\n� RESUMEN DE PRUEBAS:");
        System.out.println("✅ Exitosos: " + exitosos);
        System.out.println("🔄 Fallbacks: " + fallbacks);
        System.out.println("❌ Errores: " + errores);
        System.out.printf("� Tasa de éxito: %.1f%%\n", (double) exitosos / numLlamadas * 100);
    }

    private void mostrarAyuda() {
        System.out.println("""
                
                🔒 AYUDA - CIRCUIT BREAKER CLI
                ════════════════════════════════════════════════════════════════
                
                � OPCIONES DEL MENÚ:
                
                1️⃣  CALCULAR ENVÍO
                   Calcula el costo y tiempo de envío entre dos ciudades
                   Utiliza el Circuit Breaker para proteger contra fallos
                
                2️⃣  PROBAR CIRCUIT BREAKER
                   Prueba diferentes escenarios:
                   • Externo: Solo proveedor externo
                   • Interno: Solo servicio interno
                   • Completo: Flujo completo con fallbacks
                
                3️⃣  ESTADO DEL SERVICIO
                   Muestra el estado actual del servicio de cálculo
                
                4️⃣  PRUEBAS DE CARGA
                   Ejecuta múltiples llamadas para probar el Circuit Breaker
                   Útil para verificar el comportamiento bajo carga
                
                🎯 ESTADOS DEL CIRCUIT BREAKER:
                   • CLOSED: Funcionamiento normal
                   • OPEN: Circuit Breaker activado (usando fallbacks)
                   • HALF_OPEN: Probando recuperación del servicio
                
                💡 CONSEJOS:
                   • Use pruebas de carga para simular fallos
                   • Monitoree el cambio de estados durante las pruebas
                   • Los fallbacks garantizan disponibilidad del servicio
                """);
    }

    // Métodos auxiliares
    private String obtenerInputConDefault(String defaultValue) {
        String input = scanner.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }

    private BigDecimal obtenerPesoConDefault() {
        return obtenerPesoConDefault("2.0");
    }

    private BigDecimal obtenerPesoConDefault(String defaultValue) {
        String input = scanner.nextLine().trim();
        try {
            return input.isEmpty() ? new BigDecimal(defaultValue) : new BigDecimal(input);
        } catch (NumberFormatException e) {
            return new BigDecimal(defaultValue);
        }
    }

    private int obtenerNumeroConDefault(int defaultValue) {
        String input = scanner.nextLine().trim();
        try {
            return input.isEmpty() ? defaultValue : Integer.parseInt(input);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String obtenerEscenario() {
        String input = scanner.nextLine().trim();
        return switch (input) {
            case "1" -> "externo";
            case "2" -> "interno";
            case "3", "" -> "completo";
            default -> "completo";
        };
    }

    private void mostrarResultadoCalculo(CalculoEnvio resultado) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        
        String estadoIcon = switch (resultado.getEstado()) {
            case COMPLETADO -> "✅";
            case FALLBACK -> "🔄";
            case ERROR -> "❌";
            case TIMEOUT -> "⏱️";
            default -> "📝";
        };

        System.out.printf("""
                
                %s RESULTADO DEL CÁLCULO:
                ════════════════════════════════════════════════════════════════
                🏷️  ID: %s
                📍 Ruta: %s → %s
                ⚖️  Peso: %s kg
                📦 Dimensiones: %s
                💰 Costo: S/ %.2f
                🕐 Tiempo estimado: %d días
                🏢 Proveedor: %s
                📊 Estado: %s
                🕒 Fecha: %s
                %s
                """,
                estadoIcon,
                resultado.getId() != null ? resultado.getId() : "N/A",
                resultado.getOrigen(),
                resultado.getDestino(),
                resultado.getPeso(),
                resultado.getDimensiones(),
                resultado.getCosto(),
                resultado.getTiempoEstimadoDias(),
                resultado.getProveedorUtilizado(),
                resultado.getEstado().getDescripcion(),
                resultado.getFechaCalculo().format(formatter),
                resultado.getMensajeError() != null ? "⚠️ Mensaje: " + resultado.getMensajeError() : ""
        );
    }

    private void mostrarResultadoPrueba(CalculoEnvio resultado, String escenario) {
        String escenarioIcon = switch (escenario.toLowerCase()) {
            case "externo" -> "🌐";
            case "interno" -> "🏠";
            case "completo" -> "🔄";
            default -> "🧪";
        };

        System.out.printf("""
                
                %s PRUEBA DE CIRCUIT BREAKER - ESCENARIO: %s
                ════════════════════════════════════════════════════════════════
                """,
                escenarioIcon,
                escenario.toUpperCase()
        );
        
        mostrarResultadoCalculo(resultado);
        
        System.out.println("\n🎯 Análisis:");
        if (resultado.getEstado() == com.arka.gestorsolicitudes.domain.model.EstadoCalculo.FALLBACK) {
            System.out.println("🔴 Circuit Breaker ACTIVADO - Usando valores de fallback");
        } else {
            System.out.println("🟢 Servicio funcionando normalmente");
        }
    }
}
