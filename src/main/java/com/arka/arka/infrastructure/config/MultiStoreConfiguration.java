package com.arka.arka.infrastructure.config;

import org.springframework.context.annotation.Configuration;

/**
 * Configuración para múltiples stores de datos
 * - JPA para MySQL (RDS) - configurado en JpaConfiguration
 * - MongoDB para DocumentDB - configurado en MongoConfiguration
 * - R2DBC para acceso reactivo a MySQL - deshabilitado por defecto
 */
@Configuration
public class MultiStoreConfiguration {
}
