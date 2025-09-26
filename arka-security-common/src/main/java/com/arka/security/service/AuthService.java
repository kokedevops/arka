package com.arka.security.service;

import com.arka.security.domain.model.RefreshToken;
import com.arka.security.domain.model.Usuario;
import com.arka.security.domain.repository.RefreshTokenRepository;
import com.arka.security.domain.repository.UsuarioRepository;
import com.arka.security.dto.AuthRequest;
import com.arka.security.dto.AuthResponse;
import com.arka.security.dto.RefreshTokenRequest;
import com.arka.security.dto.RegisterRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Servicio de autenticación y autorización
 */
@Service
public class AuthService {
    
    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Registra un nuevo usuario
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new AuthenticationException("El username ya existe");
        }

        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new AuthenticationException("El email ya está registrado");
        }

        Usuario usuario = new Usuario(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNombreCompleto(),
                Usuario.Rol.USUARIO
        );

        Usuario saved = usuarioRepository.save(usuario);
        return generateAuthResponse(saved);
    }
    
    /**
     * Autentica un usuario
     */
    @Transactional
    public AuthResponse authenticate(AuthRequest request) {
        Usuario usuario = findByUsernameOrEmail(request.getIdentifier())
                .orElseThrow(() -> new AuthenticationException("Usuario no encontrado"));

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPassword())) {
            throw new AuthenticationException("Credenciales incorrectas");
        }

        if (!usuario.isActivo()) {
            throw new AuthenticationException("Usuario inactivo");
        }

        usuario.actualizarUltimoAcceso();
        usuarioRepository.save(usuario);
        return generateAuthResponse(usuario);
    }
    
    /**
     * Refresca el token usando refresh token
     */
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new AuthenticationException("Refresh token no válido"));

        if (!refreshToken.esValido()) {
            throw new AuthenticationException("Refresh token expirado o inválido");
        }

        Usuario usuario = usuarioRepository.findById(refreshToken.getUsuarioId())
                .orElseThrow(() -> new AuthenticationException("Usuario no encontrado"));

        if (!usuario.isActivo()) {
            throw new AuthenticationException("Usuario inactivo");
        }

        String newAccessToken = jwtService.generateToken(usuario);
        String newRefreshToken = jwtService.generateRefreshToken(usuario);

        refreshToken.revocar();
        refreshTokenRepository.save(refreshToken);

        RefreshToken newRefreshTokenEntity = RefreshToken.crear(usuario.getId(), 7);
        newRefreshTokenEntity.setToken(newRefreshToken);
        refreshTokenRepository.save(newRefreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .usuario(toUserInfo(usuario))
                .build();
    }

    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    token.revocar();
                    refreshTokenRepository.save(token);
                });
    }

    @Transactional
    public void logoutAll(Long usuarioId) {
        refreshTokenRepository.revokeAllTokensByUsuarioId(usuarioId);
    }

    private AuthResponse generateAuthResponse(Usuario usuario) {
        String accessToken = jwtService.generateToken(usuario);
        String refreshToken = jwtService.generateRefreshToken(usuario);

        RefreshToken refreshTokenEntity = RefreshToken.crear(usuario.getId(), 7);
        refreshTokenEntity.setToken(refreshToken);
        refreshTokenRepository.save(refreshTokenEntity);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .usuario(toUserInfo(usuario))
                .build();
    }

    private Optional<Usuario> findByUsernameOrEmail(String identifier) {
        Optional<Usuario> byUsername = usuarioRepository.findByUsername(identifier);
        return byUsername.isPresent() ? byUsername : usuarioRepository.findByEmail(identifier);
    }
    
    /**
     * Convierte Usuario a información pública
     */
    private AuthResponse.UserInfo toUserInfo(Usuario usuario) {
        return AuthResponse.UserInfo.builder()
                .id(usuario.getId())
                .username(usuario.getUsername())
                .email(usuario.getEmail())
                .nombreCompleto(usuario.getNombreCompleto())
                .rol(usuario.getRol().name())
                .rolDescripcion(usuario.getRol().getDescripcion())
                .permisos(usuario.getRol().getPermisos())
                .activo(usuario.isActivo())
                .fechaCreacion(usuario.getFechaCreacion())
                .fechaUltimoAcceso(usuario.getFechaUltimoAcceso())
                .build();
    }
    
    public boolean validateToken(String token, Usuario usuario) {
        return jwtService.isTokenValid(token, usuario);
    }

    public Optional<Usuario> getUserFromToken(String token) {
        String username = jwtService.extractUsername(token);
        return usuarioRepository.findByUsername(username);
    }

    @Transactional
    public void cleanupExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }
}
