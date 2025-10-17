# ✅ CONFIGURACIÓN LOCAL COMPLETADA - Proyecto ARKA

## 🎯 Resumen de Cambios

Se ha configurado completamente el proyecto ARKA para funcionar en **ambiente local** con base de datos MySQL remota.

---

## 📊 Configuración Implementada

### 🗄️ Base de Datos MySQL
- **Host**: `192.168.100.77`
- **Puerto**: `3306`
- **Base de datos**: `arkavalenzuelabd`
- **Usuario**: `root`
- **Contraseña**: `Koke1988****`

---

## 📁 Archivos Creados/Modificados

### 1. Archivos de Configuración Local en Config Server

#### 📂 `config-server/src/main/resources/`
- ✅ `application-local.yml` - Configuración del Config Server para local

#### 📂 `config-server/config-repository/`
- ✅ `application-local.yml` - Configuración global para todos los servicios
- ✅ `eureka-server-local.yml` - Configuración de Eureka
- ✅ `api-gateway-local.yml` - Configuración del API Gateway
- ✅ `arca-cotizador-local.yml` - Configuración del servicio de cotizaciones
- ✅ `arca-gestor-solicitudes-local.yml` - Configuración del gestor de solicitudes
- ✅ `hello-world-service-local.yml` - Configuración del servicio de prueba

### 2. Archivos de Configuración Local en cada Microservicio

- ✅ `eureka-server/src/main/resources/application-local.yml`
- ✅ `api-gateway/src/main/resources/application-local.yml`
- ✅ `arca-cotizador/src/main/resources/application-local.yml`
- ✅ `arca-gestor-solicitudes/src/main/resources/application-local.yml`
- ✅ `hello-world-service/src/main/resources/application-local.yml`
- ✅ `src/main/resources/application-local.properties` (App principal)

### 3. Archivos Modificados

- ✅ `src/main/resources/application.properties` - Actualizada conexión MySQL

### 4. Scripts de Automatización

- ✅ `scripts/start-all-services-local.ps1` - Script para iniciar todos los servicios
- ✅ `scripts/stop-all-services-local.ps1` - Script para detener todos los servicios

### 5. Documentación

- ✅ `DEPLOYMENT-GUIDE-LOCAL.md` - Guía completa de despliegue local

---

## 🚀 Cómo Usar

### Opción 1: Inicio Automático (Recomendado)

```powershell
# Desde la raíz del proyecto
.\scripts\start-all-services-local.ps1
```

Este script:
1. Inicia Eureka Server primero
2. Luego Config Server
3. Después API Gateway
4. Finalmente todos los microservicios
5. Espera los tiempos necesarios entre cada servicio

### Opción 2: Inicio Manual

```powershell
# 1. Eureka Server
gradle :eureka-server:bootRun --args='--spring.profiles.active=local'

# 2. Config Server (en otra ventana, esperar 30s)
gradle :config-server:bootRun --args='--spring.profiles.active=local'

# 3. API Gateway (en otra ventana, esperar 20s)
gradle :api-gateway:bootRun --args='--spring.profiles.active=local'

# 4. Microservicios (en ventanas separadas, esperar 20s)
gradle :arca-cotizador:bootRun --args='--spring.profiles.active=local'
gradle :arca-gestor-solicitudes:bootRun --args='--spring.profiles.active=local'
gradle :hello-world-service:bootRun --args='--spring.profiles.active=local'

# 5. Aplicación Principal (esperar 20s)
gradle bootRun --args='--spring.profiles.active=local'
```

### Detener Servicios

```powershell
.\scripts\stop-all-services-local.ps1
```

---

## 🔗 URLs de Acceso

| Servicio | URL | Descripción |
|----------|-----|-------------|
| Eureka Dashboard | http://localhost:8761 | Panel de descubrimiento |
| Config Server | http://localhost:8888 | Servidor de configuración |
| API Gateway | http://localhost:8085 | Puerta de entrada |
| Arca Cotizador | http://localhost:8081 | Servicio de cotizaciones |
| Arca Gestor | http://localhost:8082 | Gestión de solicitudes |
| Hello World | http://localhost:8083 | Servicio de prueba |
| App Principal | http://localhost:8090 | Aplicación principal |

---

## ✅ Verificaciones Pre-inicio

### 1. Verificar Java
```powershell
java -version
# Debe mostrar Java 21
```

### 2. Verificar Conexión a MySQL
```powershell
Test-NetConnection -ComputerName 192.168.100.77 -Port 3306
# Debe mostrar: TcpTestSucceeded : True
```

### 3. Verificar que los puertos estén libres
```powershell
netstat -ano | findstr ":8761"  # Debe estar vacío
netstat -ano | findstr ":8888"  # Debe estar vacío
netstat -ano | findstr ":8085"  # Debe estar vacío
```

---

## 🗃️ Configuración de Base de Datos

### Crear la base de datos (si no existe)

Conéctate al servidor MySQL y ejecuta:

```sql
CREATE DATABASE IF NOT EXISTS arkavalenzuelabd 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

-- Verificar
SHOW DATABASES LIKE 'arkavalenzuelabd';
```

### Verificar Permisos del Usuario

```sql
-- Verificar que el usuario root tenga permisos
SHOW GRANTS FOR 'root'@'%';

-- Si no tiene permisos, otorgarlos:
GRANT ALL PRIVILEGES ON arkavalenzuelabd.* TO 'root'@'%';
FLUSH PRIVILEGES;
```

---

## 📝 Características de la Configuración Local

### ✅ Configuración de MySQL
- Conexión a base de datos remota en `192.168.100.77:3306`
- Pool de conexiones optimizado (HikariCP)
- DDL auto-update habilitado
- SQL logging activado para debugging

### ✅ Configuración de Eureka
- Registro en `localhost:8761`
- Prefer IP address habilitado
- Auto-registro activado

### ✅ Configuración de Logging
- Nivel DEBUG para paquetes `com.arka` y `com.arkavalenzuela`
- Nivel INFO para Spring Framework
- SQL logging habilitado

### ✅ Configuración de Actuator
- Health checks expuestos
- Métricas disponibles
- Circuit breaker monitoring (en gestor-solicitudes)

---

## 🔄 Diferencias con Perfil AWS

| Aspecto | Local | AWS |
|---------|-------|-----|
| **MySQL Host** | 192.168.100.77 | AWS RDS Endpoint |
| **Eureka** | localhost:8761 | Load Balancer URL |
| **Security** | Básica | IAM + Security Groups |
| **Discovery** | IP-based | DNS-based |
| **Config Server** | Native (archivos locales) | Git repository |

---

## 🐛 Troubleshooting

### Problema: No se puede conectar a MySQL

**Síntomas**: `Communications link failure`

**Soluciones**:
1. Verificar que el servidor MySQL esté corriendo
2. Verificar firewall en el servidor MySQL
3. Verificar que MySQL acepte conexiones remotas:
   ```sql
   SHOW VARIABLES LIKE 'bind_address';
   -- Debe ser '0.0.0.0' o la IP específica
   ```

### Problema: Puerto ya en uso

**Síntomas**: `Address already in use: bind`

**Solución**:
```powershell
# Encontrar el proceso
netstat -ano | findstr ":8761"

# Matar el proceso (reemplazar <PID>)
taskkill /PID <PID> /F
```

### Problema: OutOfMemoryError

**Solución**:
```powershell
# Configurar más memoria para Gradle
$env:GRADLE_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m"
```

### Problema: Servicios no se registran en Eureka

**Soluciones**:
1. Verificar que Eureka Server esté corriendo primero
2. Esperar 30-60 segundos para el registro
3. Verificar logs en busca de errores
4. Visitar http://localhost:8761 para ver servicios registrados

---

## 📚 Documentación Adicional

- 📖 **[DEPLOYMENT-GUIDE-LOCAL.md](DEPLOYMENT-GUIDE-LOCAL.md)** - Guía detallada de despliegue local
- 📖 **[DEPLOYMENT-GUIDE.md](DEPLOYMENT-GUIDE.md)** - Guía de despliegue AWS (original)
- 📖 **[README.md](README.md)** - Documentación general del proyecto

---

## 🎯 Próximos Pasos

1. ✅ Iniciar los servicios con el perfil `local`
2. ✅ Verificar que todos los servicios se registren en Eureka
3. ✅ Probar los endpoints a través del API Gateway
4. ✅ Verificar que la conexión a MySQL funcione correctamente
5. ✅ Revisar logs en busca de errores o warnings

---

## 📞 Soporte

Si encuentras algún problema:

1. Revisa los logs de cada servicio
2. Consulta la sección de Troubleshooting
3. Verifica la conectividad a MySQL
4. Asegúrate de seguir el orden de inicio

---

**Configuración creada por**: ARKA Development Team  
**Fecha**: Octubre 2025  
**Perfil**: Local Development Environment  
**Base de Datos**: MySQL @ 192.168.100.77:3306
