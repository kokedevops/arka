-- Schema DDL para ARKA Security
-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    nombre_completo VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL DEFAULT 'USUARIO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_ultimo_acceso TIMESTAMP NULL
);

-- Índices para la tabla usuarios
CREATE INDEX IF NOT EXISTS idx_username ON usuarios(username);
CREATE INDEX IF NOT EXISTS idx_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_rol ON usuarios(rol);
CREATE INDEX IF NOT EXISTS idx_activo ON usuarios(activo);

-- Tabla de refresh tokens
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    usuario_id BIGINT NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    ip_address VARCHAR(45) NULL,
    user_agent VARCHAR(500) NULL
);

-- Índices para la tabla refresh_tokens
CREATE INDEX IF NOT EXISTS idx_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_usuario_id ON refresh_tokens(usuario_id);
CREATE INDEX IF NOT EXISTS idx_refresh_activo ON refresh_tokens(activo);
CREATE INDEX IF NOT EXISTS idx_expiracion ON refresh_tokens(fecha_expiracion);

-- Clave foránea para refresh_tokens
ALTER TABLE refresh_tokens ADD CONSTRAINT IF NOT EXISTS fk_refresh_usuario 
FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE;

-- Datos de prueba - Usuario administrador por defecto
MERGE INTO usuarios (username, email, password, nombre_completo, rol, activo) 
KEY(username)
VALUES (
    'admin', 
    'admin@arka.com', 
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeGNeLGUWpHKuS.i2', -- password: admin123
    'Administrador del Sistema', 
    'ADMINISTRADOR', 
    TRUE
);

-- Usuario gestor de prueba
MERGE INTO usuarios (username, email, password, nombre_completo, rol, activo) 
KEY(username)
VALUES (
    'gestor', 
    'gestor@arka.com', 
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeGNeLGUWpHKuS.i2', -- password: admin123
    'Gestor de Operaciones', 
    'GESTOR', 
    TRUE
);

-- Usuario operador de prueba
MERGE INTO usuarios (username, email, password, nombre_completo, rol, activo) 
KEY(username)
VALUES (
    'operador', 
    'operador@arka.com', 
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeGNeLGUWpHKuS.i2', -- password: admin123
    'Operador de Cálculos', 
    'OPERADOR', 
    TRUE
);

-- Usuario regular de prueba
MERGE INTO usuarios (username, email, password, nombre_completo, rol, activo) 
KEY(username)
VALUES (
    'usuario', 
    'usuario@arka.com', 
    '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeGNeLGUWpHKuS.i2', -- password: admin123
    'Usuario Regular', 
    'USUARIO', 
    TRUE
);
