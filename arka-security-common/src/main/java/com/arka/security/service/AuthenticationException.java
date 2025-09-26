package com.arka.security.service;

/**
 * Excepción personalizada para errores de autenticación y autorización.
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
}
