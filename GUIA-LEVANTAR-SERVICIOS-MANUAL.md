# 🚀 Guía Completa: Levantar y Probar Todos los Servicios ARKA

## 📋 Índice
1. [Prerrequisitos](#prerrequisitos)
2. [Orden de Inicio de Servicios](#orden-de-inicio-de-servicios)
3. [Configuración de Base de Datos](#configuración-de-base-de-datos)
4. [Servicios Principales](#servicios-principales)
5. [Servicios de Negocio](#servicios-de-negocio)
6. [Servicios Avanzados](#servicios-avanzados)
7. [Verificación de Servicios](#verificación-de-servicios)
8. [Pruebas Funcionales](#pruebas-funcionales)
9. [Solución de Problemas](#solución-de-problemas)

---

## 🔧 Prerrequisitos

### 📦 Software Requerido
```powershell
# Verificar Java 21
java -version

# Verificar Gradle
gradle -version

# Verificar Git
git --version

# Verificar PowerShell (versión 5.1+)
$PSVersionTable.PSVersion
```

### 🗄️ Base de Datos
```powershell
# MongoDB (para Reportes y Saga Tracker)
# Descargar desde: https://www.mongodb.com/try/download/community
# O usar Docker:
docker run -d --name mongodb -p 27017:27017 mongo:7

# Verificar conexión MongoDB
mongo --eval "db.adminCommand('ismaster')"
```

### ☁️ Servicios AWS (Opcional para desarrollo)
```powershell
# Instalar LocalStack para simulación AWS local
pip install localstack
localstack start

# O configurar AWS CLI con credenciales reales
aws configure
```

---

## 🎯 Orden de Inicio de Servicios

### 📊 **Secuencia Recomendada:**
1. **Config Server** (Puerto 8888)
2. **Eureka Server** (Puerto 8761)
3. **API Gateway** (Puerto 8080)
4. **Servicios de Negocio** (Puertos 8081-8088)
5. **Servicios Avanzados** (Puertos 8089+)

---

## 🏗️ Configuración de Base de Datos

### 🍃 MongoDB Setup
```powershell
# 1. Crear directorios de datos
mkdir C:\data\db -Force

# 2. Iniciar MongoDB (Windows)
mongod --dbpath C:\data\db --port 27017

# 3. Verificar conexión
mongo mongodb://localhost:27017

# 4. Crear bases de datos (se crean automáticamente en primer uso)
# - reportes_db (para arca-reportes)
# - saga_tracking (para saga-state-tracker)
```

### 🗄️ H2 Database (Para desarrollo)
```powershell
# Los servicios usan H2 en memoria por defecto
# No requiere configuración adicional para testing
```

---

## 🏗️ Servicios Principales

### 1. **Config Server** 📋
```powershell
# Terminal 1 - Config Server
cd C:\Users\valen\arkavalenzuela-1
./gradlew :config-server:bootRun

# Verificar en navegador: http://localhost:8888/actuator/health
# Configuraciones disponibles: http://localhost:8888/application/default
```

**⏳ Esperar**: Servicio completamente iniciado antes de continuar.

### 2. **Eureka Server** 🌐
```powershell
# Terminal 2 - Eureka Server
cd C:\Users\valen\arkavalenzuela-1
./gradlew :eureka-server:bootRun

# Verificar en navegador: http://localhost:8761
# Panel de Control Eureka: http://localhost:8761/eureka/web
```

**⏳ Esperar**: Ver "Eureka Server started" en los logs.

### 3. **API Gateway** 🚪
```powershell
# Terminal 3 - API Gateway
cd C:\Users\valen\arkavalenzuela-1
./gradlew :api-gateway:bootRun

# Verificar: http://localhost:8080/actuator/health
# Rutas disponibles: http://localhost:8080/actuator/gateway/routes
```

---

## 🏢 Servicios de Negocio

### 4. **Hello World Service** 👋
```powershell
# Terminal 4 - Hello World Service
cd C:\Users\valen\arkavalenzuela-1
./gradlew :hello-world-service:bootRun

# Puerto: 8081
# Salud: http://localhost:8081/actuator/health
# Prueba: http://localhost:8081/hello
```

### 5. **Arca Cotizador** 💰
```powershell
# Terminal 5 - Arca Cotizador
cd C:\Users\valen\arkavalenzuela-1
./gradlew :arca-cotizador:bootRun

# Puerto: 8082
# Salud: http://localhost:8082/actuator/health
# API: http://localhost:8082/api/cotizaciones
```

### 6. **Arca Gestor Solicitudes** 📝
```powershell
# Terminal 6 - Arca Gestor Solicitudes
cd C:\Users\valen\arkavalenzuela-1
./gradlew :arca-gestor-solicitudes:bootRun

# Puerto: 8083
# Salud: http://localhost:8083/actuator/health
# API: http://localhost:8083/api/solicitudes
```

---

## 🔧 Servicios Avanzados

### 7. **Arca Reportes** 📊
```powershell
# Terminal 7 - Arca Reportes
cd C:\Users\valen\arkavalenzuela-1

# Verificar que MongoDB esté funcionando
mongo --eval "db.runCommand({ping: 1})"

# Iniciar servicio
./gradlew :arca-reportes:bootRun

# Puerto: 8088
# Salud: http://localhost:8088/actuator/health
# API: http://localhost:8088/api/reportes
```

### 8. **Saga State Tracker** 🔄
```powershell
# Terminal 8 - Saga State Tracker
cd C:\Users\valen\arkavalenzuela-1

# Verificar MongoDB
mongo saga_tracking --eval "db.stats()"

# Iniciar servicio
./gradlew :saga-state-tracker:bootRun

# Puerto: 8089
# Salud: http://localhost:8089/saga-tracker/actuator/health
# Panel de Control: http://localhost:8089/saga-tracker/api/saga/dashboard
```

---

## ✅ Verificación de Servicios

### 🏥 Verificaciones de Salud Automáticas
```powershell
# Script para verificar todos los servicios
# Ejecutar desde la raíz del proyecto

# Verificar servicios principales
$servicios = @(
    @{nombre="Config Server"; url="http://localhost:8888/actuator/health"},
    @{nombre="Eureka Server"; url="http://localhost:8761/actuator/health"},
    @{nombre="API Gateway"; url="http://localhost:8080/actuator/health"},
    @{nombre="Hello World"; url="http://localhost:8081/actuator/health"},
    @{nombre="Arca Cotizador"; url="http://localhost:8082/actuator/health"},
    @{nombre="Arca Gestor"; url="http://localhost:8083/actuator/health"},
    @{nombre="Arca Reportes"; url="http://localhost:8088/actuator/health"},
    @{nombre="Saga Tracker"; url="http://localhost:8089/saga-tracker/actuator/health"}
)

foreach ($servicio in $servicios) {
    try {
        $respuesta = Invoke-RestMethod -Uri $servicio.url -TimeoutSec 5
        if ($respuesta.status -eq "UP") {
            Write-Host "✅ $($servicio.nombre) - SALUDABLE" -ForegroundColor Green
        } else {
            Write-Host "⚠️ $($servicio.nombre) - $($respuesta.status)" -ForegroundColor Yellow
        }
    } catch {
        Write-Host "❌ $($servicio.nombre) - INACTIVO" -ForegroundColor Red
    }
}
```

### 🌐 Verificación en Eureka
```powershell
# Abrir navegador en Panel de Control Eureka
Start-Process "http://localhost:8761"

# Verificar que todos los servicios aparezcan registrados:
# - HELLO-WORLD-SERVICE
# - ARCA-COTIZADOR
# - ARCA-GESTOR-SOLICITUDES
# - ARCA-REPORTES
# - SAGA-STATE-TRACKER
```

---

## 🧪 Pruebas Funcionales

### 📋 **Prueba 1: Hello World Service**
```powershell
# Prueba básica
curl http://localhost:8081/hello

# A través del API Gateway
curl http://localhost:8080/hello-world/hello

# Respuesta esperada: "¡Hola desde ARKA Hello World Service!"
```

### 💰 **Prueba 2: Arca Cotizador**
```powershell
# Crear cotización
$cotizacion = @{
    clienteId = "CLI001"
    productos = @(
        @{ id = "PROD001"; cantidad = 2; precio = 100.0 },
        @{ id = "PROD002"; cantidad = 1; precio = 50.0 }
    )
} | ConvertTo-Json -Depth 3

# Enviar cotización
$respuesta = Invoke-RestMethod -Uri "http://localhost:8082/api/cotizaciones" -Method Post -Body $cotizacion -ContentType "application/json"

Write-Host "Cotización creada: $($respuesta.id)"
Write-Host "Total: $($respuesta.total)"

# Obtener cotización
curl "http://localhost:8082/api/cotizaciones/$($respuesta.id)"
```

### 📝 **Prueba 3: Arca Gestor Solicitudes**
```powershell
# Crear solicitud
$solicitud = @{
    clienteId = "CLI001"
    tipo = "NUEVA_POLIZA"
    descripcion = "Solicitud de nueva póliza de seguro"
    prioridad = "ALTA"
} | ConvertTo-Json

$respuesta = Invoke-RestMethod -Uri "http://localhost:8083/api/solicitudes" -Method Post -Body $solicitud -ContentType "application/json"

Write-Host "Solicitud creada: $($respuesta.id)"

# Listar solicitudes
curl "http://localhost:8083/api/solicitudes"
```

### 📊 **Prueba 4: Arca Reportes**
```powershell
# Verificar servicio
curl "http://localhost:8088/api/reportes/health"

# Generar reporte de carritos abandonados
curl "http://localhost:8088/api/reportes/carritos-abandonados?formato=JSON&diasAtras=7"

# Generar reporte PDF (descargar archivo)
curl "http://localhost:8088/api/reportes/productos-abastecer?formato=PDF" -OutFile "reporte-productos.pdf"

# Verificar reportes programados
curl "http://localhost:8088/api/reportes/programados"
```

### 🔄 **Prueba 5: Saga State Tracker**
```powershell
# Crear estado de saga de prueba
$actualizacionSaga = @{
    ordenId = "TEST-001"
    sagaId = "SAGA-TEST-001"
    tipoEvento = "INICIO_SAGA"
    timestamp = (Get-Date).ToString("yyyy-MM-ddTHH:mm:ss")
} | ConvertTo-Json

$respuesta = Invoke-RestMethod -Uri "http://localhost:8089/saga-tracker/api/saga/actualizar-estado" -Method Post -Body $actualizacionSaga -ContentType "application/json"

# Consultar estado
curl "http://localhost:8089/saga-tracker/api/saga/estado/TEST-001"

# Ver panel de control
curl "http://localhost:8089/saga-tracker/api/saga/dashboard?horas=24"
```

---

## 🧪 Scripts de Prueba Automatizados

### 📋 **Ejecutar Todas las Pruebas**
```powershell
# Prueba completa de todos los servicios
.\test-all-services.ps1

# Prueba específica de reportes
.\test-reportes-simple.ps1

# Prueba específica de saga tracker
.\test-actividad8-saga-tracker.ps1

# Prueba de integración completa
.\test-saga.bat
```

### 📊 **Monitoreo Continuo**
```powershell
# Script para monitoreo continuo (ejecutar en PowerShell)
while ($true) {
    Clear-Host
    Write-Host "🔄 ESTADO SERVICIOS ARKA - $(Get-Date)" -ForegroundColor Cyan
    Write-Host "=" * 50
    
    $servicios = @(
        @{nombre="Config Server"; puerto="8888"},
        @{nombre="Eureka Server"; puerto="8761"},
        @{nombre="API Gateway"; puerto="8080"},
        @{nombre="Hello World"; puerto="8081"},
        @{nombre="Cotizador"; puerto="8082"},
        @{nombre="Gestor Sol."; puerto="8083"},
        @{nombre="Reportes"; puerto="8088"},
        @{nombre="Saga Tracker"; puerto="8089"}
    )
    
    foreach ($servicio in $servicios) {
        $url = "http://localhost:$($servicio.puerto)/actuator/health"
        try {
            $respuesta = Invoke-RestMethod -Uri $url -TimeoutSec 2
            $estado = if ($respuesta.status -eq "UP") { "🟢 ACTIVO" } else { "🟡 $($respuesta.status)" }
        } catch {
            $estado = "🔴 INACTIVO"
        }
        Write-Host "$($servicio.nombre.PadRight(15)) [$($servicio.puerto)] - $estado"
    }
    
    Start-Sleep -Seconds 10
}
```

---

## 🛠️ Solución de Problemas

### ❌ **Problemas Comunes**

#### **Puerto ya en uso**
```powershell
# Verificar qué proceso usa el puerto
netstat -ano | findstr :8080

# Terminar proceso por PID
taskkill /PID <PID> /F

# O cambiar puerto en application.yml
server:
  port: 8081  # Cambiar a puerto disponible
```

#### **MongoDB no conecta**
```powershell
# Verificar si MongoDB está funcionando
tasklist | findstr mongod

# Iniciar MongoDB manualmente
mongod --dbpath C:\data\db

# Verificar conectividad
mongo --eval "db.adminCommand('ping')"
```

#### **Servicios no se registran en Eureka**
```powershell
# Verificar que Config Server esté funcionando primero
curl http://localhost:8888/actuator/health

# Verificar configuración en config-repository/application.yml
# Reiniciar servicio después de Eureka
```

#### **Gradle build falla**
```powershell
# Limpiar y reconstruir
./gradlew clean build

# Verificar dependencias
./gradlew dependencies

# Construir específico
./gradlew :arca-reportes:build --debug
```

#### **OutOfMemory en compilación**
```powershell
# Aumentar memoria de Gradle
$env:GRADLE_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"

# O editar gradle.properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=512m
```

### 🔍 **Logs y Depuración**

#### **Ver logs en tiempo real**
```powershell
# Logs con más detalle
./gradlew :arca-reportes:bootRun --debug

# O configurar logging en application.yml
logging:
  level:
    com.arka: DEBUG
    org.springframework: INFO
```

#### **Verificar configuración**
```powershell
# Ver configuración actual de un servicio
curl http://localhost:8088/actuator/configprops

# Ver variables de entorno
curl http://localhost:8088/actuator/env
```

---

## 🚀 **Secuencia Completa de Inicio**

### **Script Principal** (Ejecutar paso a paso)
```powershell
# 1. PREPARACIÓN
Write-Host "🔧 Preparando entorno..."
./gradlew clean build

# 2. BASES DE DATOS
Write-Host "🗄️ Iniciando MongoDB..."
Start-Process -FilePath "mongod" -ArgumentList "--dbpath C:\data\db"
Start-Sleep -Seconds 5

# 3. SERVICIOS PRINCIPALES (en terminales separadas)
Write-Host "📋 Iniciando Config Server..."
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./gradlew :config-server:bootRun"
Start-Sleep -Seconds 30

Write-Host "🌐 Iniciando Eureka Server..."
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./gradlew :eureka-server:bootRun"
Start-Sleep -Seconds 30

Write-Host "🚪 Iniciando API Gateway..."
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./gradlew :api-gateway:bootRun"
Start-Sleep -Seconds 20

# 4. SERVICIOS DE NEGOCIO
Write-Host "👋 Iniciando Hello World Service..."
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./gradlew :hello-world-service:bootRun"

Write-Host "💰 Iniciando Arca Cotizador..."
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./gradlew :arca-cotizador:bootRun"

Write-Host "📝 Iniciando Arca Gestor Solicitudes..."
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./gradlew :arca-gestor-solicitudes:bootRun"

Start-Sleep -Seconds 45

# 5. SERVICIOS AVANZADOS
Write-Host "📊 Iniciando Arca Reportes..."
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./gradlew :arca-reportes:bootRun"

Write-Host "🔄 Iniciando Saga State Tracker..."
Start-Process powershell -ArgumentList "-Command", "cd '$PWD'; ./gradlew :saga-state-tracker:bootRun"

# 6. VERIFICACIÓN
Start-Sleep -Seconds 60
Write-Host "✅ Verificando servicios..."
.\test-all-services.ps1

Write-Host "🚀 ¡Todos los servicios iniciados! Accede a http://localhost:8761 para ver el panel de control Eureka."
```

---

## 📋 **URLs de Referencia Rápida**

| Servicio | Puerto | Verificación de Salud | Panel de Control/API |
|----------|--------|----------------------|----------------------|
| **Config Server** | 8888 | [Salud](http://localhost:8888/actuator/health) | [Configuraciones](http://localhost:8888/application/default) |
| **Eureka Server** | 8761 | [Salud](http://localhost:8761/actuator/health) | [Panel de Control](http://localhost:8761) |
| **API Gateway** | 8080 | [Salud](http://localhost:8080/actuator/health) | [Rutas](http://localhost:8080/actuator/gateway/routes) |
| **Hello World** | 8081 | [Salud](http://localhost:8081/actuator/health) | [API](http://localhost:8081/hello) |
| **Arca Cotizador** | 8082 | [Salud](http://localhost:8082/actuator/health) | [API](http://localhost:8082/api/cotizaciones) |
| **Arca Gestor** | 8083 | [Salud](http://localhost:8083/actuator/health) | [API](http://localhost:8083/api/solicitudes) |
| **Arca Reportes** | 8088 | [Salud](http://localhost:8088/actuator/health) | [API](http://localhost:8088/api/reportes) |
| **Saga Tracker** | 8089 | [Salud](http://localhost:8089/saga-tracker/actuator/health) | [Panel de Control](http://localhost:8089/saga-tracker/api/saga/dashboard) |

---

## 🎯 **Siguientes Pasos**

Una vez que todos los servicios estén funcionando:

1. **🌐 Explorar Panel de Control Eureka**: http://localhost:8761
2. **📊 Ver Panel de Control de Saga**: http://localhost:8089/saga-tracker/api/saga/dashboard
3. **🧪 Ejecutar Pruebas**: `.\test-all-services.ps1`
4. **📈 Generar Reportes**: Usar endpoints de arca-reportes
5. **🔄 Probar Patrón Saga**: Usar APIs de saga-state-tracker

¡El ecosistema ARKA está listo para desarrollo y pruebas! 🚀
