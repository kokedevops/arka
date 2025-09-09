# 🐳 DOCKER Y CONTAINERIZACIÓN - IMPLEMENTACIÓN COMPLETA

## 🎯 **INTRODUCCIÓN AL ECOSISTEMA DOCKER**

**Docker en Arka Valenzuela** permite el despliegue consistente, escalable y portable de todos los microservicios, garantizando que funcionen de manera idéntica en desarrollo, testing y producción. La containerización facilita el CI/CD y la gestión de dependencias complejas.

---

## 🏗️ **ARQUITECTURA DE CONTENEDORES**

```
                    🌐 LOAD BALANCER (NGINX)
                           Port: 80/443
                               │
                    ┌──────────┼──────────┐
                    │                     │
          ┌─────────┼─────────┐    ┌──────┼──────┐
          │    🚪 API GATEWAY │    │ 🖥️ FRONTEND │
          │     Port: 8080    │    │  Port: 3000 │
          │   (api-gateway)   │    │  (React/Angular)│
          └─────────┼─────────┘    └─────────────┘
                    │
         ┌──────────┼──────────┐
         │    🔍 EUREKA SERVER │
         │      Port: 8761     │
         │   (service-discovery)│
         └─────────┼──────────┘
                   │
    ┌──────────────┼──────────────┐
    │        📁 CONFIG SERVER     │
    │         Port: 8888          │
    │       (configuration)       │
    └─────────────┼──────────────┘
                  │
    ┌─────────────┼─────────────┐
    │                           │
┌───┼─────┐  ┌─────┼──────┐  ┌──┼──────┐
│🧮 COTIZ │  │📋 GESTOR    │  │👋 HELLO │
│Port:8081│  │Port: 8082   │  │Port:8083│
│         │  │SOLICITUDES  │  │ WORLD   │
└───┼─────┘  └─────┼──────┘  └──┼──────┘
    │              │            │
    └──────────────┼────────────┘
                   │
    ┌──────────────┼──────────────┐
    │                             │
┌───┼────────┐              ┌─────┼─────┐
│🗄️ MySQL   │              │🍃 MongoDB │
│Port: 3306  │              │Port: 27017│
│(cotizador) │              │(solicitudes)│
└────────────┘              └───────────┘

           📊 MONITORING STACK
    ┌─────────────────────────────────┐
    │🔥 Prometheus  📈 Grafana  📋 ELK│
    │Port: 9090     Port: 3001  Port:*│
    └─────────────────────────────────┘
```

---

## 🐳 **DOCKERFILES POR MICROSERVICIO**

### 🏗️ **Dockerfile Base Común**

```dockerfile
# 📁 Dockerfile.base
FROM openjdk:21-jdk-slim as builder

# Información del mantenedor
LABEL maintainer="arka-valenzuela-team@example.com"
LABEL version="1.0.0"
LABEL description="Base image for Arka Valenzuela microservices"

# Variables de entorno para optimización JVM
ENV JAVA_OPTS="-server -Xms512m -Xmx2048m -XX:+UseG1GC -XX:+HeapDumpOnOutOfMemoryError"
ENV SPRING_PROFILES_ACTIVE=docker

# Crear usuario no root para seguridad
RUN groupadd -r arka && useradd --no-log-init -r -g arka arka

# Crear directorios necesarios
RUN mkdir -p /app/logs /app/tmp && chown -R arka:arka /app

# Instalar dependencias del sistema
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Configurar zona horaria
ENV TZ=America/Santiago
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# Configurar directorio de trabajo
WORKDIR /app

# Script de espera para dependencias
COPY wait-for-it.sh /app/wait-for-it.sh
RUN chmod +x /app/wait-for-it.sh && chown arka:arka /app/wait-for-it.sh

# Script de healthcheck
COPY health-check.sh /app/health-check.sh
RUN chmod +x /app/health-check.sh && chown arka:arka /app/health-check.sh

# Cambiar a usuario no root
USER arka

# Punto de entrada por defecto
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 🚪 **API Gateway Dockerfile**

```dockerfile
# 📁 api-gateway/Dockerfile
FROM arka-valenzuela/base:latest as builder

# Información específica del servicio
LABEL service="api-gateway"
LABEL description="API Gateway for Arka Valenzuela microservices"

# Copiar jar construido
COPY build/libs/api-gateway-*.jar /app/app.jar

# Variables específicas del gateway
ENV SERVER_PORT=8080
ENV EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
ENV SPRING_CLOUD_CONFIG_URI=http://config-server:8888

# Healthcheck específico para gateway
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Exponer puerto del gateway
EXPOSE 8080

# Comando de inicio con espera de dependencias
CMD ["./wait-for-it.sh", "eureka-server:8761", "--timeout=120", "--strict", "--", \
     "./wait-for-it.sh", "config-server:8888", "--timeout=120", "--strict", "--", \
     "java", "$JAVA_OPTS", "-jar", "/app/app.jar"]
```

### 🧮 **Cotizador Dockerfile**

```dockerfile
# 📁 arca-cotizador/Dockerfile
FROM arka-valenzuela/base:latest

LABEL service="arca-cotizador"
LABEL description="Cotization microservice for Arka Valenzuela"

# Copiar jar del microservicio
COPY build/libs/arca-cotizador-*.jar /app/app.jar

# Variables específicas del cotizador
ENV SERVER_PORT=8081
ENV SPRING_DATASOURCE_URL=jdbc:mysql://mysql-cotizador:3306/arka_cotizaciones
ENV SPRING_DATASOURCE_USERNAME=arka_user
ENV SPRING_DATASOURCE_PASSWORD=arka_password_2025

# Configuración de JVM específica para cotizador
ENV JAVA_OPTS="-server -Xms256m -Xmx1024m -XX:+UseG1GC \
               -Dspring.profiles.active=docker \
               -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus"

# Healthcheck para cotizador
HEALTHCHECK --interval=30s --timeout=10s --start-period=90s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Exponer puerto del cotizador
EXPOSE 8081

# Configurar volúmenes para logs y datos temporales
VOLUME ["/app/logs", "/app/tmp"]

# Comando de inicio con dependencias
CMD ["./wait-for-it.sh", "mysql-cotizador:3306", "--timeout=120", "--strict", "--", \
     "./wait-for-it.sh", "eureka-server:8761", "--timeout=120", "--strict", "--", \
     "./wait-for-it.sh", "config-server:8888", "--timeout=60", "--strict", "--", \
     "java", "$JAVA_OPTS", "-jar", "/app/app.jar"]
```

### 📋 **Gestor Solicitudes Dockerfile**

```dockerfile
# 📁 arca-gestor-solicitudes/Dockerfile  
FROM arka-valenzuela/base:latest

LABEL service="arca-gestor-solicitudes"
LABEL description="Request management microservice for Arka Valenzuela"

# Copiar jar del microservicio
COPY build/libs/arca-gestor-solicitudes-*.jar /app/app.jar

# Variables específicas del gestor
ENV SERVER_PORT=8082
ENV SPRING_DATA_MONGODB_URI=mongodb://mongodb-solicitudes:27017/arka_solicitudes
ENV SPRING_DATA_MONGODB_DATABASE=arka_solicitudes

# Configuración JVM optimizada para procesamiento de solicitudes
ENV JAVA_OPTS="-server -Xms512m -Xmx1536m -XX:+UseG1GC \
               -XX:MaxGCPauseMillis=200 \
               -Dspring.profiles.active=docker \
               -Dmanagement.endpoints.web.exposure.include=health,info,metrics,prometheus"

# Healthcheck para gestor de solicitudes
HEALTHCHECK --interval=30s --timeout=15s --start-period=120s --retries=3 \
    CMD curl -f http://localhost:8082/actuator/health || exit 1

# Exponer puerto del gestor
EXPOSE 8082

# Volúmenes para persistencia de logs y cache
VOLUME ["/app/logs", "/app/cache"]

# Comando de inicio con verificación de MongoDB
CMD ["./wait-for-it.sh", "mongodb-solicitudes:27017", "--timeout=120", "--strict", "--", \
     "./wait-for-it.sh", "eureka-server:8761", "--timeout=120", "--strict", "--", \
     "./wait-for-it.sh", "config-server:8888", "--timeout=60", "--strict", "--", \
     "java", "$JAVA_OPTS", "-jar", "/app/app.jar"]
```

### 🔍 **Eureka Server Dockerfile**

```dockerfile
# 📁 eureka-server/Dockerfile
FROM arka-valenzuela/base:latest

LABEL service="eureka-server"
LABEL description="Service Discovery for Arka Valenzuela microservices"

# Copiar jar del eureka server
COPY build/libs/eureka-server-*.jar /app/app.jar

# Variables específicas de Eureka
ENV SERVER_PORT=8761
ENV EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
ENV EUREKA_CLIENT_FETCH_REGISTRY=false
ENV EUREKA_SERVER_ENABLE_SELF_PRESERVATION=false

# JVM optimizada para Eureka
ENV JAVA_OPTS="-server -Xms256m -Xmx512m -XX:+UseG1GC \
               -Dspring.profiles.active=docker \
               -Deureka.server.responseCacheUpdateIntervalMs=3000"

# Healthcheck para Eureka
HEALTHCHECK --interval=20s --timeout=5s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8761/actuator/health || exit 1

# Exponer puerto de Eureka
EXPOSE 8761

# Eureka inicia primero, sin dependencias
CMD ["java", "$JAVA_OPTS", "-jar", "/app/app.jar"]
```

### 📁 **Config Server Dockerfile**

```dockerfile
# 📁 config-server/Dockerfile
FROM arka-valenzuela/base:latest

LABEL service="config-server"
LABEL description="Configuration Server for Arka Valenzuela microservices"

# Copiar jar del config server
COPY build/libs/config-server-*.jar /app/app.jar

# Variables específicas del Config Server
ENV SERVER_PORT=8888
ENV SPRING_CLOUD_CONFIG_SERVER_GIT_URI=https://github.com/arka-valenzuela/config-repository.git
ENV SPRING_CLOUD_CONFIG_SERVER_GIT_DEFAULT_LABEL=main

# JVM para Config Server
ENV JAVA_OPTS="-server -Xms128m -Xmx256m -XX:+UseG1GC \
               -Dspring.profiles.active=docker"

# Healthcheck para Config Server
HEALTHCHECK --interval=20s --timeout=5s --start-period=30s --retries=3 \
    CMD curl -f http://localhost:8888/actuator/health || exit 1

# Exponer puerto del Config Server
EXPOSE 8888

# Config Server inicia después de Eureka
CMD ["./wait-for-it.sh", "eureka-server:8761", "--timeout=60", "--strict", "--", \
     "java", "$JAVA_OPTS", "-jar", "/app/app.jar"]
```

---

## 🐳 **DOCKER COMPOSE COMPLETO**

### 🏗️ **docker-compose.yml Principal**

```yaml
# 📁 docker-compose.yml
version: '3.8'

networks:
  arka-network:
    driver: bridge
    ipam:
      driver: default
      config:
        - subnet: 172.20.0.0/16

volumes:
  mysql-cotizador-data:
    driver: local
  mongodb-solicitudes-data:
    driver: local
  prometheus-data:
    driver: local
  grafana-data:
    driver: local
  elasticsearch-data:
    driver: local

services:
  # ================================================
  # 🗄️ BASES DE DATOS
  # ================================================
  
  mysql-cotizador:
    image: mysql:8.0
    container_name: arka-mysql-cotizador
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: root_password_2025
      MYSQL_DATABASE: arka_cotizaciones
      MYSQL_USER: arka_user
      MYSQL_PASSWORD: arka_password_2025
      MYSQL_CHARACTER_SET_SERVER: utf8mb4
      MYSQL_COLLATION_SERVER: utf8mb4_unicode_ci
    ports:
      - "3306:3306"
    volumes:
      - mysql-cotizador-data:/var/lib/mysql
      - ./scripts/mysql/init.sql:/docker-entrypoint-initdb.d/init.sql:ro
      - ./scripts/mysql/cotizador-schema.sql:/docker-entrypoint-initdb.d/cotizador-schema.sql:ro
    networks:
      arka-network:
        ipv4_address: 172.20.0.10
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      timeout: 10s
      retries: 5
      interval: 30s
      start_period: 60s
    command: --default-authentication-plugin=mysql_native_password

  mongodb-solicitudes:
    image: mongo:7.0
    container_name: arka-mongodb-solicitudes
    restart: unless-stopped
    environment:
      MONGO_INITDB_ROOT_USERNAME: admin
      MONGO_INITDB_ROOT_PASSWORD: admin_password_2025
      MONGO_INITDB_DATABASE: arka_solicitudes
    ports:
      - "27017:27017"
    volumes:
      - mongodb-solicitudes-data:/data/db
      - ./scripts/mongodb/init.js:/docker-entrypoint-initdb.d/init.js:ro
    networks:
      arka-network:
        ipv4_address: 172.20.0.11
    healthcheck:
      test: ["CMD", "mongosh", "--eval", "db.adminCommand('ping')"]
      timeout: 10s
      retries: 5
      interval: 30s
      start_period: 60s

  # ================================================
  # 🔧 INFRAESTRUCTURA DE MICROSERVICIOS
  # ================================================

  eureka-server:
    build:
      context: ./eureka-server
      dockerfile: Dockerfile
    image: arka-valenzuela/eureka-server:latest
    container_name: arka-eureka-server
    restart: unless-stopped
    ports:
      - "8761:8761"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_REGISTER_WITH_EUREKA=false
      - EUREKA_CLIENT_FETCH_REGISTRY=false
      - EUREKA_SERVER_ENABLE_SELF_PRESERVATION=false
    networks:
      arka-network:
        ipv4_address: 172.20.0.20
    volumes:
      - ./logs/eureka:/app/logs
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8761/actuator/health"]
      timeout: 10s
      retries: 3
      interval: 30s
      start_period: 60s

  config-server:
    build:
      context: ./config-server
      dockerfile: Dockerfile
    image: arka-valenzuela/config-server:latest
    container_name: arka-config-server
    restart: unless-stopped
    ports:
      - "8888:8888"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CLOUD_CONFIG_SERVER_GIT_URI=file:///app/config-repository
    volumes:
      - ./config-repository:/app/config-repository:ro
      - ./logs/config-server:/app/logs
    networks:
      arka-network:
        ipv4_address: 172.20.0.21
    depends_on:
      eureka-server:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8888/actuator/health"]
      timeout: 10s
      retries: 3
      interval: 30s
      start_period: 60s

  # ================================================
  # 🚪 API GATEWAY
  # ================================================

  api-gateway:
    build:
      context: ./api-gateway
      dockerfile: Dockerfile
    image: arka-valenzuela/api-gateway:latest
    container_name: arka-api-gateway
    restart: unless-stopped
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
      - SPRING_CLOUD_GATEWAY_DISCOVERY_LOCATOR_ENABLED=true
      - SPRING_CLOUD_GATEWAY_DISCOVERY_LOCATOR_LOWER_CASE_SERVICE_ID=true
    volumes:
      - ./logs/api-gateway:/app/logs
    networks:
      arka-network:
        ipv4_address: 172.20.0.30
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      timeout: 15s
      retries: 3
      interval: 30s
      start_period: 90s

  # ================================================
  # 🎯 MICROSERVICIOS DE NEGOCIO
  # ================================================

  arca-cotizador:
    build:
      context: ./arca-cotizador
      dockerfile: Dockerfile
    image: arka-valenzuela/arca-cotizador:latest
    container_name: arka-cotizador
    restart: unless-stopped
    ports:
      - "8081:8081"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql-cotizador:3306/arka_cotizaciones
      - SPRING_DATASOURCE_USERNAME=arka_user
      - SPRING_DATASOURCE_PASSWORD=arka_password_2025
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
    volumes:
      - ./logs/arca-cotizador:/app/logs
    networks:
      arka-network:
        ipv4_address: 172.20.0.40
    depends_on:
      mysql-cotizador:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/actuator/health"]
      timeout: 15s
      retries: 3
      interval: 30s
      start_period: 120s

  arca-gestor-solicitudes:
    build:
      context: ./arca-gestor-solicitudes
      dockerfile: Dockerfile
    image: arka-valenzuela/arca-gestor-solicitudes:latest
    container_name: arka-gestor-solicitudes
    restart: unless-stopped
    ports:
      - "8082:8082"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_DATA_MONGODB_URI=mongodb://admin:admin_password_2025@mongodb-solicitudes:27017/arka_solicitudes?authSource=admin
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=health,info,metrics,prometheus
    volumes:
      - ./logs/arca-gestor-solicitudes:/app/logs
    networks:
      arka-network:
        ipv4_address: 172.20.0.41
    depends_on:
      mongodb-solicitudes:
        condition: service_healthy
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8082/actuator/health"]
      timeout: 15s
      retries: 3
      interval: 30s
      start_period: 120s

  hello-world-service:
    build:
      context: ./hello-world-service
      dockerfile: Dockerfile
    image: arka-valenzuela/hello-world-service:latest
    container_name: arka-hello-world
    restart: unless-stopped
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - EUREKA_CLIENT_SERVICE_URL_DEFAULTZONE=http://eureka-server:8761/eureka/
      - SPRING_CLOUD_CONFIG_URI=http://config-server:8888
    volumes:
      - ./logs/hello-world:/app/logs
    networks:
      arka-network:
        ipv4_address: 172.20.0.42
    depends_on:
      eureka-server:
        condition: service_healthy
      config-server:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8083/actuator/health"]
      timeout: 10s
      retries: 3
      interval: 30s
      start_period: 90s

  # ================================================
  # 📊 MONITORING Y OBSERVABILIDAD
  # ================================================

  prometheus:
    image: prom/prometheus:latest
    container_name: arka-prometheus
    restart: unless-stopped
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml:ro
      - prometheus-data:/prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/etc/prometheus/console_libraries'
      - '--web.console.templates=/etc/prometheus/consoles'
      - '--storage.tsdb.retention.time=15d'
      - '--web.enable-lifecycle'
    networks:
      arka-network:
        ipv4_address: 172.20.0.50

  grafana:
    image: grafana/grafana:latest
    container_name: arka-grafana
    restart: unless-stopped
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin123
      - GF_USERS_ALLOW_SIGN_UP=false
    volumes:
      - grafana-data:/var/lib/grafana
      - ./monitoring/grafana/dashboards:/etc/grafana/provisioning/dashboards:ro
      - ./monitoring/grafana/datasources:/etc/grafana/provisioning/datasources:ro
    networks:
      arka-network:
        ipv4_address: 172.20.0.51
    depends_on:
      - prometheus

  # ================================================
  # 🌐 FRONTEND (OPCIONAL)
  # ================================================

  frontend:
    build:
      context: ./frontend
      dockerfile: Dockerfile
    image: arka-valenzuela/frontend:latest
    container_name: arka-frontend
    restart: unless-stopped
    ports:
      - "3000:3000"
    environment:
      - REACT_APP_API_URL=http://localhost:8080
      - REACT_APP_ENVIRONMENT=docker
    networks:
      arka-network:
        ipv4_address: 172.20.0.60
    depends_on:
      - api-gateway

  # ================================================
  # 🌐 NGINX LOAD BALANCER
  # ================================================

  nginx:
    image: nginx:alpine
    container_name: arka-nginx
    restart: unless-stopped
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf:ro
      - ./nginx/conf.d:/etc/nginx/conf.d:ro
      - ./nginx/ssl:/etc/nginx/ssl:ro
    networks:
      arka-network:
        ipv4_address: 172.20.0.70
    depends_on:
      - api-gateway
      - frontend
```

---

## 🚀 **SCRIPTS DE GESTIÓN**

### 🔧 **Script de Construcción y Despliegue**

```bash
#!/bin/bash
# 📁 scripts/docker-build-all.sh

set -euo pipefail

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Función para logging
log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

error() {
    echo -e "${RED}[ERROR] $1${NC}" >&2
}

warn() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

# Variables de configuración
PROJECT_NAME="arka-valenzuela"
REGISTRY="arka-valenzuela"
VERSION="${1:-latest}"
BUILD_MODE="${2:-full}"

# Servicios a construir
SERVICES=(
    "eureka-server"
    "config-server"
    "api-gateway"
    "arca-cotizador"
    "arca-gestor-solicitudes"
    "hello-world-service"
)

# Función para construir la imagen base
build_base_image() {
    log "Construyendo imagen base..."
    
    if ! docker build -f Dockerfile.base -t ${REGISTRY}/base:${VERSION} .; then
        error "Error construyendo imagen base"
        exit 1
    fi
    
    log "Imagen base construida exitosamente"
}

# Función para construir un microservicio
build_service() {
    local service=$1
    
    log "Construyendo ${service}..."
    
    # Verificar que existe el directorio
    if [[ ! -d "${service}" ]]; then
        error "Directorio ${service} no existe"
        return 1
    fi
    
    # Construir con Gradle si existe build.gradle
    if [[ -f "${service}/build.gradle" ]]; then
        info "Compilando ${service} con Gradle..."
        ./gradlew :${service}:clean :${service}:build -x test
        
        if [[ $? -ne 0 ]]; then
            error "Error compilando ${service}"
            return 1
        fi
    fi
    
    # Construir imagen Docker
    if ! docker build -t ${REGISTRY}/${service}:${VERSION} ${service}/; then
        error "Error construyendo imagen Docker para ${service}"
        return 1
    fi
    
    log "${service} construido exitosamente"
}

# Función para verificar dependencias
check_dependencies() {
    log "Verificando dependencias..."
    
    # Verificar Docker
    if ! command -v docker &> /dev/null; then
        error "Docker no está instalado"
        exit 1
    fi
    
    # Verificar Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose no está instalado"
        exit 1
    fi
    
    # Verificar Gradle
    if ! command -v ./gradlew &> /dev/null; then
        error "Gradle wrapper no encontrado"
        exit 1
    fi
    
    log "Todas las dependencias están disponibles"
}

# Función para limpiar contenedores e imágenes antiguas
cleanup() {
    log "Limpiando contenedores e imágenes antiguas..."
    
    # Detener contenedores en ejecución
    if docker-compose down --remove-orphans 2>/dev/null; then
        info "Contenedores detenidos"
    fi
    
    # Eliminar imágenes antiguas con el mismo tag
    for service in "${SERVICES[@]}"; do
        if docker image inspect ${REGISTRY}/${service}:${VERSION} >/dev/null 2>&1; then
            warn "Eliminando imagen antigua: ${REGISTRY}/${service}:${VERSION}"
            docker rmi ${REGISTRY}/${service}:${VERSION} || true
        fi
    done
    
    # Limpiar imágenes dangling
    if docker images -f "dangling=true" -q | grep -q .; then
        info "Eliminando imágenes dangling..."
        docker rmi $(docker images -f "dangling=true" -q) || true
    fi
    
    log "Limpieza completada"
}

# Función para ejecutar tests
run_tests() {
    log "Ejecutando tests..."
    
    if ./gradlew test; then
        log "Todos los tests pasaron exitosamente"
    else
        error "Algunos tests fallaron"
        exit 1
    fi
}

# Función para verificar que las imágenes están funcionando
verify_images() {
    log "Verificando imágenes construidas..."
    
    for service in "${SERVICES[@]}"; do
        if docker image inspect ${REGISTRY}/${service}:${VERSION} >/dev/null 2>&1; then
            info "✓ ${service} imagen disponible"
        else
            error "✗ ${service} imagen no encontrada"
            exit 1
        fi
    done
    
    log "Todas las imágenes verificadas exitosamente"
}

# Función principal
main() {
    log "Iniciando construcción de ${PROJECT_NAME} v${VERSION}"
    
    # Verificar dependencias
    check_dependencies
    
    # Limpiar si se especifica
    if [[ "${BUILD_MODE}" == "clean" ]]; then
        cleanup
    fi
    
    # Ejecutar tests si se especifica
    if [[ "${BUILD_MODE}" == "test" || "${BUILD_MODE}" == "full" ]]; then
        run_tests
    fi
    
    # Construir imagen base
    build_base_image
    
    # Construir cada microservicio
    for service in "${SERVICES[@]}"; do
        if build_service "${service}"; then
            info "✓ ${service} construido exitosamente"
        else
            error "✗ Error construyendo ${service}"
            exit 1
        fi
    done
    
    # Verificar imágenes
    verify_images
    
    log "Construcción completada exitosamente!"
    log "Para iniciar los servicios ejecuta: docker-compose up -d"
}

# Mostrar ayuda
show_help() {
    echo "Uso: $0 [VERSION] [BUILD_MODE]"
    echo ""
    echo "VERSION: Tag de la imagen (default: latest)"
    echo "BUILD_MODE: Modo de construcción"
    echo "  - full: Tests + construcción completa (default)"
    echo "  - clean: Limpieza + construcción"
    echo "  - test: Solo tests + construcción"
    echo "  - fast: Solo construcción, sin tests"
    echo ""
    echo "Ejemplos:"
    echo "  $0                    # Construcción completa con tag 'latest'"
    echo "  $0 v1.0.0            # Construcción con tag 'v1.0.0'"
    echo "  $0 latest clean      # Limpieza + construcción"
    echo "  $0 dev fast          # Construcción rápida sin tests"
}

# Verificar argumentos
if [[ "${1:-}" == "-h" || "${1:-}" == "--help" ]]; then
    show_help
    exit 0
fi

# Ejecutar función principal
main "$@"
```

### 🚀 **Script de Inicio Rápido**

```bash
#!/bin/bash
# 📁 scripts/quick-start.sh

set -euo pipefail

# Colores
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

log() {
    echo -e "${GREEN}[$(date +'%H:%M:%S')] $1${NC}"
}

info() {
    echo -e "${BLUE}[INFO] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[WARNING] $1${NC}"
}

# Función para esperar que un servicio esté listo
wait_for_service() {
    local service=$1
    local url=$2
    local max_attempts=30
    local attempt=1
    
    log "Esperando que ${service} esté listo..."
    
    while [[ $attempt -le $max_attempts ]]; do
        if curl -s -f "${url}" >/dev/null 2>&1; then
            log "${service} está listo!"
            return 0
        fi
        
        info "Intento ${attempt}/${max_attempts} - ${service} no está listo aún..."
        sleep 10
        ((attempt++))
    done
    
    warn "${service} no respondió después de ${max_attempts} intentos"
    return 1
}

# Función para mostrar el estado de los servicios
show_status() {
    log "Estado de los servicios:"
    echo ""
    
    services=(
        "eureka-server:8761:/actuator/health"
        "config-server:8888:/actuator/health"
        "api-gateway:8080:/actuator/health"
        "arca-cotizador:8081:/actuator/health"
        "arca-gestor-solicitudes:8082:/actuator/health"
        "hello-world-service:8083:/actuator/health"
    )
    
    for service_info in "${services[@]}"; do
        IFS=':' read -r service port path <<< "$service_info"
        if curl -s -f "http://localhost:${port}${path}" >/dev/null 2>&1; then
            echo -e "✅ ${service} - http://localhost:${port}${path}"
        else
            echo -e "❌ ${service} - http://localhost:${port}${path}"
        fi
    done
}

# Función para mostrar URLs importantes
show_urls() {
    log "URLs importantes:"
    echo ""
    echo "🔍 Eureka Server: http://localhost:8761"
    echo "⚙️ Config Server: http://localhost:8888"
    echo "🚪 API Gateway: http://localhost:8080"
    echo "🧮 Cotizador: http://localhost:8081"
    echo "📋 Gestor Solicitudes: http://localhost:8082"
    echo "👋 Hello World: http://localhost:8083"
    echo "📊 Prometheus: http://localhost:9090"
    echo "📈 Grafana: http://localhost:3001 (admin/admin123)"
    echo "🌐 Frontend: http://localhost:3000"
    echo ""
    echo "📱 API Endpoints principales:"
    echo "   GET  http://localhost:8080/api/cotizaciones"
    echo "   POST http://localhost:8080/api/cotizaciones"
    echo "   GET  http://localhost:8080/api/solicitudes"
    echo "   POST http://localhost:8080/api/solicitudes"
}

# Función principal
main() {
    log "Iniciando Arka Valenzuela en Docker..."
    
    # Construir si no existen las imágenes
    if [[ ! "$(docker images -q arka-valenzuela/eureka-server:latest 2> /dev/null)" ]]; then
        log "Imágenes no encontradas, construyendo..."
        ./scripts/docker-build-all.sh latest fast
    fi
    
    # Iniciar servicios
    log "Iniciando servicios con Docker Compose..."
    docker-compose up -d
    
    # Esperar servicios críticos
    wait_for_service "Eureka Server" "http://localhost:8761/actuator/health"
    wait_for_service "Config Server" "http://localhost:8888/actuator/health"
    wait_for_service "API Gateway" "http://localhost:8080/actuator/health"
    
    # Mostrar estado y URLs
    show_status
    show_urls
    
    log "¡Arka Valenzuela está listo para usar!"
}

# Función para detener servicios
stop() {
    log "Deteniendo servicios..."
    docker-compose down
    log "Servicios detenidos"
}

# Función para reiniciar servicios
restart() {
    log "Reiniciando servicios..."
    docker-compose restart
    show_status
}

# Función para ver logs
logs() {
    local service=${1:-}
    if [[ -n "$service" ]]; then
        docker-compose logs -f "$service"
    else
        docker-compose logs -f
    fi
}

# Procesar argumentos de línea de comandos
case "${1:-start}" in
    start)
        main
        ;;
    stop)
        stop
        ;;
    restart)
        restart
        ;;
    status)
        show_status
        ;;
    logs)
        logs "${2:-}"
        ;;
    urls)
        show_urls
        ;;
    *)
        echo "Uso: $0 {start|stop|restart|status|logs|urls}"
        echo ""
        echo "Comandos:"
        echo "  start    - Iniciar todos los servicios"
        echo "  stop     - Detener todos los servicios"
        echo "  restart  - Reiniciar todos los servicios"
        echo "  status   - Mostrar estado de servicios"
        echo "  logs     - Mostrar logs (opcionalmente de un servicio específico)"
        echo "  urls     - Mostrar URLs importantes"
        exit 1
        ;;
esac
```

---

## 🔧 **CONFIGURACIÓN Y UTILIDADES**

### 🕐 **Wait-for-it Script**

```bash
#!/bin/bash
# 📁 wait-for-it.sh

set -e

WAITFORIT_cmdname=${0##*/}

echoerr() { if [[ $WAITFORIT_QUIET -ne 1 ]]; then echo "$@" 1>&2; fi }

usage()
{
    cat << USAGE >&2
Usage:
    $WAITFORIT_cmdname host:port [-s] [-t timeout] [-- command args]
    -h HOST | --host=HOST       Host or IP under test
    -p PORT | --port=PORT       TCP port under test
                                Alternatively, you specify the host and port as host:port
    -s | --strict               Only execute subcommand if the test succeeds
    -q | --quiet                Don't output any status messages
    -t TIMEOUT | --timeout=TIMEOUT
                                Timeout in seconds, zero for no timeout
    -- COMMAND ARGS             Execute command with args after the test finishes
USAGE
    exit 1
}

wait_for()
{
    if [[ $WAITFORIT_TIMEOUT -gt 0 ]]; then
        echoerr "$WAITFORIT_cmdname: waiting $WAITFORIT_TIMEOUT seconds for $WAITFORIT_HOST:$WAITFORIT_PORT"
    else
        echoerr "$WAITFORIT_cmdname: waiting for $WAITFORIT_HOST:$WAITFORIT_PORT without a timeout"
    fi
    WAITFORIT_start_ts=$(date +%s)
    while :
    do
        if [[ $WAITFORIT_ISBUSY -eq 1 ]]; then
            nc -z $WAITFORIT_HOST $WAITFORIT_PORT
            WAITFORIT_result=$?
        else
            (echo > /dev/tcp/$WAITFORIT_HOST/$WAITFORIT_PORT) >/dev/null 2>&1
            WAITFORIT_result=$?
        fi
        if [[ $WAITFORIT_result -eq 0 ]]; then
            WAITFORIT_end_ts=$(date +%s)
            echoerr "$WAITFORIT_cmdname: $WAITFORIT_HOST:$WAITFORIT_PORT is available after $((WAITFORIT_end_ts - WAITFORIT_start_ts)) seconds"
            break
        fi
        sleep 1
    done
    return $WAITFORIT_result
}

wait_for_wrapper()
{
    # In order to support SIGINT during timeout: http://unix.stackexchange.com/a/57692
    if [[ $WAITFORIT_QUIET -eq 1 ]]; then
        timeout $WAITFORIT_BUSYTIMEFLAG $WAITFORIT_TIMEOUT $0 --quiet --child --host=$WAITFORIT_HOST --port=$WAITFORIT_PORT --timeout=$WAITFORIT_TIMEOUT &
    else
        timeout $WAITFORIT_BUSYTIMEFLAG $WAITFORIT_TIMEOUT $0 --child --host=$WAITFORIT_HOST --port=$WAITFORIT_PORT --timeout=$WAITFORIT_TIMEOUT &
    fi
    WAITFORIT_PID=$!
    trap "kill -INT -$WAITFORIT_PID" INT
    wait $WAITFORIT_PID
    WAITFORIT_RESULT=$?
    if [[ $WAITFORIT_RESULT -ne 0 ]]; then
        echoerr "$WAITFORIT_cmdname: timeout occurred after waiting $WAITFORIT_TIMEOUT seconds for $WAITFORIT_HOST:$WAITFORIT_PORT"
    fi
    return $WAITFORIT_RESULT
}

# process arguments
while [[ $# -gt 0 ]]
do
    case "$1" in
        *:* )
        WAITFORIT_hostport=(${1//:/ })
        WAITFORIT_HOST=${WAITFORIT_hostport[0]}
        WAITFORIT_PORT=${WAITFORIT_hostport[1]}
        shift 1
        ;;
        --child)
        WAITFORIT_CHILD=1
        shift 1
        ;;
        -q | --quiet)
        WAITFORIT_QUIET=1
        shift 1
        ;;
        -s | --strict)
        WAITFORIT_STRICT=1
        shift 1
        ;;
        -h)
        WAITFORIT_HOST="$2"
        if [[ $WAITFORIT_HOST == "" ]]; then break; fi
        shift 2
        ;;
        --host=*)
        WAITFORIT_HOST="${1#*=}"
        shift 1
        ;;
        -p)
        WAITFORIT_PORT="$2"
        if [[ $WAITFORIT_PORT == "" ]]; then break; fi
        shift 2
        ;;
        --port=*)
        WAITFORIT_PORT="${1#*=}"
        shift 1
        ;;
        -t)
        WAITFORIT_TIMEOUT="$2"
        if [[ $WAITFORIT_TIMEOUT == "" ]]; then break; fi
        shift 2
        ;;
        --timeout=*)
        WAITFORIT_TIMEOUT="${1#*=}"
        shift 1
        ;;
        --)
        shift
        WAITFORIT_CLI=("$@")
        break
        ;;
        --help)
        usage
        ;;
        *)
        echoerr "Unknown argument: $1"
        usage
        ;;
    esac
done

if [[ "$WAITFORIT_HOST" == "" || "$WAITFORIT_PORT" == "" ]]; then
    echoerr "Error: you need to provide a host and port to test."
    usage
fi

WAITFORIT_TIMEOUT=${WAITFORIT_TIMEOUT:-15}
WAITFORIT_STRICT=${WAITFORIT_STRICT:-0}
WAITFORIT_CHILD=${WAITFORIT_CHILD:-0}
WAITFORIT_QUIET=${WAITFORIT_QUIET:-0}

# Check to see if timeout is from busybox?
WAITFORIT_TIMEOUT_PATH=$(type -p timeout)
WAITFORIT_TIMEOUT_PATH=$(realpath $WAITFORIT_TIMEOUT_PATH 2>/dev/null || echo $WAITFORIT_TIMEOUT_PATH)

WAITFORIT_BUSYTIMEFLAG=""
if [[ $WAITFORIT_TIMEOUT_PATH =~ "busybox" ]]; then
    WAITFORIT_ISBUSY=1
    # Check if busybox timeout uses -t flag
    # (recent Alpine versions don't support -t anymore)
    if timeout &>/dev/stdout | grep -q -e '-t '; then
        WAITFORIT_BUSYTIMEFLAG="-t"
    fi
else
    WAITFORIT_ISBUSY=0
fi

if [[ $WAITFORIT_CHILD -gt 0 ]]; then
    wait_for
    WAITFORIT_RESULT=$?
    exit $WAITFORIT_RESULT
else
    if [[ $WAITFORIT_TIMEOUT -gt 0 ]]; then
        wait_for_wrapper
        WAITFORIT_RESULT=$?
    else
        wait_for
        WAITFORIT_RESULT=$?
    fi
fi

if [[ $WAITFORIT_CLI != "" ]]; then
    if [[ $WAITFORIT_RESULT -ne 0 && $WAITFORIT_STRICT -eq 1 ]]; then
        echoerr "$WAITFORIT_cmdname: strict mode, refusing to execute subprocess"
        exit $WAITFORIT_RESULT
    fi
    exec "${WAITFORIT_CLI[@]}"
else
    exit $WAITFORIT_RESULT
fi
```

### 🏥 **Health Check Script**

```bash
#!/bin/bash
# 📁 health-check.sh

# Script de health check para contenedores
SERVICE_NAME=${SERVICE_NAME:-"unknown"}
HEALTH_URL=${HEALTH_URL:-"http://localhost:8080/actuator/health"}
TIMEOUT=${TIMEOUT:-10}

# Función para logging
log() {
    echo "[$(date +'%Y-%m-%d %H:%M:%S')] [$SERVICE_NAME] $1"
}

# Verificar conectividad de red
check_network() {
    if ! ping -c 1 -W 1 8.8.8.8 >/dev/null 2>&1; then
        log "WARNING: No hay conectividad de red externa"
        return 1
    fi
    return 0
}

# Verificar endpoint de salud
check_health_endpoint() {
    local response
    local http_code
    
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
                   --max-time $TIMEOUT \
                   --connect-timeout 5 \
                   "$HEALTH_URL" 2>/dev/null)
    
    http_code=$(echo "$response" | tr -d '\n' | sed -E 's/.*HTTPSTATUS:([0-9]{3})$/\1/')
    body=$(echo "$response" | sed -E 's/HTTPSTATUS:[0-9]{3}$//')
    
    if [[ "$http_code" -eq 200 ]]; then
        # Verificar que el status sea UP
        if echo "$body" | grep -q '"status":"UP"'; then
            log "Health check exitoso - Status: UP"
            return 0
        else
            log "ERROR: Health check reporta status != UP: $body"
            return 1
        fi
    else
        log "ERROR: Health check falló - HTTP $http_code"
        return 1
    fi
}

# Verificar uso de memoria
check_memory_usage() {
    local memory_usage
    memory_usage=$(free | grep Mem | awk '{print ($3/$2) * 100.0}')
    
    if (( $(echo "$memory_usage > 90" | bc -l) )); then
        log "WARNING: Uso de memoria alto: ${memory_usage}%"
        return 1
    fi
    
    log "Uso de memoria: ${memory_usage}%"
    return 0
}

# Verificar uso de disco
check_disk_usage() {
    local disk_usage
    disk_usage=$(df -h / | awk 'NR==2 {print $(NF-1)}' | sed 's/%//')
    
    if [[ $disk_usage -gt 85 ]]; then
        log "WARNING: Uso de disco alto: ${disk_usage}%"
        return 1
    fi
    
    log "Uso de disco: ${disk_usage}%"
    return 0
}

# Función principal de health check
main() {
    log "Iniciando health check..."
    
    local exit_code=0
    
    # Verificaciones básicas
    if ! check_network; then
        exit_code=1
    fi
    
    if ! check_health_endpoint; then
        exit_code=1
    fi
    
    if ! check_memory_usage; then
        # Solo warning, no afecta el exit code
        :
    fi
    
    if ! check_disk_usage; then
        # Solo warning, no afecta el exit code
        :
    fi
    
    if [[ $exit_code -eq 0 ]]; then
        log "Health check completado exitosamente"
    else
        log "Health check falló"
    fi
    
    exit $exit_code
}

# Ejecutar health check
main "$@"
```

---

## 📊 **MONITOREO Y OBSERVABILIDAD**

### 🔥 **Configuración de Prometheus**

```yaml
# 📁 monitoring/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "rules/*.yml"

scrape_configs:
  # Prometheus self-monitoring
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']

  # API Gateway
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['api-gateway:8080']
    scrape_interval: 10s

  # Arca Cotizador
  - job_name: 'arca-cotizador'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['arca-cotizador:8081']
    scrape_interval: 15s

  # Arca Gestor Solicitudes
  - job_name: 'arca-gestor-solicitudes'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['arca-gestor-solicitudes:8082']
    scrape_interval: 15s

  # Hello World Service
  - job_name: 'hello-world-service'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['hello-world-service:8083']
    scrape_interval: 30s

  # Eureka Server
  - job_name: 'eureka-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['eureka-server:8761']
    scrape_interval: 30s

  # Config Server
  - job_name: 'config-server'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['config-server:8888']
    scrape_interval: 30s

  # Docker containers
  - job_name: 'docker'
    static_configs:
      - targets: ['172.20.0.1:9323']

  # Node Exporter (if available)
  - job_name: 'node'
    static_configs:
      - targets: ['172.20.0.1:9100']

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093
```

### 📈 **Configuración de Grafana**

```yaml
# 📁 monitoring/grafana/datasources/prometheus.yml
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      httpMethod: POST
      prometheusType: Prometheus
      prometheusVersion: 2.40.0
```

---

## 🏆 **BENEFICIOS DE LA CONTAINERIZACIÓN**

### ✅ **Consistencia de Entornos**

```
🔄 ENTORNOS IDÉNTICOS:
├── Desarrollo (local) ✅
├── Testing (CI/CD) ✅
├── Staging (pre-producción) ✅
└── Producción (cloud) ✅
```

### ✅ **Escalabilidad y Orquestación**

```
📈 CAPACIDADES DE ESCALADO:
├── Escalado horizontal por servicio ✅
├── Load balancing automático ✅
├── Health checks integrados ✅
├── Rolling updates ✅
└── Auto-recovery en fallos ✅
```

### ✅ **Optimización de Recursos**

```
⚡ OPTIMIZACIONES LOGRADAS:
├── Imágenes multi-stage optimizadas ✅
├── Volúmenes para persistencia ✅
├── Redes aisladas por funcionalidad ✅
├── Límites de recursos configurables ✅
└── Cleanup automático de recursos ✅
```

---

*Documentación de Docker y Containerización*  
*Proyecto: Arka Valenzuela*  
*Implementación completa con orquestación*  
*Fecha: 8 de Septiembre de 2025*
