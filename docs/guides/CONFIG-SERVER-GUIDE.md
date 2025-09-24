# 🏗️ ARKA CONFIG SERVER - GUÍA COMPLETA

<div align="center">
  <img src="https://img.shields.io/badge/Config%20Server-Spring%20Cloud-brightgreen" alt="Config Server"/>
  <img src="https://img.shields.io/badge/Centralized-Configuration-blue" alt="Centralized"/>
  <img src="https://img.shields.io/badge/Microservices-Ready-orange" alt="Microservices"/>
</div>

---

## 📋 **ÍNDICE**

- [🎯 ¿Qué es el Config Server?](#-qué-es-el-config-server)
- [🏗️ Implementación en ARKA](#️-implementación-en-arka)
- [🚀 Cómo Iniciar](#-cómo-iniciar)
- [🔧 Configuración](#-configuración)
- [📊 Testing y Validación](#-testing-y-validación)
- [🌐 Integration con Microservicios](#-integration-con-microservicios)
- [🛠️ Troubleshooting](#️-troubleshooting)

---

## 🎯 **¿QUÉ ES EL CONFIG SERVER?**

El **Config Server** es un componente centralizado de Spring Cloud que permite:

### ✅ **Beneficios**
- **📁 Configuración centralizada** - Un solo lugar para todas las configuraciones
- **🔄 Refresh dinámico** - Cambios sin reiniciar servicios
- **🌍 Profiles por ambiente** - dev, test, prod separados
- **🔐 Seguridad** - Configuraciones sensibles protegidas
- **📊 Versionado** - Historial de cambios en configuraciones
- **🚀 Escalabilidad** - Fácil gestión de múltiples microservicios

### 🏗️ **Arquitectura**
```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Config Repo   │    │   Config Server  │    │  Microservices  │
│  (File/Git)     │◄──►│  (Port: 8888)    │◄──►│   (Clients)     │
│                 │    │                  │    │                 │
│ - application   │    │ - Authentication │    │ - Auto-refresh  │
│ - profiles      │    │ - Health checks  │    │ - Fallback      │
│ - service cfg   │    │ - Eureka client  │    │ - Encryption    │
└─────────────────┘    └──────────────────┘    └─────────────────┘
```

---

## 🏗️ **IMPLEMENTACIÓN EN ARKA**

### 📁 **Estructura del Proyecto**
```
arkajvalenzuela/
├── config-server/                  # 🏗️ Config Server Module
│   ├── src/main/java/
│   │   └── com/arkavalenzuela/configserver/
│   │       ├── ConfigServerApplication.java
│   │       └── config/SecurityConfig.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── bootstrap.yml
│   ├── build.gradle
│   └── Dockerfile
├── config-repository/              # 📂 Configuration Files
│   ├── application.yml             # 🌐 Default config
│   ├── config-server-dev.yml       # 🏗️ Config Server config
│   ├── eureka-server-dev.yml       # 🔍 Eureka config
│   ├── api-gateway-dev.yml         # 🌐 Gateway config
│   ├── arca-cotizador-dev.yml      # 💰 Cotizador config
│   └── arca-gestor-solicitudes-dev.yml # 📋 Gestor config
└── scripts/                        # 🛠️ Automation scripts
    ├── start-config-server.sh
    ├── start-config-server.bat
    ├── test-config-server.sh
    └── start-with-config-server.sh
```

### 🔧 **Características Implementadas**
- ✅ **Native file system** support (config-repository/)
- ✅ **Git repository** support (backup option)
- ✅ **Eureka integration** para service discovery
- ✅ **Security básica** con user/password
- ✅ **Health checks** y monitoring
- ✅ **Docker containerization**
- ✅ **Multi-profile** support (dev, prod, test)

---

## 🚀 **CÓMO INICIAR**

### ⚡ **Opción 1: Inicio Rápido Individual**

#### **Windows:**
```bash
# 1. Compilar
gradlew.bat :config-server:build

# 2. Iniciar
scripts\start-config-server.bat

# 3. Verificar
scripts\test-config-server.bat
```

#### **Linux/Mac:**
```bash
# 1. Compilar
./gradlew :config-server:build

# 2. Iniciar
./scripts/start-config-server.sh

# 3. Verificar
./scripts/test-config-server.sh
```

### 🌟 **Opción 2: Plataforma Completa con Config Server**

#### **Windows:**
```bash
scripts\start-with-config-server.bat
```

#### **Linux/Mac:**
```bash
./scripts/start-with-config-server.sh
```

### 🐳 **Opción 3: Docker Compose**
```bash
# Iniciar todo con Docker
docker-compose up config-server eureka-server

# Verificar
curl http://localhost:8888/actuator/health
```

---

## 🔧 **CONFIGURACIÓN**

### 📊 **URLs del Config Server**

| 🎯 Endpoint | 📝 Descripción | 🔗 URL |
|-------------|----------------|---------|
| **Health Check** | Estado del servicio | http://localhost:8888/actuator/health |
| **Application Info** | Información de la app | http://localhost:8888/actuator/info |
| **Default Config** | Configuración por defecto | http://localhost:8888/application/default |
| **Service Config** | Config específica del servicio | http://localhost:8888/{service}/{profile} |
| **Environment** | Variables de entorno | http://localhost:8888/actuator/env |
| **Refresh** | Refrescar configuración | http://localhost:8888/actuator/refresh |

### 📂 **Configuraciones Disponibles**

#### **🌐 application.yml** (Configuración global)
```yaml
# Configuración compartida para todos los microservicios
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      connection-timeout: 20000
      maximum-pool-size: 10

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

security:
  jwt:
    secret: 404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
    expiration: 86400000
```

#### **🏗️ config-server-dev.yml** (Config Server específico)
```yaml
server:
  port: 8888

spring:
  application:
    name: config-server
  cloud:
    config:
      server:
        native:
          search-locations: file:../config-repository

security:
  user:
    name: config-admin
    password: config-secret
```

#### **🔍 eureka-server-dev.yml** (Eureka específico)
```yaml
server:
  port: 8761

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
  server:
    enable-self-preservation: false
```

### 🔐 **Seguridad**

#### **Autenticación Básica**
- **Usuario**: `config-admin`
- **Password**: `config-secret`

#### **Uso con curl:**
```bash
# Con autenticación
curl -u config-admin:config-secret \
  http://localhost:8888/application/default

# Sin autenticación (desarrollo)
curl http://localhost:8888/application/default
```

---

## 📊 **TESTING Y VALIDACIÓN**

### 🧪 **Tests Automatizados**

#### **Ejecutar tests completos:**
```bash
# Windows
scripts\test-config-server.bat

# Linux/Mac
./scripts/test-config-server.sh
```

#### **Tests manuales:**
```bash
# 1. Health Check
curl http://localhost:8888/actuator/health

# 2. Configuración por defecto
curl http://localhost:8888/application/default

# 3. Configuración específica
curl http://localhost:8888/eureka-server/dev
curl http://localhost:8888/api-gateway/dev
curl http://localhost:8888/arca-cotizador/dev

# 4. Información de la aplicación
curl http://localhost:8888/actuator/info

# 5. Environment
curl http://localhost:8888/actuator/env
```

### ✅ **Respuestas Esperadas**

#### **Health Check (200 OK):**
```json
{
  "status": "UP",
  "components": {
    "configServer": {
      "status": "UP",
      "details": {
        "repository": "file:../config-repository"
      }
    },
    "diskSpace": {
      "status": "UP"
    },
    "ping": {
      "status": "UP"
    }
  }
}
```

#### **Default Configuration (200 OK):**
```json
{
  "name": "application",
  "profiles": ["default"],
  "label": null,
  "version": null,
  "state": null,
  "propertySources": [
    {
      "name": "file:../config-repository/application.yml",
      "source": {
        "spring.datasource.driver-class-name": "com.mysql.cj.jdbc.Driver",
        "eureka.client.service-url.defaultZone": "http://localhost:8761/eureka/",
        "security.jwt.secret": "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
      }
    }
  ]
}
```

---

## 🌐 **INTEGRATION CON MICROSERVICIOS**

### 🔧 **Configurar Microservicio como Cliente**

#### **1. Agregar dependencia en build.gradle:**
```gradle
dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-config'
    implementation 'org.springframework.cloud:spring-cloud-starter-bootstrap'
}
```

#### **2. Crear bootstrap.yml en el microservicio:**
```yaml
spring:
  application:
    name: mi-microservicio
  cloud:
    config:
      uri: http://localhost:8888
      fail-fast: true
      retry:
        initial-interval: 3000
        max-attempts: 5
  profiles:
    active: dev
```

#### **3. Habilitar refresh automático:**
```java
@RestController
@RefreshScope  // 🔄 Permite refresh dinámico
public class ConfigController {
    
    @Value("${mi.configuracion:default}")
    private String miConfiguracion;
    
    @GetMapping("/config")
    public String getConfig() {
        return miConfiguracion;
    }
}
```

### 🔄 **Refresh de Configuración**

#### **Refresh manual:**
```bash
# Refrescar un microservicio específico
curl -X POST http://localhost:8080/actuator/refresh

# Refrescar Config Server
curl -X POST http://localhost:8888/actuator/refresh
```

#### **Refresh automático con Spring Cloud Bus (opcional):**
```bash
# Broadcast refresh a todos los servicios
curl -X POST http://localhost:8888/actuator/bus-refresh
```

---

## 🛠️ **TROUBLESHOOTING**

### ❌ **Problemas Comunes**

#### **1. Config Server no inicia**
```bash
# Verificar Java 21
java -version

# Verificar puerto 8888 libre
netstat -an | grep 8888

# Revisar logs
tail -f logs/config-server.log
```

#### **2. Configuraciones no se cargan**
```bash
# Verificar directorio config-repository
ls -la config-repository/

# Verificar permisos
chmod +r config-repository/*.yml

# Test directo
curl http://localhost:8888/application/default
```

#### **3. Microservicio no conecta al Config Server**
```bash
# Verificar bootstrap.yml
cat src/main/resources/bootstrap.yml

# Verificar logs del microservicio
grep "config" logs/mi-microservicio.log

# Test de conectividad
curl http://localhost:8888/mi-microservicio/dev
```

#### **4. Eureka no registra Config Server**
```bash
# Verificar registro en Eureka
curl http://localhost:8761/eureka/apps | grep CONFIG-SERVER

# Verificar configuración Eureka en Config Server
curl http://localhost:8888/config-server/dev
```

### 🔍 **Logs Útiles**

#### **Config Server logs:**
```bash
# Logs de configuración
tail -f logs/config-server.log | grep "config"

# Logs de Eureka
tail -f logs/config-server.log | grep "eureka"

# Logs de seguridad
tail -f logs/config-server.log | grep "security"
```

### 🩺 **Health Checks**

#### **Verificación completa:**
```bash
# 1. Config Server health
curl http://localhost:8888/actuator/health

# 2. Config Server info
curl http://localhost:8888/actuator/info

# 3. Environment variables
curl http://localhost:8888/actuator/env

# 4. Configuration properties
curl http://localhost:8888/actuator/configprops

# 5. Eureka status
curl http://localhost:8761/eureka/apps
```

---

## 📚 **RECURSOS ADICIONALES**

### 🔗 **Enlaces Útiles**
- **Spring Cloud Config**: https://spring.io/projects/spring-cloud-config
- **Bootstrap Configuration**: https://cloud.spring.io/spring-cloud-commons/multi/multi__spring_cloud_context_application_context_services.html
- **Config Server Reference**: https://cloud.spring.io/spring-cloud-config/reference/html/

### 📋 **Comandos Rápidos**

```bash
# 🚀 Inicio rápido
./scripts/start-config-server.sh

# 🧪 Testing completo
./scripts/test-config-server.sh

# 🔄 Refresh configuración
curl -X POST http://localhost:8888/actuator/refresh

# 📊 Status general
curl http://localhost:8888/actuator/health

# 🔍 Ver todas las configuraciones
curl http://localhost:8888/application/default
```

---

<div align="center">
  <strong>🏗️ Config Server Implementation Complete</strong><br/>
  <em>ARKA Platform - Centralized Configuration Management</em>
</div>
