package com.arka.arkavalenzuela.reactive;

import com.arka.arkavalenzuela.domain.model.Solicitud;
import com.arka.arkavalenzuela.domain.model.Cotizacion;
import com.arka.arkavalenzuela.application.service.SolicitudService;
import com.arka.arkavalenzuela.application.service.CotizacionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Pruebas reactivas con StepVerifier para validar flujos Mono y Flux
 * Estas pruebas demuestran el manejo correcto de programación reactiva
 */
@ExtendWith(MockitoExtension.class)
class ReactiveStepVerifierTest {

    @Mock
    private SolicitudService solicitudService;
    
    @Mock
    private CotizacionService cotizacionService;

    private Solicitud solicitudTest;
    private Cotizacion cotizacionTest;
    private List<Solicitud> solicitudesTest;

    @BeforeEach
    void setUp() {
        // Configurar datos de prueba
        solicitudTest = new Solicitud();
        solicitudTest.setId(1L);
        solicitudTest.setDescripcion("Solicitud de prueba");
        solicitudTest.setFechaCreacion(LocalDateTime.now());
        solicitudTest.setEstado("PENDIENTE");

        cotizacionTest = new Cotizacion();
        cotizacionTest.setId(1L);
        cotizacionTest.setSolicitudId(1L);
        cotizacionTest.setMonto(new BigDecimal("1000.0"));

        solicitudesTest = Arrays.asList(
            createSolicitud(1L, "Primera solicitud"),
            createSolicitud(2L, "Segunda solicitud"),
            createSolicitud(3L, "Tercera solicitud")
        );
    }

    @Test
    void testCrearSolicitudReactivo_Success() {
        // Given
        when(solicitudService.crearSolicitud(any())).thenReturn(Mono.just(solicitudTest));

        // When & Then
        StepVerifier.create(solicitudService.crearSolicitud(solicitudTest))
            .expectNext(solicitudTest)
            .verifyComplete();
    }

    @Test
    void testCrearSolicitudReactivo_WithDelay() {
        // Given
        when(solicitudService.crearSolicitud(any()))
            .thenReturn(Mono.just(solicitudTest).delayElement(Duration.ofMillis(100)));

        // When & Then
        StepVerifier.create(solicitudService.crearSolicitud(solicitudTest))
            .expectNext(solicitudTest)
            .expectComplete()
            .verify(Duration.ofSeconds(5));
    }

    @Test
    void testObtenerSolicitudPorId_Success() {
        // Given
        when(solicitudService.obtenerPorId(1L)).thenReturn(Mono.just(solicitudTest));

        // When & Then
        StepVerifier.create(solicitudService.obtenerPorId(1L))
            .expectNext(solicitudTest)
            .verifyComplete();
    }

    @Test
    void testObtenerSolicitudPorId_NotFound() {
        // Given
        when(solicitudService.obtenerPorId(999L)).thenReturn(Mono.empty());

        // When & Then
        StepVerifier.create(solicitudService.obtenerPorId(999L))
            .expectComplete()
            .verify();
    }

    @Test
    void testObtenerSolicitudPorId_Error() {
        // Given
        when(solicitudService.obtenerPorId(anyLong()))
            .thenReturn(Mono.error(new RuntimeException("Error de base de datos")));

        // When & Then
        StepVerifier.create(solicitudService.obtenerPorId(1L))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    void testObtenerTodasLasSolicitudes_Success() {
        // Given
        when(solicitudService.obtenerTodas()).thenReturn(Flux.fromIterable(solicitudesTest));

        // When & Then
        StepVerifier.create(solicitudService.obtenerTodas())
            .expectNext(solicitudesTest.get(0))
            .expectNext(solicitudesTest.get(1))
            .expectNext(solicitudesTest.get(2))
            .verifyComplete();
    }

    @Test
    void testObtenerTodasLasSolicitudes_Empty() {
        // Given
        when(solicitudService.obtenerTodas()).thenReturn(Flux.empty());

        // When & Then
        StepVerifier.create(solicitudService.obtenerTodas())
            .expectComplete()
            .verify();
    }

    @Test
    void testObtenerSolicitudesConFiltro() {
        // Given
        List<Solicitud> solicitudesFiltradas = Arrays.asList(solicitudesTest.get(0));
        when(solicitudService.obtenerPorEstado("PENDIENTE"))
            .thenReturn(Flux.fromIterable(solicitudesFiltradas));

        // When & Then
        StepVerifier.create(solicitudService.obtenerPorEstado("PENDIENTE"))
            .expectNextCount(1)
            .verifyComplete();
    }

    @Test
    void testGenerarCotizacion_Success() {
        // Given
        when(cotizacionService.generarCotizacion(1L)).thenReturn(Mono.just(cotizacionTest));

        // When & Then
        StepVerifier.create(cotizacionService.generarCotizacion(1L))
            .expectNext(cotizacionTest)
            .verifyComplete();
    }

    @Test
    void testFlujoCombinado_SolicitudYCotizacion() {
        // Given
        when(solicitudService.crearSolicitud(any())).thenReturn(Mono.just(solicitudTest));
        when(cotizacionService.generarCotizacion(1L)).thenReturn(Mono.just(cotizacionTest));

        // When
        Mono<String> resultado = solicitudService.crearSolicitud(solicitudTest)
            .flatMap(solicitud -> cotizacionService.generarCotizacion(solicitud.getId()))
            .map(cotizacion -> "Solicitud creada con cotización: " + cotizacion.getMonto());

        // Then
        StepVerifier.create(resultado)
            .expectNext("Solicitud creada con cotización: 1000.0")
            .verifyComplete();
    }

    @Test
    void testManejoDeErroresConFallback() {
        // Given
        when(solicitudService.obtenerPorId(1L))
            .thenReturn(Mono.error(new RuntimeException("Error de conexión")));

        // When
        Mono<Solicitud> resultado = solicitudService.obtenerPorId(1L)
            .onErrorReturn(createSolicitudDefault());

        // Then
        StepVerifier.create(resultado)
            .expectNextMatches(solicitud -> solicitud.getDescripcion().equals("Solicitud por defecto"))
            .verifyComplete();
    }

    @Test
    void testBackpressure_ConFluxLargo() {
        // Given
        Flux<Integer> flujoLargo = Flux.range(1, 1000)
            .delayElements(Duration.ofMillis(1));

        // When & Then
        StepVerifier.create(flujoLargo)
            .expectNextCount(1000)
            .verifyComplete();
    }

    @Test
    void testTimeoutManagement() {
        // Given
        when(solicitudService.obtenerPorId(1L))
            .thenReturn(Mono.just(solicitudTest).delayElement(Duration.ofSeconds(2)));

        // When & Then
        StepVerifier.create(solicitudService.obtenerPorId(1L).timeout(Duration.ofSeconds(1)))
            .expectError()
            .verify();
    }

    @Test
    void testRetryMechanism() {
        // Given - Create a service that fails first 2 times then succeeds
        AtomicInteger callCount = new AtomicInteger(0);
        
        // Create a Mono that simulates the retry behavior
        Mono<Solicitud> retryMono = Mono.defer(() -> {
            int count = callCount.incrementAndGet();
            if (count <= 2) {
                return Mono.error(new RuntimeException("Fallo temporal " + count));
            } else {
                return Mono.just(solicitudTest);
            }
        }).retry(2);

        // When & Then
        StepVerifier.create(retryMono)
            .expectNext(solicitudTest)
            .verifyComplete();
    }

    @Test
    void testZipMultiplesServicios() {
        // Given
        when(solicitudService.obtenerPorId(1L)).thenReturn(Mono.just(solicitudTest));
        when(cotizacionService.generarCotizacion(1L)).thenReturn(Mono.just(cotizacionTest));

        // When
        Mono<String> resultado = Mono.zip(
            solicitudService.obtenerPorId(1L),
            cotizacionService.generarCotizacion(1L)
        ).map(tuple -> String.format("Solicitud %s con cotización %s", 
            tuple.getT1().getDescripcion(), 
            tuple.getT2().getMonto()));

        // Then
        StepVerifier.create(resultado)
            .expectNext("Solicitud Solicitud de prueba con cotización 1000.0")
            .verifyComplete();
    }

    // Métodos auxiliares
    private Solicitud createSolicitud(Long id, String descripcion) {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(id);
        solicitud.setDescripcion(descripcion);
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitud.setEstado("PENDIENTE");
        return solicitud;
    }

    private Solicitud createSolicitudDefault() {
        Solicitud solicitud = new Solicitud();
        solicitud.setId(0L);
        solicitud.setDescripcion("Solicitud por defecto");
        solicitud.setFechaCreacion(LocalDateTime.now());
        solicitud.setEstado("DEFAULT");
        return solicitud;
    }
}
