# Arka Microservices Platform

Plataforma de microservicios basada en Spring Boot 3.2.3 y Spring Cloud para la gestión de cotizaciones y solicitudes.

## 🚀 Inicio Rápido

### Prerrequisitos
- Java 21 (Amazon Corretto recomendado)
- MySQL 8.0+
- Gradle 8.5+

### Configuración de Base de Datos
```sql
CREATE DATABASE arkabd CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'arka_user'@'localhost' IDENTIFIED BY 'arka_pass123';
GRANT ALL PRIVILEGES ON arkabd.* TO 'arka_user'@'localhost';
FLUSH PRIVILEGES;
```

### Despliegue con Gradle bootRun
```powershell
# Iniciar todos los servicios
.\scripts\deployment\start-all-services.ps1

# O manualmente uno por uno:
.\gradlew.bat :eureka-server:bootRun &
.\gradlew.bat :config-server:bootRun &  
.\gradlew.bat :api-gateway:bootRun &
.\gradlew.bat :arca-cotizador:bootRun &
.\gradlew.bat :arca-gestor-solicitudes:bootRun &
```

## 🏗️ Arquitectura

### Microservicios
- **eureka-server** (8761): Registro y descubrimiento de servicios
- **config-server** (8888): Configuración centralizada
- **api-gateway** (8080): Gateway y enrutamiento ✅ **FUNCIONAL**
- **arca-cotizador** (8081): Servicio de cotizaciones
- **arca-gestor-solicitudes** (8082): Gestión de solicitudes

### Módulos Comunes
- **arka-security-common**: Componentes de seguridad compartidos

## 📁 Estructura del Proyecto

```
arka-2/
├── api-gateway/           # API Gateway (Spring Cloud Gateway)
├── arca-cotizador/        # Microservicio de Cotizaciones  
├── arca-gestor-solicitudes/ # Microservicio Gestor de Solicitudes
├── arka-security-common/   # Módulo de Seguridad Común
├── config-server/         # Servidor de Configuración
├── eureka-server/         # Servidor de Registry
├── hello-world-service/   # Servicio de Prueba
├── config-repository/     # Configuraciones centralizadas
├── docs/                  # Documentación
│   ├── deployment/        # Guías de despliegue
│   └── guides/           # Guías técnicas
├── scripts/              # Scripts de automatización
│   └── deployment/       # Scripts de despliegue
├── infrastructure/       # IaC y configuraciones
├── k8s/                 # Manifiestos Kubernetes
└── monitoring/          # Configuración de monitoreo
```

## 🛠️ Scripts de Despliegue

### Scripts PowerShell (Windows)
- `scripts\deployment\start-all-services.ps1` - Iniciar todos los servicios
- `scripts\deployment\stop-all-services.ps1` - Detener todos los servicios  
- `scripts\deployment\check-services-health.ps1` - Verificar estado de servicios

### Scripts Shell (Linux/Mac)
- `scripts\deployment\deploy-docker.sh` - Despliegue con Docker
- `scripts\deployment\docker-manager.sh` - Gestión de contenedores

## 📖 Documentación

### Despliegue
- [Guía de Despliegue Docker](docs/deployment/DOCKER-DEPLOYMENT-GUIDE.md)
- [Pipeline Gradle](docs/deployment/GRADLE-BUILD-PIPELINE.md) 
- [CI/CD Pipelines](docs/deployment/CICD-PIPELINES.md)

### Guías Técnicas
- [Guía de Ejecución Completa](docs/guides/GUIA_EJECUCION_COMPLETA.md)
- [Configuración Spring Cloud](docs/guides/SPRING-CLOUD-CONFIG.md)
- [Seguridad JWT](docs/guides/SPRING-SECURITY-JWT.md)

## 🔧 Configuración

### Perfiles de Aplicación
- `dev`: Desarrollo local con H2/MySQL
- `aws`: Producción en AWS
- `local`: Desarrollo local básico

### Puertos por Defecto
- API Gateway: 8080
- Eureka Server: 8761  
- Config Server: 8888
- Cotizador: 8081
- Gestor Solicitudes: 8082

## ✅ Estado del Proyecto

### ✅ Completado
- [x] Configuración base de microservicios
- [x] API Gateway funcional
- [x] Integración Eureka y Config Server
- [x] Resolución de conflictos Spring Security
- [x] Scripts de despliegue automatizado
- [x] Organización de documentación

### 🔄 En Progreso
- [ ] Testing completo de integración
- [ ] Monitoreo y observabilidad
- [ ] Configuración de producción

## 🐛 Resolución de Problemas

### Error: "Unknown database 'arka-base'"
- Verificar configuración de base de datos en application.properties
- Usar perfil correcto: `--spring.profiles.active=dev`

### Error: Bean definition conflicts
- ✅ **RESUELTO**: Se eliminó dependencia conflictiva de arka-security-common en API Gateway

### Servicios no se comunican
- Verificar que Eureka Server esté corriendo en puerto 8761
- Revisar logs de registro de servicios

## 🚀 Próximos Pasos

1. **Testing**: Implementar tests de integración completos
2. **Monitoreo**: Configurar Micrometer y Prometheus  
3. **Seguridad**: Implementar JWT completo en todos los servicios
4. **CI/CD**: Completar pipelines de Jenkins/GitHub Actions
5. **Documentación**: API documentation con OpenAPI/Swagger

---

**Contacto**: kokedevops  
**Versión**: 1.0.0  
**Última actualización**: Septiembre 2025