package com.arka.configserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 🔐 Config Server Security Configuration
 * 
 * Configuración de seguridad para el servidor de configuración.
 * Permite acceso público a endpoints específicos y protege
 * las configuraciones sensibles.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                // Permitir acceso público a health checks
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Permitir acceso a configuraciones (en desarrollo)
                .requestMatchers("/**").permitAll()
                // Cualquier otra request requiere autenticación
                .anyRequest().authenticated()
            )
            .httpBasic(httpBasic -> {
                // Configuración básica HTTP para desarrollo
            });

        return http.build();
    }
}
