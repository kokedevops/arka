package com.arka.arkavalenzuela.infrastructure.adapter.in.web.test;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Security Test Controller for testing security configurations
 * This controller provides test endpoints with different security requirements
 */
@RestController
@RequestMapping("/api")
public class SecurityTestController {

    @GetMapping("/public/health")
    public ResponseEntity<Map<String, String>> publicHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "arka-security-test");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('USUARIO') or hasRole('GESTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("users", "lista de usuarios");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/solicitudes")
    @PreAuthorize("hasRole('USUARIO') or hasRole('GESTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getSolicitudes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        // Validar parámetros
        if (page < 0 || size <= 0 || size > 100) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Parámetros inválidos: page debe ser >= 0, size debe ser 1-100");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("solicitudes", "lista de solicitudes");
        response.put("page", page);
        response.put("size", size);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/solicitudes")
    @PreAuthorize("hasRole('GESTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> createSolicitud(@RequestBody Map<String, Object> solicitud) {
        // Validar entrada
        String descripcion = (String) solicitud.get("descripcion");
        String estado = (String) solicitud.get("estado");
        String tipo = (String) solicitud.get("tipo");
        
        // Validar tamaño de request PRIMERO (simular límite de 5KB)
        if (tipo != null && tipo.length() > 5000) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Request demasiado grande");
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(error);
        }
        
        // Luego validar campos obligatorios
        if (descripcion == null || descripcion.trim().isEmpty() || 
            estado == null || estado.trim().isEmpty() ||
            tipo == null || tipo.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Campos obligatorios no pueden estar vacíos");
            return ResponseEntity.badRequest().body(error);
        }
        
        // Finalmente validar contenido malicioso
        if (containsMaliciousContent(descripcion) || containsMaliciousContent(estado) || containsMaliciousContent(tipo)) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Contenido malicioso detectado");
            return ResponseEntity.badRequest().body(error);
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("id", 1);
        response.put("descripcion", descripcion);
        response.put("estado", estado);
        response.put("tipo", tipo);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/solicitudes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSolicitud(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/solicitudes/mis-solicitudes")
    @PreAuthorize("hasRole('USUARIO') or hasRole('GESTOR') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMisSolicitudes() {
        Map<String, Object> response = new HashMap<>();
        response.put("solicitudes", "mis solicitudes");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminUsers() {
        Map<String, Object> response = new HashMap<>();
        response.put("adminUsers", "lista de usuarios admin");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getAdminConfig() {
        Map<String, Object> response = new HashMap<>();
        response.put("config", "configuración admin");
        return ResponseEntity.ok(response);
    }

    private boolean containsMaliciousContent(String content) {
        if (content == null) return false;
        
        String[] maliciousPatterns = {
            "<script", "javascript:", "eval(", "document.cookie",
            "SELECT ", "DROP ", "INSERT ", "UPDATE ", "DELETE ",
            "UNION ", "OR 1=1", "'; --", "xp_", "sp_"
        };
        
        String lowerContent = content.toLowerCase();
        for (String pattern : maliciousPatterns) {
            if (lowerContent.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }
}
