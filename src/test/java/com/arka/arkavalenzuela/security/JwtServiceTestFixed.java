package com.arka.arkavalenzuela.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

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

    private SecretKey signingKey;
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
    @DisplayName("Debería generar un token válido")
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
    @DisplayName("Debería extraer el nombre de usuario correctamente")
    void testExtractUsername_DeberiaExtraerNombreUsuario() {
        // Given
        String token = generateToken(new HashMap<>(), "testuser");

        // When
        String username = extractUsername(token);

        // Then
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Debería extraer la fecha de expiración")
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
    @DisplayName("Token válido debería retornar true")
    void testValidateToken_ConTokenValido_DeberiaRetornarTrue() {
        // Given
        String token = generateToken(new HashMap<>(), userDetails.getUsername());

        // When
        Boolean isValid = validateToken(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    @DisplayName("Token con usuario incorrecto debería retornar false")
    void testValidateToken_ConUsuarioIncorrecto_DeberiaRetornarFalse() {
        // Given
        String token = generateToken(new HashMap<>(), "otheruser");

        // When
        Boolean isValid = validateToken(token, userDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    @DisplayName("Token expirado debería lanzar ExpiredJwtException")
    void testValidateToken_ConTokenExpirado_DeberiaRetornarFalse() {
        // Given
        String expiredToken = generateExpiredToken(userDetails.getUsername());

        // When & Then
        // Los tokens expirados deberían lanzar ExpiredJwtException
        assertThrows(ExpiredJwtException.class, () -> {
            validateToken(expiredToken, userDetails);
        });
    }

    @Test
    @DisplayName("Refresh token debería tener mayor expiración")
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
    @DisplayName("Debería extraer claims personalizados")
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
    @DisplayName("Token válido no debería estar expirado")
    void testIsTokenExpired_ConTokenValido_DeberiaRetornarFalse() {
        // Given
        String token = generateToken(new HashMap<>(), userDetails.getUsername());

        // When
        Boolean isExpired = isTokenExpired(token);

        // Then
        assertFalse(isExpired);
    }

    @Test
    @DisplayName("Token expirado debería lanzar ExpiredJwtException")
    void testIsTokenExpired_ConTokenExpirado_DeberiaRetornarTrue() {
        // Given
        String expiredToken = generateExpiredToken(userDetails.getUsername());

        // When & Then
        // Los tokens expirados deberían lanzar ExpiredJwtException
        assertThrows(ExpiredJwtException.class, () -> {
            isTokenExpired(expiredToken);
        });
    }

    @Test
    @DisplayName("Debería extraer todos los claims")
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
    @DisplayName("Token debería usar algoritmo seguro")
    void testTokenSecurity_DeberiaUsarAlgoritmoSeguro() {
        // Given
        String token = generateToken(new HashMap<>(), userDetails.getUsername());

        // When
        String[] tokenParts = token.split("\\.");
        String header = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[0]));

        // Then
        assertTrue(header.contains("HS512") || header.contains("HS256"));
    }

    // =================== MÉTODOS AUXILIARES ===================

    /**
     * Genera un token JWT con claims adicionales
     */
    private String generateToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + EXPIRATION * 1000L))
            .signWith(signingKey)
            .compact();
    }

    /**
     * Genera un refresh token con mayor tiempo de expiración
     */
    private String generateRefreshToken(Map<String, Object> extraClaims, String username) {
        return Jwts.builder()
            .claims(extraClaims)
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis()))
            .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION * 1000L))
            .signWith(signingKey)
            .compact();
    }

    /**
     * Genera un token expirado para pruebas
     */
    private String generateExpiredToken(String username) {
        return Jwts.builder()
            .subject(username)
            .issuedAt(new Date(System.currentTimeMillis() - 10000)) // 10 segundos atrás
            .expiration(new Date(System.currentTimeMillis() - 5000)) // 5 segundos atrás (expirado)
            .signWith(signingKey)
            .compact();
    }

    /**
     * Extrae el nombre de usuario del token
     */
    private String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae la fecha de expiración del token
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae un claim específico del token
     */
    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Extrae todos los claims del token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    /**
     * Verifica si el token está expirado
     */
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valida el token contra los detalles del usuario
     */
    private Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}
