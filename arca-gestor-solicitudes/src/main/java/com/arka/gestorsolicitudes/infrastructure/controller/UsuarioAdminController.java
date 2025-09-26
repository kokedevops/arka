package com.arka.gestorsolicitudes.infrastructure.controller;

import com.arka.security.domain.model.Usuario;
import com.arka.security.domain.repository.UsuarioRepository;
import com.arka.security.dto.RegisterRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador para gestión de usuarios (solo administradores)
 */
@RestController
@RequestMapping("/api/admin/usuarios")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UsuarioAdminController {
    
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioAdminController(UsuarioRepository usuarioRepository,
                                  PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Listar todos los usuarios (solo administradores)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAllActive();
    }
    
    /**
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
    
    /**
     * Crear usuario con rol específico (solo administradores)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> crearUsuario(@RequestBody CreateUserRequest request) {
        Usuario usuario = new Usuario(
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                request.getNombreCompleto(),
                request.getRol()
        );
        
        if (usuarioRepository.existsByUsername(request.getUsername()) ||
            usuarioRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().build();
        }

        Usuario savedUser = usuarioRepository.save(usuario);
        return ResponseEntity.ok(savedUser);
    }
    
    /**
     * Actualizar rol de usuario
     */
    @PutMapping("/{id}/rol")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> actualizarRol(@PathVariable Long id, @RequestBody RolUpdateRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setRol(request.getRol());
        Usuario actualizado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(actualizado);
    }
    
    /**
     * Activar/Desactivar usuario
     */
    @PutMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Usuario> cambiarEstado(@PathVariable Long id, @RequestBody EstadoUpdateRequest request) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setActivo(request.isActivo());
        Usuario actualizado = usuarioRepository.save(usuario);
        return ResponseEntity.ok(actualizado);
    }
    
    /**
     * Eliminar usuario (desactivar)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Usuario usuario = usuarioOpt.get();
        usuario.desactivar();
        usuarioRepository.save(usuario);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Obtener estadísticas de usuarios
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<UserStats> obtenerEstadisticas() {
        long totalActivos = usuarioRepository.countActiveUsers();
        long administradores = usuarioRepository.findByRolAndActivoTrue(Usuario.Rol.ADMINISTRADOR).size();
        long gestores = usuarioRepository.findByRolAndActivoTrue(Usuario.Rol.GESTOR).size();
        long operadores = usuarioRepository.findByRolAndActivoTrue(Usuario.Rol.OPERADOR).size();
        long usuarios = usuarioRepository.findByRolAndActivoTrue(Usuario.Rol.USUARIO).size();

        UserStats stats = new UserStats();
        stats.setTotalActivos(totalActivos);
        stats.setAdministradores(administradores);
        stats.setGestores(gestores);
        stats.setOperadores(operadores);
        stats.setUsuarios(usuarios);

        return ResponseEntity.ok(stats);
    }
    
    // DTOs internos
    public static class CreateUserRequest extends RegisterRequest {
        private Usuario.Rol rol;
        
        public Usuario.Rol getRol() {
            return rol;
        }
        
        public void setRol(Usuario.Rol rol) {
            this.rol = rol;
        }
    }
    
    public static class RolUpdateRequest {
        private Usuario.Rol rol;
        
        public Usuario.Rol getRol() {
            return rol;
        }
        
        public void setRol(Usuario.Rol rol) {
            this.rol = rol;
        }
    }
    
    public static class EstadoUpdateRequest {
        private boolean activo;
        
        public boolean isActivo() {
            return activo;
        }
        
        public void setActivo(boolean activo) {
            this.activo = activo;
        }
    }
    
    public static class UserStats {
        private Long totalActivos;
        private Long administradores;
        private Long gestores;
        private Long operadores;
        private Long usuarios;
        
        // Getters y setters
        public Long getTotalActivos() { return totalActivos; }
        public void setTotalActivos(Long totalActivos) { this.totalActivos = totalActivos; }
        
        public Long getAdministradores() { return administradores; }
        public void setAdministradores(Long administradores) { this.administradores = administradores; }
        
        public Long getGestores() { return gestores; }
        public void setGestores(Long gestores) { this.gestores = gestores; }
        
        public Long getOperadores() { return operadores; }
        public void setOperadores(Long operadores) { this.operadores = operadores; }
        
        public Long getUsuarios() { return usuarios; }
        public void setUsuarios(Long usuarios) { this.usuarios = usuarios; }
    }
}
