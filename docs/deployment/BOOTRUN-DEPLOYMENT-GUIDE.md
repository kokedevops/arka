# 🚀 Despliegue con Gradle bootRun - Guía Completa

## 📋 Resumen

Este proyecto está configurado para ejecutar cada microservicio usando **Gradle bootRun**. Cada servicio puede ejecutarse de forma independiente o todos juntos usando scripts automatizados.

## 🏗️ Arquitectura de Microservicios

```
┌─────────────────────────────────────────────────────────────────┐
│                     🌐 Ecosystem Overview                      │
├─────────────────────────────────────────────────────────────────┤
│ 1️⃣ Config Server (Puerto: 8888)                               │
│    ├─ Configuración centralizada                               │
│    └─ Base para todos los servicios                            │
├─────────────────────────────────────────────────────────────────┤
│ 2️⃣ Eureka Server (Puerto: 8761)                               │
│    ├─ Service Discovery                                         │
│    └─ Registry de microservicios                               │
├─────────────────────────────────────────────────────────────────┤
│ 3️⃣ API Gateway (Puerto: 8080)                                 │
│    ├─ Punto de entrada único                                   │
│    ├─ Routing y Load Balancing                                 │
│    └─ Security y Rate Limiting                                 │
├─────────────────────────────────────────────────────────────────┤
│ 4️⃣ Business Services                                           │
│    ├─ Hello World Service (Puerto: 8081)                       │
│    ├─ Arca Cotizador (Puerto: 8082)                           │
│    └─ Arca Gestor Solicitudes (Puerto: 8083)                  │
└─────────────────────────────────────────────────────────────────┘
```

## 🔧 Comandos Individuales por Servicio

### 1️⃣ Config Server (PRIMERO)
```powershell
# Comando básico
.\gradlew.bat :config-server:bootRun

# Con perfiles específicos
.\gradlew.bat :config-server:bootRun --args="--spring.profiles.active=dev"
```

### 2️⃣ Eureka Server (SEGUNDO)
```powershell
# Comando básico
.\gradlew.bat :eureka-server:bootRun

# Con configuración personalizada
.\gradlew.bat :eureka-server:bootRun --args="--server.port=8761"
```

### 3️⃣ API Gateway (TERCERO)
```powershell
# Comando básico
.\gradlew.bat :api-gateway:bootRun

# Con debug habilitado
.\gradlew.bat :api-gateway:bootRun --debug-jvm
```

### 4️⃣ Microservicios de Negocio

#### Hello World Service
```powershell
.\gradlew.bat :hello-world-service:bootRun
```

#### Arca Cotizador
```powershell
.\gradlew.bat :arca-cotizador:bootRun
```

#### Arca Gestor Solicitudes
```powershell
.\gradlew.bat :arca-gestor-solicitudes:bootRun
```

## 🎯 Orden de Ejecución Recomendado

**⚠️ IMPORTANTE:** Los servicios deben iniciarse en este orden específico:

1. **Config Server** (8888) - Configuración centralizada
2. **Eureka Server** (8761) - Service Discovery
3. **API Gateway** (8080) - Punto de entrada
4. **Microservicios** (8081, 8082, 8083...) - Servicios de negocio

## 🚀 Scripts Automatizados

### Script para Windows PowerShell

```powershell
# start-all-services.ps1
Write-Host "🚀 Iniciando todos los microservicios..." -ForegroundColor Green

# 1. Config Server
Write-Host "1️⃣ Iniciando Config Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; .\gradlew.bat :config-server:bootRun"
Start-Sleep -Seconds 30

# 2. Eureka Server
Write-Host "2️⃣ Iniciando Eureka Server..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; .\gradlew.bat :eureka-server:bootRun"
Start-Sleep -Seconds 20

# 3. API Gateway
Write-Host "3️⃣ Iniciando API Gateway..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; .\gradlew.bat :api-gateway:bootRun"
Start-Sleep -Seconds 15

# 4. Microservicios
Write-Host "4️⃣ Iniciando microservicios de negocio..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; .\gradlew.bat :hello-world-service:bootRun"
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; .\gradlew.bat :arca-cotizador:bootRun"
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; .\gradlew.bat :arca-gestor-solicitudes:bootRun"

Write-Host "✅ Todos los servicios iniciados!" -ForegroundColor Green
Write-Host "🌐 URLs disponibles:" -ForegroundColor Cyan
Write-Host "   - Config Server: http://localhost:8888"
Write-Host "   - Eureka Server: http://localhost:8761"
Write-Host "   - API Gateway: http://localhost:8080"
Write-Host "   - Hello World: http://localhost:8081"
Write-Host "   - Cotizador: http://localhost:8082"
Write-Host "   - Gestor: http://localhost:8083"
```

### Script para Linux/Mac

```bash
#!/bin/bash
# start-all-services.sh

echo "🚀 Iniciando todos los microservicios..."

# 1. Config Server
echo "1️⃣ Iniciando Config Server..."
gnome-terminal --tab --title="Config Server" -- bash -c "./gradlew :config-server:bootRun; exec bash"
sleep 30

# 2. Eureka Server
echo "2️⃣ Iniciando Eureka Server..."
gnome-terminal --tab --title="Eureka Server" -- bash -c "./gradlew :eureka-server:bootRun; exec bash"
sleep 20

# 3. API Gateway
echo "3️⃣ Iniciando API Gateway..."
gnome-terminal --tab --title="API Gateway" -- bash -c "./gradlew :api-gateway:bootRun; exec bash"
sleep 15

# 4. Microservicios
echo "4️⃣ Iniciando microservicios de negocio..."
gnome-terminal --tab --title="Hello World" -- bash -c "./gradlew :hello-world-service:bootRun; exec bash"
gnome-terminal --tab --title="Cotizador" -- bash -c "./gradlew :arca-cotizador:bootRun; exec bash"
gnome-terminal --tab --title="Gestor" -- bash -c "./gradlew :arca-gestor-solicitudes:bootRun; exec bash"

echo "✅ Todos los servicios iniciados!"
echo "🌐 URLs disponibles:"
echo "   - Config Server: http://localhost:8888"
echo "   - Eureka Server: http://localhost:8761"
echo "   - API Gateway: http://localhost:8080"
echo "   - Hello World: http://localhost:8081"
echo "   - Cotizador: http://localhost:8082"
echo "   - Gestor: http://localhost:8083"
```

## 🔍 Verificación de Servicios

### Health Checks
```powershell
# Verificar todos los servicios
function Test-ServiceHealth {
    $services = @(
        @{Name="Config Server"; Url="http://localhost:8888/actuator/health"},
        @{Name="Eureka Server"; Url="http://localhost:8761/actuator/health"},
        @{Name="API Gateway"; Url="http://localhost:8080/actuator/health"},
        @{Name="Hello World"; Url="http://localhost:8081/actuator/health"},
        @{Name="Cotizador"; Url="http://localhost:8082/actuator/health"},
        @{Name="Gestor"; Url="http://localhost:8083/actuator/health"}
    )
    
    foreach ($service in $services) {
        try {
            $response = Invoke-RestMethod -Uri $service.Url -TimeoutSec 5
            if ($response.status -eq "UP") {
                Write-Host "✅ $($service.Name): UP" -ForegroundColor Green
            } else {
                Write-Host "❌ $($service.Name): DOWN" -ForegroundColor Red
            }
        } catch {
            Write-Host "❌ $($service.Name): NO RESPONSE" -ForegroundColor Red
        }
    }
}

# Ejecutar health check
Test-ServiceHealth
```

### Verificar Registry de Eureka
```powershell
# Ver servicios registrados en Eureka
Invoke-RestMethod -Uri "http://localhost:8761/eureka/apps" -Headers @{"Accept"="application/json"} | ConvertTo-Json -Depth 5
```

## 🐛 Debugging y Logs

### Ejecutar con Debug
```powershell
# Con debug de JVM
.\gradlew.bat :arca-cotizador:bootRun --debug-jvm

# Con logs de debug de Spring
.\gradlew.bat :arca-cotizador:bootRun --args="--logging.level.com.arka=DEBUG"

# Con perfiles específicos
.\gradlew.bat :arca-cotizador:bootRun --args="--spring.profiles.active=dev,debug"
```

### Variables de Entorno
```powershell
# Configurar memoria JVM
$env:JAVA_OPTS="-Xmx1024m -Xms512m"
.\gradlew.bat :arca-cotizador:bootRun

# Configurar puerto personalizado
.\gradlew.bat :arca-cotizador:bootRun --args="--server.port=9082"
```

## 🔄 Desarrollo en Caliente (Hot Reload)

### Con Spring Boot DevTools
```powershell
# Los servicios ya incluyen DevTools para hot reload
.\gradlew.bat :arca-cotizador:bootRun

# En otra terminal, recompilar automáticamente
.\gradlew.bat :arca-cotizador:classes --continuous
```

### Con Gradle Continuous Build
```powershell
# Recompilación automática al detectar cambios
.\gradlew.bat :arca-cotizador:bootRun --continuous
```

## 📊 Monitoreo durante Desarrollo

### Endpoints de Actuator Disponibles
```
# Config Server (8888)
http://localhost:8888/actuator/health
http://localhost:8888/actuator/info
http://localhost:8888/actuator/env

# Eureka Server (8761)
http://localhost:8761/
http://localhost:8761/actuator/health

# API Gateway (8080)
http://localhost:8080/actuator/health
http://localhost:8080/actuator/routes
http://localhost:8080/actuator/gateway/routes

# Microservicios (808X)
http://localhost:808X/actuator/health
http://localhost:808X/actuator/metrics
http://localhost:808X/actuator/info
```

## 🛑 Detener Servicios

### Detener individualmente
```powershell
# Buscar procesos Java
Get-Process java

# Detener por PID
Stop-Process -Id <PID>

# O usar Ctrl+C en cada terminal
```

### Script para detener todos
```powershell
# stop-all-services.ps1
Write-Host "🛑 Deteniendo todos los servicios..." -ForegroundColor Yellow

# Detener todos los procesos Java de Gradle
Get-Process | Where-Object {$_.ProcessName -eq "java" -and $_.CommandLine -like "*gradle*"} | Stop-Process -Force

Write-Host "✅ Todos los servicios detenidos!" -ForegroundColor Green
```

## 🔧 Configuración Avanzada

### Perfiles de Spring
```yaml
# application-dev.yml
server:
  port: ${PORT:8082}
spring:
  profiles:
    active: dev
logging:
  level:
    com.arka: DEBUG
    org.springframework.cloud: DEBUG
```

### JVM Options Personalizadas
```powershell
# Configurar en gradle.properties
org.gradle.jvmargs=-Xmx2048m -Xms1024m -XX:+UseG1GC

# O por servicio específico
.\gradlew.bat :arca-cotizador:bootRun -Dspring.profiles.active=dev -Xmx1024m
```

## 🚀 Quick Start Commands

### Desarrollo Completo
```powershell
# Terminal 1: Config Server
.\gradlew.bat :config-server:bootRun

# Terminal 2: Eureka Server (esperar 30s)
.\gradlew.bat :eureka-server:bootRun

# Terminal 3: API Gateway (esperar 20s)
.\gradlew.bat :api-gateway:bootRun

# Terminal 4: Cotizador (esperar 15s)
.\gradlew.bat :arca-cotizador:bootRun

# Terminal 5: Gestor
.\gradlew.bat :arca-gestor-solicitudes:bootRun
```

### Solo un servicio para desarrollo
```powershell
# Desarrollo independiente (sin Eureka)
.\gradlew.bat :arca-cotizador:bootRun --args="--eureka.client.enabled=false"
```

## 📱 URLs de Acceso Rápido

- **🏠 Home**: http://localhost:8080
- **🔍 Eureka Dashboard**: http://localhost:8761
- **⚙️ Config Server**: http://localhost:8888
- **🌍 Hello World**: http://localhost:8081/hello
- **💰 Cotizador**: http://localhost:8082/cotizaciones
- **📋 Gestor**: http://localhost:8083/solicitudes

## 💡 Quick Start Commands

### Desarrollo Completo (Recomendado)
```powershell
# 🚀 Script automatizado - Inicia todos los servicios
.\start-all-services.ps1

# ✅ Verificar que todo esté funcionando
.\check-services-health.ps1

# 🛑 Detener todos los servicios
.\stop-all-services.ps1
```

### Desarrollo Individual
```powershell
# 🎯 Iniciar solo un servicio específico
.\quick-start.ps1 -Service arca-cotizador

# 🔗 Iniciar un servicio con sus dependencias
.\quick-start.ps1 -Service arca-cotizador -WithDependencies

# 🐛 Iniciar en modo debug
.\quick-start.ps1 -Service arca-cotizador -Debug -Profile dev
```

### Comandos Gradle Directos
```powershell
# 📋 Ver todas las tareas disponibles
.\gradlew.bat tasks --group="Arka Deployment"

# 🚀 Iniciar todos los servicios (usando Gradle)
.\gradlew.bat startAllServices

# 🔍 Health check completo
.\gradlew.bat checkHealth

# 🛑 Detener todos los servicios
.\gradlew.bat stopAllServices
```

## 🎯 Desarrollo por Servicio

### 🔧 Config Server
```powershell
# Básico
.\gradlew.bat :config-server:bootRun

# Con perfil específico
.\gradlew.bat :config-server:bootRun --args="--spring.profiles.active=dev"

# Script automatizado
.\quick-start.ps1 -Service config-server
```

### 🔍 Eureka Server
```powershell
# Básico (requiere Config Server)
.\gradlew.bat :eureka-server:bootRun

# Con dependencias automáticas
.\quick-start.ps1 -Service eureka-server -WithDependencies
```

### 🌐 API Gateway
```powershell
# Básico (requiere Config Server y Eureka)
.\gradlew.bat :api-gateway:bootRun

# Con dependencias automáticas
.\quick-start.ps1 -Service api-gateway -WithDependencies
```

### 💰 Arca Cotizador
```powershell
# Básico
.\gradlew.bat :arca-cotizador:bootRun

# Con dependencias
.\quick-start.ps1 -Service arca-cotizador -WithDependencies

# Standalone (sin Eureka) para desarrollo
.\gradlew.bat :arca-cotizador:bootRun --args="--eureka.client.enabled=false"
```

### 📋 Arca Gestor Solicitudes
```powershell
# Básico
.\gradlew.bat :arca-gestor-solicitudes:bootRun

# Con dependencias
.\quick-start.ps1 -Service arca-gestor-solicitudes -WithDependencies
```

## 🔧 Configuración Avanzada

### Variables de Entorno
```powershell
# Configurar memoria JVM
$env:GRADLE_OPTS = "-Xmx2048m -Xms1024m"

# Configurar perfil por defecto
$env:SPRING_PROFILES_ACTIVE = "dev"

# Ejecutar servicio
.\gradlew.bat :arca-cotizador:bootRun
```

### Puertos Personalizados
```powershell
# Cotizador en puerto 9082
.\gradlew.bat :arca-cotizador:bootRun --args="--server.port=9082"

# Con script automatizado
.\quick-start.ps1 -Service arca-cotizador -Port 9082
```

### Debug Remoto
```powershell
# Habilitar debug JVM
.\gradlew.bat :arca-cotizador:bootRun --debug-jvm

# Debug en puerto específico
.\gradlew.bat :arca-cotizador:bootRun --args="--spring.profiles.active=dev" --debug-jvm
```

## 📊 Monitoreo y Logs

### Endpoints de Actuator
```bash
# Health checks
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8761/actuator/health  # Eureka Server  
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8082/actuator/health  # Cotizador
curl http://localhost:8083/actuator/health  # Gestor
```

### Información del Sistema
```bash
# Info del servicio
curl http://localhost:8082/actuator/info

# Métricas
curl http://localhost:8082/actuator/metrics

# Variables de entorno
curl http://localhost:8082/actuator/env
```

### Logs en Tiempo Real
Los logs aparecen directamente en la consola de cada servicio. Para logs más detallados:

```powershell
# Ejecutar con logs debug
.\gradlew.bat :arca-cotizador:bootRun --args="--logging.level.com.arka=DEBUG"

# Logs de Spring Cloud
.\gradlew.bat :arca-cotizador:bootRun --args="--logging.level.org.springframework.cloud=DEBUG"
```

## 🔄 Hot Reload y Desarrollo

### Spring Boot DevTools
Todos los servicios incluyen DevTools para hot reload automático:

```powershell
# Iniciar con DevTools habilitado
.\gradlew.bat :arca-cotizador:bootRun -Pdev

# En otra terminal, compilación continua
.\gradlew.bat :arca-cotizador:classes --continuous
```

### Compilación Automática
```powershell
# Compilación continua de todos los módulos
.\gradlew.bat classes --continuous

# Solo un módulo específico
.\gradlew.bat :arca-cotizador:classes --continuous
```

## 🧪 Testing durante Desarrollo

### Tests Unitarios
```powershell
# Ejecutar tests de un servicio
.\gradlew.bat :arca-cotizador:test

# Tests con coverage
.\gradlew.bat :arca-cotizador:jacocoTestReport
```

### Tests de Integración
```powershell
# Tests de integración
.\gradlew.bat :arca-cotizador:integrationTest

# Tests completos
.\gradlew.bat :arca-cotizador:check
```

## 🛠️ Troubleshooting

### Problemas Comunes

#### Puerto en Uso
```powershell
# Encontrar proceso que usa el puerto
netstat -ano | findstr :8082

# Terminar proceso por PID
Stop-Process -Id <PID> -Force
```

#### Servicio No Inicia
```powershell
# Verificar configuración
.\gradlew.bat :arca-cotizador:bootRun --info

# Ver logs detallados
.\gradlew.bat :arca-cotizador:bootRun --debug
```

#### Error de Conexión a Eureka
```powershell
# Verificar que Eureka esté ejecutándose
curl http://localhost:8761/

# Iniciar solo el servicio sin Eureka
.\gradlew.bat :arca-cotizador:bootRun --args="--eureka.client.enabled=false"
```

### Comandos de Diagnóstico
```powershell
# Ver procesos Java
Get-Process java

# Ver puertos en uso
netstat -ano | findstr :808

# Health check automatizado
.\check-services-health.ps1
```

## 📱 URLs de Acceso Rápido

### Servicios Principales
- **🏠 API Gateway**: http://localhost:8080
- **🔍 Eureka Dashboard**: http://localhost:8761
- **⚙️ Config Server**: http://localhost:8888

### Servicios de Negocio
- **👋 Hello World**: http://localhost:8081/hello
- **💰 Cotizador**: http://localhost:8082/cotizaciones  
- **📋 Gestor**: http://localhost:8083/solicitudes

### Health Checks
- **Config**: http://localhost:8888/actuator/health
- **Eureka**: http://localhost:8761/actuator/health
- **Gateway**: http://localhost:8080/actuator/health
- **Cotizador**: http://localhost:8082/actuator/health
- **Gestor**: http://localhost:8083/actuator/health

## 🎉 ¡Listo para Desarrollar!

Tu ecosistema de microservicios está configurado para:
- ✅ **Desarrollo ágil** con hot reload
- ✅ **Debugging completo** con puntos de interrupción
- ✅ **Monitoreo en tiempo real** con health checks
- ✅ **Escalabilidad local** con múltiples instancias
- ✅ **Testing integrado** con coverage reports

¡Disfruta desarrollando con Arka! 🚀