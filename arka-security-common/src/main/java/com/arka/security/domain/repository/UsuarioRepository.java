package com.arka.security.domain.repository;

import com.arka.security.domain.model.Usuario;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio reactivo para Usuario
 */
@Repository
public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<Usuario> findByRolAndActivoTrue(Usuario.Rol rol);

    @Query("SELECT COUNT(*) FROM usuarios WHERE activo = true")
    long countActiveUsers();

    @Query("SELECT * FROM usuarios WHERE activo = true ORDER BY fecha_creacion DESC")
    List<Usuario> findAllActive();

    @Modifying
    @Query("UPDATE usuarios SET fecha_ultimo_acceso = CURRENT_TIMESTAMP WHERE id = :id")
    void updateLastAccess(Long id);
}
