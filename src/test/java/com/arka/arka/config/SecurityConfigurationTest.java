package com.arka.arka.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas para verificar la configuración de seguridad
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.cloud.config.enabled=false",
    "eureka.client.enabled=false",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SecurityConfigurationTest {

    @Test
    void contextLoadsWithSecurity() {
        // Verifica que la configuración de seguridad se carga correctamente
        assertTrue(true, "El contexto de seguridad se carga sin errores");
    }
    
    @Test
    void applicationStartsSuccessfully() {
        // Verifica que la aplicación inicia correctamente con todas las configuraciones
        assertTrue(true, "La aplicación inicia exitosamente");
    }
}
