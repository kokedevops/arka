package com.arka.arkavalenzuela.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Test Security Configuration - provides controlled security behavior for testing
 * This configuration provides realistic security behavior while avoiding conflicts
 */
@TestConfiguration
@EnableWebSecurity
@Order(1) // Highest priority to override main config
public class TestSecurityConfig {

    /**
     * Test security filter chain with controlled access rules
     */
    @Bean(name = "testSecurityFilterChain")
    @Primary
    @Order(1)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            // Disable CSRF for API testing
            .csrf(csrf -> csrf.disable())
            
            // Stateless session management like in production
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure specific authorization rules for testing
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - should always work
                .requestMatchers("/api/public/**", "/health", "/actuator/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/", "/error").permitAll()
                
                // CORS preflight requests should be allowed
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                
                // Protected endpoints - should require authentication
                .requestMatchers("/api/solicitudes/**", "/api/users/**").authenticated()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Default deny
                .anyRequest().authenticated()
            )
            
            // Enable HTTP Basic for simple testing (no JWT complexity)
            .httpBasic(httpBasic -> httpBasic.realmName("Test"))
            
            // Add security headers
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                )
            )
            
            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Disable other authentication methods to keep it simple
            .formLogin(formLogin -> formLogin.disable())
            .rememberMe(rememberMe -> rememberMe.disable())
            .logout(logout -> logout.disable())
            
            .build();
    }

    /**
     * CORS configuration for testing
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://localhost:*"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Test UserDetailsService with known test users
     */
    @Bean(name = "testUserDetailsService")
    @Primary
    public UserDetailsService testUserDetailsService() {
        UserDetails testUser = User.builder()
            .username("testuser")
            .password("testpass")
            .roles("USER")
            .build();
            
        UserDetails adminUser = User.builder()
            .username("admin")
            .password("admin")
            .roles("USER", "ADMIN")
            .build();
        
        return new InMemoryUserDetailsManager(testUser, adminUser);
    }
    
    /**
     * Simple password encoder for testing
     */
    @Bean(name = "testPasswordEncoder")
    @Primary
    @SuppressWarnings("deprecation")
    public PasswordEncoder testPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }
}
