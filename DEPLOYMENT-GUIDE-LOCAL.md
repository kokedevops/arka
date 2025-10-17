# 🏠 Guía de Despliegue LOCAL - Proyecto Arka

## 📋 Configuración de Ambiente Local

### 🗄️ Base de Datos MySQL
- **Host**: `192.168.100.77`
- **Puerto**: `3306`
- **Base de datos**: `arkavalenzuelabd`
- **Usuario**: `root`
- **Contraseña**: `Koke1988****`

### 📊 Puertos de Servicios

| Servicio | Puerto | Descripción |
|----------|--------|-------------|
| **Eureka Server** | `8761` | 🔍 Descubrimiento de servicios |
| **Config Server** | `8888` | ⚙️ Servidor de configuración |
| **API Gateway** | `8085` | 🚪 Entrada principal del sistema |
| **Arca Cotizador** | `8081` | 💰 Servicio de cotizaciones |
| **Arca Gestor Solicitudes** | `8082` | 📝 Gestión de solicitudes |
| **Hello World Service** | `8083` | 🌍 Servicio de prueba |
| **Aplicación Principal** | `8090` | 🏠 Aplicación principal |

---

## 🚀 Inicio Rápido - Automático

### Opción 1: Script de PowerShell (Recomendado)

```powershell
# Iniciar todos los servicios
.\scripts\start-all-services-local.ps1

# Detener todos los servicios
.\scripts\stop-all-services-local.ps1
```

---

## 🔧 Inicio Manual - Paso a Paso

### ⚠️ **IMPORTANTE**: Seguir este orden estrictamente

```powershell
# 1️⃣ Eureka Server (PRIMERO - Descubrimiento de servicios)
gradle :eureka-server:bootRun --args='--spring.profiles.active=local'

# ⏳ Esperar 30 segundos

# 2️⃣ Config Server (SEGUNDO - Configuración centralizada)
# IMPORTANTE: Usa perfiles 'local,native' para archivos locales
gradle :config-server:bootRun --args='--spring.profiles.active=local,native'

# ⏳ Esperar 20 segundos

# 3️⃣ API Gateway (TERCERO - Puerta de entrada)
gradle :api-gateway:bootRun --args='--spring.profiles.active=local'

# ⏳ Esperar 20 segundos

# 4️⃣ Microservicios (EN PARALELO - Abrir nuevas ventanas de PowerShell)
gradle :arca-cotizador:bootRun --args='--spring.profiles.active=local'
gradle :arca-gestor-solicitudes:bootRun --args='--spring.profiles.active=local'
gradle :hello-world-service:bootRun --args='--spring.profiles.active=local'

# ⏳ Esperar 20 segundos

# 5️⃣ Aplicación Principal (ÚLTIMO)
gradle bootRun --args='--spring.profiles.active=local'
```

---

## 🔗 URLs de Acceso

Una vez desplegado correctamente:

### Servicios de Infraestructura
- 🔍 **Eureka Dashboard**: http://localhost:8761
- ⚙️ **Config Server**: http://localhost:8888/actuator/health
- 🚪 **API Gateway**: http://localhost:8085

### Microservicios
- 💰 **Arca Cotizador**: http://localhost:8081/actuator/health
- 📝 **Arca Gestor Solicitudes**: http://localhost:8082/actuator/health
- 🌍 **Hello World**: http://localhost:8083/actuator/health

### Aplicación Principal
- 🏠 **App Principal**: http://localhost:8090
- 📊 **Health Check**: http://localhost:8090/actuator/health

---

## 🔍 Verificación de Servicios

### 1. Verificar Eureka
```powershell
# Ver todos los servicios registrados
curl http://localhost:8761
```

### 2. Verificar Config Server
```powershell
# Ver configuración de un servicio
curl http://localhost:8888/arca-cotizador/local
curl http://localhost:8888/arca-gestor-solicitudes/local
```

### 3. Verificar Health de cada servicio
```powershell
curl http://localhost:8761/actuator/health  # Eureka
curl http://localhost:8888/actuator/health  # Config Server
curl http://localhost:8085/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # Cotizador
curl http://localhost:8082/actuator/health  # Gestor
curl http://localhost:8083/actuator/health  # Hello World
curl http://localhost:8090/actuator/health  # Main App
```

### 4. Verificar Conexión a MySQL
```powershell
# Desde PowerShell (si tienes MySQL Client)
mysql -h 192.168.100.77 -P 3306 -u root -pKoke1988**** -e "SHOW DATABASES;"
```

---

## 📝 Prerequisitos

### Software Requerido
- ✅ **Java 21** (Amazon Corretto o OpenJDK)
- ✅ **Gradle 8.x** (wrapper incluido)
- ✅ **MySQL 8.x** (servidor en 192.168.100.77)
- ⚠️ **MongoDB** (opcional, puerto 27017 - para notificaciones)

### Verificar Instalación
```powershell
# Verificar Java
java -version

# Verificar Gradle
gradle --version

# Verificar conectividad a MySQL
Test-NetConnection -ComputerName 192.168.100.77 -Port 3306
```

---

## 🗃️ Configuración de Base de Datos

### Crear Base de Datos (si no existe)
```sql
CREATE DATABASE IF NOT EXISTS arkavalenzuelabd 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Verificar
USE arkavalenzuelabd;
SHOW TABLES;
```

### Configuración de Usuario
```sql
-- Si necesitas crear el usuario
CREATE USER 'root'@'%' IDENTIFIED BY 'Koke1988****';
GRANT ALL PRIVILEGES ON arkavalenzuelabd.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

---

## 🐛 Troubleshooting

### Problema: No se puede conectar a MySQL
```powershell
# Verificar conectividad
Test-NetConnection -ComputerName 192.168.100.77 -Port 3306

# Verificar firewall
netsh advfirewall firewall show rule name=all | findstr "3306"
```

**Solución**: 
- Verificar que MySQL esté corriendo en el servidor
- Verificar que el firewall permita conexiones al puerto 3306
- Verificar que MySQL esté configurado para aceptar conexiones remotas

### Problema: Puerto ya en uso
```powershell
# Ver qué proceso está usando el puerto
netstat -ano | findstr ":8761"
netstat -ano | findstr ":8888"
netstat -ano | findstr ":8085"

# Matar el proceso (reemplazar PID)
taskkill /PID <PID> /F
```

### Problema: Servicios no se registran en Eureka
**Solución**:
1. Verificar que Eureka Server esté corriendo primero
2. Esperar 30-60 segundos después de iniciar cada servicio
3. Revisar logs en busca de errores de conexión
4. Verificar la configuración de `eureka.client.service-url.defaultZone`

### Problema: OutOfMemoryError
**Solución**:
```powershell
# Aumentar memoria para Gradle
$env:GRADLE_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"

# Luego iniciar los servicios
```

---

## 📊 Monitoreo

### Ver logs en tiempo real
Cada servicio corre en su propia ventana de PowerShell. Para ver logs:
- Los logs se muestran directamente en la ventana de PowerShell
- Para guardar logs: `gradle :service:bootRun > logs/service.log 2>&1`

### Verificar estado en Eureka
Abrir navegador: http://localhost:8761

Deberías ver todos estos servicios registrados:
- ✅ CONFIG-SERVER
- ✅ API-GATEWAY
- ✅ ARCA-COTIZADOR
- ✅ ARCA-GESTOR-SOLICITUDES
- ✅ HELLO-WORLD-SERVICE
- ✅ ARKAJVALENZUELA

---

## 🛑 Detener Servicios

### Opción 1: Script de PowerShell
```powershell
.\scripts\stop-all-services-local.ps1
```

### Opción 2: Manual
- Presionar `Ctrl + C` en cada ventana de PowerShell
- O cerrar todas las ventanas de PowerShell

### Opción 3: Matar todos los procesos Java
```powershell
Get-Process | Where-Object {$_.ProcessName -like "*java*"} | Stop-Process -Force
```

---

## 📝 Notas Importantes

1. **Orden de Inicio**: Es CRÍTICO seguir el orden: Eureka → Config → Gateway → Microservicios → Main App
2. **Tiempos de Espera**: Esperar los tiempos indicados entre cada servicio
3. **MySQL**: Asegurarse de que la base de datos esté accesible antes de iniciar
4. **Memoria**: Los servicios pueden requerir ~6-8GB de RAM en total
5. **Primera Ejecución**: La primera vez puede tomar más tiempo por la descarga de dependencias

---

## 🔄 Diferencias vs Perfil AWS

| Configuración | Local | AWS |
|---------------|-------|-----|
| MySQL Host | 192.168.100.77 | AWS RDS Endpoint |
| Eureka URL | localhost:8761 | AWS Load Balancer |
| Discovery | IP Address | Hostname |
| Security | Basic | IAM + Security Groups |
| MongoDB | localhost:27017 | AWS DocumentDB |

---

📝 **Última actualización**: Octubre 2025  
👨‍💻 **ARKA Development Team**
