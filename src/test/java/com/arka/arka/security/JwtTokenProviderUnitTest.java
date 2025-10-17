package com.arka.arka.security;

import com.arka.arka.infrastructure.config.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtTokenProvider without Spring context
 */
class JwtTokenProviderUnitTest {

    private JwtTokenProvider jwtTokenProvider;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        
        // Set test values using reflection to avoid Spring injection
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", 
            "arkaSecretKeyForJWTGenerationThatMustBeLongEnoughForHS512Algorithm2025");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationMs", 604800000L); // 7 days

        authentication = new UsernamePasswordAuthenticationToken(
            "testuser",
            "password",
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }

    @Test
    void generateJwtToken_ShouldCreateValidToken() {
        // When
        String token = jwtTokenProvider.generateJwtToken(authentication);
        
        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertEquals(3, token.split("\\.").length); // Header.Payload.Signature
    }

    @Test
    void getUsernameFromJwtToken_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtTokenProvider.generateJwtToken(authentication);
        
        // When
        String username = jwtTokenProvider.getUsernameFromJwtToken(token);
        
        // Then
        assertEquals("testuser", username);
    }

    @Test
    void validateJwtToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtTokenProvider.generateJwtToken(authentication);
        
        // When
        boolean isValid = jwtTokenProvider.validateJwtToken(token);
        
        // Then
        assertTrue(isValid);
    }

    @Test
    void validateJwtToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "invalid.token.here";
        
        // When
        boolean isValid = jwtTokenProvider.validateJwtToken(invalidToken);
        
        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtTokenProvider.validateJwtToken(null);
        
        // Then
        assertFalse(isValid);
    }

    @Test
    void validateJwtToken_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean isValid = jwtTokenProvider.validateJwtToken("");
        
        // Then
        assertFalse(isValid);
    }

    @Test
    void generateRefreshToken_ShouldCreateValidRefreshToken() {
        // When
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertEquals(3, refreshToken.split("\\.").length);
    }

    @Test
    void refreshToken_ShouldHaveLongerExpirationThanAccessToken() {
        // Given
        String accessToken = jwtTokenProvider.generateJwtToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        // When
        Date accessExpiration = jwtTokenProvider.getExpirationFromJwtToken(accessToken);
        Date refreshExpiration = jwtTokenProvider.getExpirationFromJwtToken(refreshToken);
        
        // Then
        assertTrue(refreshExpiration.after(accessExpiration));
    }

    @Test
    void getExpirationFromJwtToken_ShouldReturnFutureDate() {
        // Given
        String token = jwtTokenProvider.generateJwtToken(authentication);
        
        // When
        Date expiration = jwtTokenProvider.getExpirationFromJwtToken(token);
        
        // Then
        assertNotNull(expiration);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void isRefreshToken_WithRefreshToken_ShouldReturnTrue() {
        // Given
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
        
        // When
        boolean isRefresh = jwtTokenProvider.isRefreshToken(refreshToken);
        
        // Then
        assertTrue(isRefresh);
    }

    @Test
    void isRefreshToken_WithAccessToken_ShouldReturnFalse() {
        // Given
        String accessToken = jwtTokenProvider.generateJwtToken(authentication);
        
        // When
        boolean isRefresh = jwtTokenProvider.isRefreshToken(accessToken);
        
        // Then
        assertFalse(isRefresh);
    }

    @Test
    void generateTokenFromUsername_ShouldCreateValidToken() {
        // When
        String token = jwtTokenProvider.generateTokenFromUsername("testuser");
        
        // Then
        assertNotNull(token);
        String username = jwtTokenProvider.getUsernameFromJwtToken(token);
        assertEquals("testuser", username);
    }

    @Test
    void getAuthoritiesFromJwtToken_ShouldReturnCorrectAuthorities() {
        // Given
        String token = jwtTokenProvider.generateJwtToken(authentication);
        
        // When
        List<String> authorities = jwtTokenProvider.getAuthoritiesFromJwtToken(token);
        
        // Then
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertTrue(authorities.contains("ROLE_USER"));
    }

    @Test
    void isTokenExpiringSoon_WithFreshToken_ShouldReturnFalse() {
        // Given
        String token = jwtTokenProvider.generateJwtToken(authentication);
        
        // When
        boolean isExpiringSoon = jwtTokenProvider.isTokenExpiringSoon(token);
        
        // Then
        assertFalse(isExpiringSoon);
    }

    @Test
    void getTokenRemainingTime_WithFreshToken_ShouldReturnPositiveValue() {
        // Given
        String token = jwtTokenProvider.generateJwtToken(authentication);
        
        // When
        long remainingTime = jwtTokenProvider.getTokenRemainingTime(token);
        
        // Then
        assertTrue(remainingTime > 0);
        // Should be close to 24 hours (86400 seconds)
        assertTrue(remainingTime > 86300); // Allow some margin for execution time
    }

    @Test
    void validateJwtToken_WithMalformedToken_ShouldReturnFalse() {
        // Given
        String malformedToken = "malformed-token-without-proper-structure";
        
        // When
        boolean isValid = jwtTokenProvider.validateJwtToken(malformedToken);
        
        // Then
        assertFalse(isValid);
    }

    @Test
    void generateJwtToken_WithMultipleAuthorities_ShouldIncludeAllAuthorities() {
        // Given
        Authentication multiRoleAuth = new UsernamePasswordAuthenticationToken(
            "admin",
            "password",
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("PERMISSION_READ")
            )
        );
        
        // When
        String token = jwtTokenProvider.generateJwtToken(multiRoleAuth);
        
        // Then
        List<String> authorities = jwtTokenProvider.getAuthoritiesFromJwtToken(token);
        
        assertEquals(3, authorities.size());
        assertTrue(authorities.contains("ROLE_ADMIN"));
        assertTrue(authorities.contains("ROLE_USER"));
        assertTrue(authorities.contains("PERMISSION_READ"));
    }

    @Test
    void getJwtExpirationMs_ShouldReturnConfiguredValue() {
        // When
        Long expiration = jwtTokenProvider.getJwtExpirationMs();
        
        // Then
        assertEquals(86400000L, expiration);
    }

    @Test
    void getRefreshTokenExpirationMs_ShouldReturnConfiguredValue() {
        // When
        Long refreshExpiration = jwtTokenProvider.getRefreshTokenExpirationMs();
        
        // Then
        assertEquals(604800000L, refreshExpiration);
    }

    @Test
    void getUsernameFromJwtToken_WithInvalidToken_ShouldReturnNull() {
        // Given
        String invalidToken = "invalid.token.structure";
        
        // When
        String username = jwtTokenProvider.getUsernameFromJwtToken(invalidToken);
        
        // Then
        assertNull(username);
    }

    @Test
    void getAuthoritiesFromJwtToken_WithInvalidToken_ShouldReturnEmptyList() {
        // Given
        String invalidToken = "invalid.token.structure";
        
        // When
        List<String> authorities = jwtTokenProvider.getAuthoritiesFromJwtToken(invalidToken);
        
        // Then
        assertNotNull(authorities);
        assertTrue(authorities.isEmpty());
    }

    @Test
    void getExpirationFromJwtToken_WithInvalidToken_ShouldReturnNull() {
        // Given
        String invalidToken = "invalid.token.structure";
        
        // When
        Date expiration = jwtTokenProvider.getExpirationFromJwtToken(invalidToken);
        
        // Then
        assertNull(expiration);
    }
}
