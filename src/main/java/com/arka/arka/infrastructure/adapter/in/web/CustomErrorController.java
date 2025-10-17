package com.arka.arka.infrastructure.adapter.in.web;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Controlador personalizado para manejo de errores
 */
@RestController
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        // Obtener el código de estado del error
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        HttpStatus httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        
        if (status != null) {
            Integer statusCode = Integer.valueOf(status.toString());
            httpStatus = HttpStatus.valueOf(statusCode);
        }
        
        // Crear respuesta de error
        response.put("error", true);
        response.put("status", httpStatus.value());
        response.put("message", getErrorMessage(httpStatus));
        response.put("timestamp", System.currentTimeMillis());
        response.put("application", "ARKA E-commerce Platform");
        
        // Agregar información adicional según el tipo de error
        switch (httpStatus) {
            case NOT_FOUND:
                response.put("suggestion", "Check the URL or visit / for available endpoints");
                break;
            case UNAUTHORIZED:
                response.put("suggestion", "Use HTTP Basic Auth with valid credentials");
                response.put("demoUsers", "admin/admin123, user/user123, demo/demo123");
                break;
            case FORBIDDEN:
                response.put("suggestion", "You don't have permission to access this resource");
                break;
            default:
                response.put("suggestion", "Contact system administrator if the problem persists");
        }
        
        return ResponseEntity.status(httpStatus).body(response);
    }
    
    private String getErrorMessage(HttpStatus status) {
        switch (status) {
            case NOT_FOUND:
                return "The requested resource was not found";
            case UNAUTHORIZED:
                return "Authentication required";
            case FORBIDDEN:
                return "Access denied";
            case INTERNAL_SERVER_ERROR:
                return "Internal server error";
            case BAD_REQUEST:
                return "Bad request";
            default:
                return "An error occurred: " + status.getReasonPhrase();
        }
    }
}
