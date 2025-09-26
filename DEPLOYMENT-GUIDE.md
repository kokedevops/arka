# 🚀 Guía de Despliegue - Proyecto Arka

## 📋 Configuración Final de Puertos

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **API Gateway** | `8085` | 🚪 Entrada principal del sistema |
| **Arca Cotizador** | `8081` | 💰 Servicio de cotizaciones |
| **Arca Gestor Solicitudes** | `8082` | 📝 Gestión de solicitudes |
| **Hello World Service** | `8083-8084` | 🌍 Servicio de prueba (múltiples instancias) |
| **Aplicación Principal** | `8090` | 🏠 Aplicación principal |
| **Eureka Server** | `8761` | 🔍 Descubrimiento de servicios |
| **Config Server** | `8888` | ⚙️ Servidor de configuración |

## 🔄 Orden de Despliegue

### ⚠️ **IMPORTANTE**: Seguir este orden estrictamente

```bash
# 1️⃣ Eureka Server (PRIMERO - Descubrimiento de servicios)
gradle :eureka-server:bootRun --args='--spring.profiles.active=aws'

# 2️⃣ Config Server (SEGUNDO - Configuración centralizada)
gradle :config-server:bootRun --args='--spring.profiles.active=aws'

# ⏳ Esperar 30-60 segundos para que se estabilicen los servicios base

# 3️⃣ API Gateway (TERCERO - Puerta de entrada)
gradle :api-gateway:bootRun --args='--spring.profiles.active=aws'

# 4️⃣ Microservicios (EN PARALELO)
gradle :arca-cotizador:bootRun --args='--spring.profiles.active=aws' &
gradle :arca-gestor-solicitudes:bootRun --args='--spring.profiles.active=aws' &
gradle :hello-world-service:bootRun --args='--spring.profiles.active=aws' &

# 5️⃣ Aplicación Principal (ÚLTIMO)
gradle bootRun --args='--spring.profiles.active=aws'
```

## 🔗 URLs de Acceso

Una vez desplegado correctamente:

- 🌐 **API Gateway**: http://localhost:8085
- 🔍 **Eureka Dashboard**: http://localhost:8761
- ⚙️ **Config Server**: http://localhost:8888
- 💰 **Arca Cotizador**: http://localhost:8081
- 📝 **Arca Gestor**: http://localhost:8082
- 🌍 **Hello World**: http://localhost:8083
- 🏠 **App Principal**: http://localhost:8090

## 🐳 Despliegue con Docker

```bash
# Construir todas las imágenes
docker-compose build

# Iniciar todos los servicios
docker-compose up -d

# Ver logs
docker-compose logs -f

# Detener servicios
docker-compose down
```

## 🔍 Verificación de Salud

```bash
# Verificar que Eureka ve todos los servicios
curl http://localhost:8761

# Verificar API Gateway
curl http://localhost:8085/actuator/health

# Verificar cada microservicio
curl http://localhost:8081/actuator/health  # Cotizador
curl http://localhost:8082/actuator/health  # Gestor
curl http://localhost:8083/actuator/health  # Hello World
```

## ⚡ Comandos Rápidos

### Inicio Secuencial (Recomendado para desarrollo)
```bash
./scripts/start-all-services.sh
```

### Inicio con Docker
```bash
./scripts/docker-start.sh
```

### Detener Servicios
```bash
./scripts/stop-all-services.sh
```

---
📝 **Nota**: Siempre verificar que cada servicio esté completamente levantado antes de iniciar el siguiente, especialmente Eureka y Config Server.