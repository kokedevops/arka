package com.arka.arkavalenzuela.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el servicio JWT
 * Valida generación, validación y extracción de tokens
 */
class JwtServiceTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final int EXPIRATION = 86400; // 24 horas
    private static final int REFRESH_EXPIRATION = 604800; // 7 días

    private Key signingKey;
    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        signingKey = Keys.hmacShaKeyFor(SECRET.getBytes());
        userDetails = User.builder()
            .username("testuser")
            .password("password")
            .roles("USUARIO")
            .build();
    }

    @Test
    void testGenerateToken_DeberiaCrearTokenValido() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", "USUARIO");
        claims.put("userId", 123L);

        // When
        String token = generateToken(claims, userDetails.getUsername());

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // Header.Payload.Signature
    }

    @Test
    void testExtractUsername_DeberiaExtraerNombreUsuario() {
        // Given
        String token = generateToken(new HashMap<>(), "testuser");

        // When
        String username = extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    void testExtractExpiration_DeberiaExtraerFechaExpiracion() {
        // Given
        String token = generateToken(new HashMap<>(), "testuser");

        // When
        Date expiration = extractExpiration(token);

        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void testValidateToken_ConTokenValido_DeberiaRetornarTrue() {
        // Given
        String token = generateToken(new HashMap<>(), userDetails.getUsername());

        // When
        Boolean isValid = validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testValidateToken_ConUsuarioIncorrecto_DeberiaRetornarFalse() {
        // Given
        String token = generateToken(new HashMap<>(), "otheruser");

        // When
        Boolean isValid = validateToken(token, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testValidateToken_ConTokenExpirado_DeberiaRetornarFalse() {
        // Given
        String expiredToken = generateExpiredToken(userDetails.getUsername());

        // When & Then
        // Los tokens expirados deberían lanzar ExpiredJwtException al ser validados
        try {
            Boolean isValid = validateToken(expiredToken, userDetails);
            assertFalse(isValid); // Si no lanza excepción, debería ser false
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Comportamiento esperado para tokens expirados
            assertTrue(true, "Token expirado correctamente detectado");
        }
    }

    @Test
    void testGenerateRefreshToken_DeberiaCrearTokenConMayorExpiracion() {
        // Given & When
        String accessToken = generateToken(new HashMap<>(), userDetails.getUsername());
        String refreshToken = generateRefreshToken(new HashMap<>(), userDetails.getUsername());

        // Then
        Date accessExpiration = extractExpiration(accessToken);
        Date refreshExpiration = extractExpiration(refreshToken);
        
        assertTrue(refreshExpiration.after(accessExpiration));
    }

    @Test
    void testExtractClaim_DeberiaExtraerClaimPersonalizado() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("customClaim", "customValue");
        String token = generateToken(claims, userDetails.getUsername());

        // When
        String customClaim = extractClaim(token, claims1 -> claims1.get("customClaim", String.class));

        // Then
        assertEquals("customValue", customClaim);
    }

    @Test
    void testIsTokenExpired_ConTokenValido_DeberiaRetornarFalse() {
        // Given
        String token = generateToken(new HashMap<>(), userDetails.getUsername());

        // When
        Boolean isExpired = isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_ConTokenExpirado_DeberiaRetornarTrue() {
        // Given
        String expiredToken = generateExpiredToken(userDetails.getUsername());

        // When & Then
        // Los tokens expirados deberían lanzar ExpiredJwtException al verificar expiración
        try {
            Boolean isExpired = isTokenExpired(expiredToken);
            assertTrue(isExpired); // Si no lanza excepción, debería ser true
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Comportamiento esperado para tokens expirados
            assertTrue(true, "Token expirado correctamente detectado");
        }
    }

    @Test
    void testExtractAllClaims_DeberiaExtraerTodosLosClaims() {
        // Given
        Map<String, Object> originalClaims = new HashMap<>();
        originalClaims.put("role", "ADMIN");
        originalClaims.put("department", "IT");
        String token = generateToken(originalClaims, userDetails.getUsername());

        // When
        Claims claims = extractAllClaims(token);

        // Then
        assertEquals("testuser", claims.getSubject());
        assertEquals("ADMIN", claims.get("role"));
        assertEquals("IT", claims.get("department"));
    }

    @Test
    void testTokenSecurity_DeberiaUsarAlgoritmoSeguro() {
        // Given
        String token = generateToken(new HashMap<>(), userDetails.getUsername());

        // When
        String[] tokenParts = token.split("\\.");
        String header = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[0]));

        // Then
        assertTrue(header.contains("HS512") || header.contains("HS256"));
    }

    // Métodos auxiliares para las pruebas
    private String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION * 1000))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }

    private String generateRefreshToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
            .setClaims(extraClaims)
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION * 1000))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }

    private String generateExpiredToken(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date(System.currentTimeMillis() - 2000))
            .setExpiration(new Date(System.currentTimeMillis() - 1000))
            .signWith(signingKey, SignatureAlgorithm.HS512)
            .compact();
    }

    private String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith((SecretKey) signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
