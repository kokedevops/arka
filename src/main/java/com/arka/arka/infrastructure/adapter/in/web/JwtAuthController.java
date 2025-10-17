package com.arka.arka.infrastructure.adapter.in.web;

import com.arka.arka.infrastructure.adapter.in.web.dto.JwtResponse;
import com.arka.arka.infrastructure.adapter.in.web.dto.RefreshTokenRequest;
import com.arka.arka.infrastructure.config.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * Controlador de autenticación JWT para la aplicación principal ARKA
 */
@RestController
@RequestMapping("/api/auth")
public class JwtAuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * Endpoint para login con JWT
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        try {
            // Usar "identifier" o "username" como campo de usuario
            String username = request.getIdentifier() != null ? request.getIdentifier() : request.getUsername();
            
            if (username == null || request.getPassword() == null) {
                return ResponseEntity.badRequest()
                    .body(new JwtResponse(false, "Username/identifier and password are required"));
            }
            
            // Autenticar con Spring Security
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Generar tokens JWT
            String accessToken = jwtTokenProvider.generateJwtToken(authentication);
            String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);
            
            // Obtener authorities
            List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
            
            // Calcular tiempo de expiración en segundos
            Long expiresIn = jwtTokenProvider.getJwtExpirationMs() / 1000;
            Long refreshExpiresIn = jwtTokenProvider.getRefreshTokenExpirationMs() / 1000;
            
            JwtResponse jwtResponse = new JwtResponse(
                true, 
                "Login exitoso",
                accessToken,
                refreshToken,
                authentication.getName(),
                authorities,
                expiresIn,
                refreshExpiresIn
            );
            
            return ResponseEntity.ok(jwtResponse);
            
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401)
                .body(new JwtResponse(false, "Credenciales inválidas"));
        }
    }

    /**
     * Endpoint para refrescar token JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(new JwtResponse(false, "Refresh token es requerido"));
            }
            
            // Validar refresh token
            if (!jwtTokenProvider.validateJwtToken(refreshToken) || 
                !jwtTokenProvider.isRefreshToken(refreshToken)) {
                return ResponseEntity.status(401)
                    .body(new JwtResponse(false, "Refresh token inválido o expirado"));
            }
            
            // Obtener username del refresh token
            String username = jwtTokenProvider.getUsernameFromJwtToken(refreshToken);
            
            if (username == null) {
                return ResponseEntity.status(401)
                    .body(new JwtResponse(false, "Refresh token inválido"));
            }
            
            // Generar nuevos tokens
            String newAccessToken = jwtTokenProvider.generateTokenFromUsername(username);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(
                new UsernamePasswordAuthenticationToken(username, null)
            );
            
            // Calcular tiempo de expiración
            Long expiresIn = jwtTokenProvider.getJwtExpirationMs() / 1000;
            Long refreshExpiresIn = jwtTokenProvider.getRefreshTokenExpirationMs() / 1000;
            
            JwtResponse jwtResponse = new JwtResponse(
                true,
                "Token refrescado exitosamente",
                newAccessToken,
                newRefreshToken,
                username,
                null, // Las authorities se obtienen del token en el filtro
                expiresIn,
                refreshExpiresIn
            );
            
            return ResponseEntity.ok(jwtResponse);
            
        } catch (Exception e) {
            return ResponseEntity.status(401)
                .body(new JwtResponse(false, "Error al refrescar token"));
        }
    }

    /**
     * Endpoint para validar token
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String token = request.get("token");
            
            if (token == null || token.trim().isEmpty()) {
                response.put("valid", false);
                response.put("message", "Token es requerido");
                return ResponseEntity.badRequest().body(response);
            }
            
            boolean isValid = jwtTokenProvider.validateJwtToken(token);
            
            if (isValid) {
                String username = jwtTokenProvider.getUsernameFromJwtToken(token);
                List<String> authorities = jwtTokenProvider.getAuthoritiesFromJwtToken(token);
                long remainingTime = jwtTokenProvider.getTokenRemainingTime(token);
                boolean expiringSoon = jwtTokenProvider.isTokenExpiringSoon(token);
                
                response.put("valid", true);
                response.put("username", username);
                response.put("authorities", authorities);
                response.put("remainingTime", remainingTime);
                response.put("expiringSoon", expiringSoon);
                response.put("message", "Token válido");
            } else {
                response.put("valid", false);
                response.put("message", "Token inválido o expirado");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Error al validar token");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para verificar el estado de autenticación
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getAuthStatus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> response = new HashMap<>();
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            response.put("authenticated", true);
            response.put("username", auth.getName());
            response.put("authorities", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        } else {
            response.put("authenticated", false);
            response.put("username", null);
            response.put("authorities", null);
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para administradores solamente
     */
    @GetMapping("/admin/info")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Información solo para administradores");
        response.put("timestamp", System.currentTimeMillis());
        response.put("user", SecurityContextHolder.getContext().getAuthentication().getName());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint público para obtener información de usuarios demo
     */
    @GetMapping("/demo-users")
    public ResponseEntity<Map<String, Object>> getDemoUsers() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, String> users = new HashMap<>();
        users.put("admin", "admin123 (ADMIN, USER roles)");
        users.put("user", "user123 (USER role)");
        users.put("demo", "demo123 (USER role)");
        
        response.put("demoUsers", users);
        response.put("loginEndpoint", "POST /api/auth/login");
        response.put("refreshEndpoint", "POST /api/auth/refresh");
        response.put("validateEndpoint", "POST /api/auth/validate");
        response.put("instruction", "POST /api/auth/login with JSON: {\"identifier\":\"admin\", \"password\":\"admin123\"}");
        response.put("jwtInfo", "Response includes accessToken and refreshToken");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Información sobre cómo hacer login (GET request)
     */
    @GetMapping("/login")
    public ResponseEntity<Map<String, Object>> loginInfo() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("message", "JWT Authentication endpoint information");
        response.put("method", "POST");
        response.put("endpoint", "/api/auth/login");
        response.put("contentType", "application/json");
        
        Map<String, Object> jsonExample = new HashMap<>();
        jsonExample.put("identifier", "admin");
        jsonExample.put("password", "admin123");
        
        response.put("jsonBody", jsonExample);
        
        Map<String, String> responseExample = new HashMap<>();
        responseExample.put("accessToken", "eyJhbGciOiJIUzUxMiJ9...");
        responseExample.put("refreshToken", "eyJhbGciOiJIUzUxMiJ9...");
        responseExample.put("tokenType", "Bearer");
        responseExample.put("expiresIn", "86400 (seconds)");
        
        response.put("responseExample", responseExample);
        
        Map<String, String> usage = new HashMap<>();
        usage.put("authorization", "Authorization: Bearer <accessToken>");
        usage.put("refresh", "POST /api/auth/refresh with refreshToken");
        usage.put("validate", "POST /api/auth/validate with token");
        
        response.put("usage", usage);
        
        return ResponseEntity.ok(response);
    }

    /**
     * Clase para el request de login
     */
    public static class LoginRequest {
        private String identifier;
        private String username;
        private String password;
        
        // Getters y Setters
        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
