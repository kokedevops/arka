package com.arka.arka.infrastructure.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request para refrescar token JWT
 */
public class RefreshTokenRequest {
    
    @JsonProperty("refreshToken")
    private String refreshToken;

    // Constructor por defecto
    public RefreshTokenRequest() {}

    // Constructor con parámetros
    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    // Getters y Setters
    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
