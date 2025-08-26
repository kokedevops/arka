# 🚀 GUÍA DE EJECUCIÓN COMPLETA - PROYECTO ARKA

## 📋 Requisitos Previos

### Instalaciones Necesarias
```bash
# Java 21 (obligatorio)
java --version  # Debe mostrar Java 21

# Docker Desktop
docker --version
docker-compose --version

# Git
git --version

# MySQL (opcional - se puede usar Docker)
mysql --version
```

### Variables de Entorno
```bash
# Windows PowerShell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:MYSQL_ROOT_PASSWORD = "rootpassword"
$env:MYSQL_DATABASE = "arkadb"
$env:JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
```

## 🏗️ PREPARACIÓN DEL ENTORNO

### 1. Clonar y Configurar el Repositorio
```bash
# Clonar el proyecto
git clone https://github.com/kokedevops/arkavalenzuela.git
cd arkavalenzuela

# Verificar estructura
dir  # Windows
ls   # Linux/Mac
```

### 2. Compilar Todo el Proyecto
```bash
# Windows
.\gradlew.bat clean build

# Linux/Mac
./gradlew clean build
```

### 3. Configurar Base de Datos

#### Opción A: Usar Docker (Recomendado)
```bash
# Levantar solo MySQL con Docker
docker run -d \
  --name mysql-arka \
  -e MYSQL_ROOT_PASSWORD=rootpassword \
  -e MYSQL_DATABASE=arkadb \
  -e MYSQL_USER=arkauser \
  -e MYSQL_PASSWORD=arkapass \
  -p 3306:3306 \
  mysql:8.0
```

#### Opción B: MySQL Local
```sql
-- Conectar a MySQL como root
CREATE DATABASE arkadb;
CREATE USER 'arkauser'@'localhost' IDENTIFIED BY 'arkapass';
GRANT ALL PRIVILEGES ON arkadb.* TO 'arkauser'@'localhost';
FLUSH PRIVILEGES;
```

## 🎯 EJECUCIÓN PASO A PASO

### FASE 1: Servicios de Infraestructura

#### 1. Eureka Server (Service Discovery)
```bash
# Terminal 1
cd eureka-server
..\gradlew.bat bootRun

# Verificar en browser:
# http://localhost:8761
# Debe mostrar Eureka Dashboard
```

#### 2. Config Server (Configuración Centralizada)
```bash
# Terminal 2
cd config-server  # Si existe
..\gradlew.bat bootRun

# O usar configuración local en cada servicio
```

### FASE 2: Microservicios de Negocio

#### 3. Arca Cotizador
```bash
# Terminal 3
cd arca-cotizador
..\gradlew.bat bootRun

# Logs esperados:
# "Eureka registration successful"
# "Started ArcaCotizadorApplication"
```

#### 4. Arca Gestor Solicitudes
```bash
# Terminal 4
cd arca-gestor-solicitudes
..\gradlew.bat bootRun

# Logs esperados:
# "Eureka registration successful"
# "Started ArcarGestorSolicitudesApplication"
```

#### 5. Hello World Service (Pruebas)
```bash
# Terminal 5
cd hello-world-service
..\gradlew.bat bootRun
```

### FASE 3: API Gateway

#### 6. API Gateway (Punto de Entrada)
```bash
# Terminal 6
cd api-gateway
..\gradlew.bat bootRun

# Verificar routing:
# http://localhost:8080/eureka/web (Dashboard Eureka via Gateway)
```

### FASE 4: Aplicación Principal

#### 7. Aplicación Principal ARKA
```bash
# Terminal 7
cd . # Directorio raíz
.\gradlew.bat bootRun

# Aplicación principal corriendo en:
# http://localhost:8090
```

## 🐳 EJECUCIÓN CON DOCKER

### Opción 1: Docker Compose (Recomendado)
```bash
# Construir todas las imágenes
docker-compose build

# Levantar todo el stack
docker-compose up -d

# Verificar servicios
docker-compose ps

# Ver logs
docker-compose logs -f [servicio]

# Parar todo
docker-compose down
```

### Opción 2: Scripts Automatizados
```bash
# Windows - Usar scripts incluidos
cd scripts

# Levantar Spring Cloud completo
.\start-spring-cloud.bat

# Levantar todos los servicios
.\start-all-services.bat

# Probar load balancing
.\test-load-balancing.bat
```

## 🔧 CONFIGURACIÓN DE PUERTOS

| Servicio | Puerto | URL | Descripción |
|----------|--------|-----|-------------|
| **Eureka Server** | 8761 | http://localhost:8761 | Service Discovery |
| **API Gateway** | 8080 | http://localhost:8080 | Gateway principal |
| **Arca Cotizador** | 8081 | http://localhost:8081 | Microservicio cotizaciones |
| **Arca Gestor Solicitudes** | 8082 | http://localhost:8082 | Microservicio solicitudes |
| **Hello World Service** | 8083/8084 | http://localhost:8083 | Servicio de pruebas |
| **Aplicación Principal** | 8090 | http://localhost:8090 | App principal ARKA |
| **MySQL** | 3306 | localhost:3306 | Base de datos |

## 🧪 VERIFICACIÓN DE FUNCIONAMIENTO

### 1. Verificar Eureka Dashboard
```bash
# Abrir browser:
http://localhost:8761

# Debe mostrar todos los servicios registrados:
# - ARCA-COTIZADOR
# - ARCA-GESTOR-SOLICITUDES
# - HELLO-WORLD-SERVICE
# - API-GATEWAY
```

### 2. Probar API Gateway
```bash
# Via Gateway (puerto 8080)
curl http://localhost:8080/cotizador/api/health
curl http://localhost:8080/gestor/api/health
curl http://localhost:8080/hello/api/greeting
```

### 3. Probar Microservicios Directos
```bash
# Cotizador directo
curl http://localhost:8081/api/cotizaciones

# Gestor directo
curl http://localhost:8082/api/solicitudes

# Hello World
curl http://localhost:8083/api/greeting
```

### 4. Probar Load Balancing
```bash
# Levantar múltiples instancias de Hello World
cd hello-world-service

# Instancia 1 (puerto 8083)
.\gradlew.bat bootRun

# Instancia 2 (puerto 8084) - Nueva terminal
.\gradlew.bat bootRun --args="--server.port=8084 --spring.profiles.active=instance2"

# Probar load balancing via Gateway
curl http://localhost:8080/hello/api/greeting  # Instancia 1
curl http://localhost:8080/hello/api/greeting  # Instancia 2 (round-robin)
```

## 🔐 AUTENTICACIÓN Y SEGURIDAD

### 1. Obtener Token JWT
```bash
# Login para obtener token
curl -X POST http://localhost:8090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Respuesta esperada:
# {
#   "token": "eyJhbGciOiJIUzUxMiJ9...",
#   "type": "Bearer",
#   "username": "admin",
#   "roles": ["ADMIN"]
# }
```

### 2. Usar Token en Requests
```bash
# Guardar token
$token = "eyJhbGciOiJIUzUxMiJ9..."

# Usar token en requests protegidos
curl -H "Authorization: Bearer $token" \
  http://localhost:8090/api/admin/usuarios
```

### 3. Endpoints por Rol

#### Usuario (ROLE_USUARIO)
```bash
curl -H "Authorization: Bearer $token" \
  http://localhost:8090/api/usuario/perfil
```

#### Gestor (ROLE_GESTOR)
```bash
curl -H "Authorization: Bearer $token" \
  http://localhost:8090/api/gestor/solicitudes
```

#### Administrador (ROLE_ADMIN)
```bash
curl -H "Authorization: Bearer $token" \
  http://localhost:8090/api/admin/usuarios
```

## 📊 ENDPOINTS REACTIVOS

### 1. Cotizaciones (Reactive)
```bash
# Generar cotización (reactivo)
curl -X POST http://localhost:8090/api/cotizaciones/generar/1

# Stream de cotizaciones (Server-Sent Events)
curl -H "Accept: application/stream+json" \
  http://localhost:8090/api/cotizaciones/stream

# Aplicar descuento (reactivo)
curl -X PUT http://localhost:8090/api/cotizaciones/1/descuento?porcentaje=10
```

### 2. Solicitudes (Reactive)
```bash
# Crear solicitud (reactivo)
curl -X POST http://localhost:8090/api/solicitudes \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "descripcion": "Solicitud de cotización",
    "monto": 1000.00
  }'
```

## 🐛 TROUBLESHOOTING

### Problemas Comunes

#### 1. Puerto Ocupado
```bash
# Verificar qué usa el puerto
netstat -ano | findstr :8080
taskkill /PID [PID] /F
```

#### 2. Eureka No Registra Servicios
```bash
# Verificar configuración en application.yml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
```

#### 3. Base de Datos No Conecta
```bash
# Verificar MySQL corriendo
docker ps | grep mysql

# Verificar configuración
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/arkadb
    username: arkauser
    password: arkapass
```

#### 4. Compilación Falla
```bash
# Limpiar y recompilar
.\gradlew.bat clean
.\gradlew.bat build --refresh-dependencies
```

### Logs y Monitoreo
```bash
# Ver logs de cada servicio
docker-compose logs -f eureka-server
docker-compose logs -f api-gateway
docker-compose logs -f arca-cotizador

# Monitorear recursos
docker stats
```

## 🎯 TESTS E2E

### Ejecutar Suite Completa
```bash
# Tests unitarios
.\gradlew.bat test

# Tests de integración
.\gradlew.bat integrationTest

# Tests de seguridad
.\gradlew.bat test --tests "*SecurityTest*"

# Tests reactivos
.\gradlew.bat test --tests "*ReactiveTest*"
```

### Scenarios de Prueba
```bash
# 1. Flujo completo: Solicitud → Cotización → Aceptación
curl -X POST http://localhost:8090/api/solicitudes -d '{...}'
curl -X POST http://localhost:8090/api/cotizaciones/generar/1
curl -X PUT http://localhost:8090/api/cotizaciones/1/aceptar

# 2. Load balancing con múltiples instancias
# Levantar 2+ instancias y probar distribución

# 3. Fault tolerance
# Parar un servicio y verificar que Gateway maneja el error
```

## 🚀 DEPLOYMENT PRODUCTION

### AWS (Ver AWS-CLOUD-IMPLEMENTATION.md)
```bash
# Usar CloudFormation templates incluidos
aws cloudformation create-stack --stack-name arka-prod \
  --template-body file://aws/arka-infrastructure.yaml
```

### Docker Swarm
```bash
docker swarm init
docker stack deploy -c docker-compose.prod.yml arka
```

---

## ✅ CHECKLIST DE VERIFICACIÓN

- [ ] ✅ Eureka Server levantado (puerto 8761)
- [ ] ✅ API Gateway funcionando (puerto 8080)
- [ ] ✅ Arca Cotizador registrado en Eureka
- [ ] ✅ Arca Gestor Solicitudes registrado en Eureka
- [ ] ✅ MySQL conectado y tablas creadas
- [ ] ✅ JWT authentication funcionando
- [ ] ✅ Endpoints reactivos respondiendo
- [ ] ✅ Load balancing operativo
- [ ] ✅ Tests pasando
- [ ] ✅ Docker compose levantado correctamente

**¡Proyecto ARKA completamente operativo! 🎉**
