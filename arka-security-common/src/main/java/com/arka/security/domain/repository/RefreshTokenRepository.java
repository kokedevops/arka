package com.arka.security.domain.repository;

import com.arka.security.domain.model.RefreshToken;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio reactivo para RefreshToken
 */
@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Query("SELECT * FROM refresh_tokens WHERE usuario_id = :usuarioId AND activo = true ORDER BY fecha_creacion DESC")
    List<RefreshToken> findActiveTokensByUsuarioId(Long usuarioId);

    @Modifying
    @Query("UPDATE refresh_tokens SET activo = false WHERE usuario_id = :usuarioId")
    void revokeAllTokensByUsuarioId(Long usuarioId);

    @Modifying
    @Query("DELETE FROM refresh_tokens WHERE fecha_expiracion < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();

    Optional<RefreshToken> findFirstByTokenAndUsuarioIdAndActivoTrueAndFechaExpiracionAfter(String token, Long usuarioId, LocalDateTime fecha);

    long countByUsuarioIdAndActivoTrue(Long usuarioId);

    List<RefreshToken> findByIpAddressAndActivoTrue(String ipAddress);
}
