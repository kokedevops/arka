package com.arka.arka.infrastructure.config.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utilidad para manejar tokens JWT
 */
@Component
public class JwtTokenProvider {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenProvider.class);

    @Value("${arka.jwt.secret:arkaSecretKeyForJWTGenerationThatMustBeLongEnoughForHS512Algorithm2025}")
    private String jwtSecret;

    @Value("${arka.jwt.expiration:86400000}") // 24 horas en millisegundos
    private Long jwtExpirationMs;

    @Value("${arka.jwt.refresh-expiration:604800000}") // 7 días en millisegundos
    private Long refreshTokenExpirationMs;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    /**
     * Generar token JWT desde Authentication
     */
    public String generateJwtToken(Authentication authentication) {
        String username = authentication.getName();
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        claims.put("type", "access");

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generar refresh token
     */
    public String generateRefreshToken(Authentication authentication) {
        String username = authentication.getName();
        Date expiryDate = new Date(System.currentTimeMillis() + refreshTokenExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Generar token desde username
     */
    public String generateTokenFromUsername(String username) {
        Date expiryDate = new Date(System.currentTimeMillis() + jwtExpirationMs);

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");

        return Jwts.builder()
                .subject(username)
                .claims(claims)
                .issuedAt(new Date())
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Obtener username del token
     */
    public String getUsernameFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getSubject();
        } catch (JwtException e) {
            logger.error("Error al obtener username del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Obtener authorities del token
     */
    @SuppressWarnings("unchecked")
    public java.util.List<String> getAuthoritiesFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return (java.util.List<String>) claims.get("authorities");
        } catch (JwtException e) {
            logger.error("Error al obtener authorities del token: {}", e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    /**
     * Validar token JWT
     */
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (MalformedJwtException e) {
            logger.error("Token JWT malformado: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("Token JWT expirado: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string está vacío: {}", e.getMessage());
        } catch (JwtException e) {
            logger.error("Error de validación JWT: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Verificar si es un refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return "refresh".equals(claims.get("type"));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Obtener fecha de expiración del token
     */
    public Date getExpirationFromJwtToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration();
        } catch (JwtException e) {
            logger.error("Error al obtener expiración del token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Verificar si el token está próximo a expirar (menos de 5 minutos)
     */
    public boolean isTokenExpiringSoon(String token) {
        Date expiration = getExpirationFromJwtToken(token);
        if (expiration == null) return true;

        long timeUntilExpiration = expiration.getTime() - System.currentTimeMillis();
        return timeUntilExpiration < 300000; // 5 minutos en millisegundos
    }

    /**
     * Obtener tiempo restante de vida del token en segundos
     */
    public long getTokenRemainingTime(String token) {
        Date expiration = getExpirationFromJwtToken(token);
        if (expiration == null) return 0;

        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(0, remaining / 1000); // convertir a segundos
    }

    // Getters para las configuraciones
    public Long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    public Long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }
}
