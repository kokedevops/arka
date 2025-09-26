package com.arka.security.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories;

/**
 * Configuración de auto-configuración para el módulo de seguridad ARKA
 */
@Configuration
@ComponentScan(basePackages = {
    "com.arka.security.service",
    "com.arka.security.config"
})
@EntityScan(basePackages = "com.arka.security.domain.model")
@EnableJdbcRepositories(basePackages = "com.arka.security.domain.repository")
public class ArkaSecurityAutoConfiguration {
    
}
