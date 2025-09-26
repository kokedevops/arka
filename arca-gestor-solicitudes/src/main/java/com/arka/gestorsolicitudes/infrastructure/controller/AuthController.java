package com.arka.gestorsolicitudes.infrastructure.controller;

import com.arka.security.dto.AuthRequest;
import com.arka.security.dto.AuthResponse;
import com.arka.security.dto.RefreshTokenRequest;
import com.arka.security.dto.RegisterRequest;
import com.arka.security.service.AuthService;
import com.arka.security.service.AuthenticationException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para autenticación y autorización
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    /**
     * Registra un nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.badRequest().body(AuthResponse.builder().build());
        }
    }
    
    /**
     * Autentica un usuario
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        try {
            AuthResponse response = authService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder().build());
        }
    }
    
    /**
     * Refresca el token de acceso
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            AuthResponse response = authService.refreshToken(request);
            return ResponseEntity.ok(response);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(AuthResponse.builder().build());
        }
    }
    
    /**
     * Cierra sesión (revoca refresh token)
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            authService.logout(request.getRefreshToken());
            return ResponseEntity.ok("Sesión cerrada exitosamente");
        } catch (AuthenticationException ex) {
            return ResponseEntity.badRequest().body("Error al cerrar sesión");
        }
    }
    
    /**
     * Cierra todas las sesiones de un usuario (requiere estar autenticado)
     */
    @PostMapping("/logout-all")
    public ResponseEntity<String> logoutAll(@RequestHeader("X-User-Id") Long userId) {
        try {
            authService.logoutAll(userId);
            return ResponseEntity.ok("Todas las sesiones cerradas exitosamente");
        } catch (AuthenticationException ex) {
            return ResponseEntity.badRequest().body("Error al cerrar sesiones");
        }
    }
    
    /**
     * Valida el token actual (endpoint de utilidad)
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }
        
        // El filtro JWT ya validó el token si llegamos aquí
        return ResponseEntity.ok("Token válido");
    }
    
    /**
     * Obtiene información del usuario actual
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> getCurrentUser(
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader("X-User-Name") String username,
            @RequestHeader("X-User-Role") String role) {
        
        // Construir información del usuario desde los headers
        AuthResponse.UserInfo userInfo = AuthResponse.UserInfo.builder()
                .id(userId)
                .username(username)
                .rol(role)
                .build();
        
        return ResponseEntity.ok(userInfo);
    }
}
