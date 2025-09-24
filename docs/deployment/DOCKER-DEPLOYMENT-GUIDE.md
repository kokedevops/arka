# 🐳 Guía de Despliegue Docker - ARKA E-commerce Platform

Esta guía te ayudará a desplegar toda la plataforma ARKA E-commerce usando Docker y Docker Compose.

## 📋 **REQUISITOS PREVIOS**

### **Software Necesario:**
- ✅ Docker Desktop (Windows/Mac) o Docker Engine (Linux)
- ✅ Docker Compose
- ✅ Git
- ✅ 8 GB RAM mínimo (16 GB recomendado)
- ✅ 10 GB espacio libre en disco

### **Puertos que deben estar libres:**
- `8080` - API Gateway
- `8888` - Aplicación Principal
- `8761` - Eureka Server
- `8082` - Gestor Solicitudes
- `8083` - Cotizador  
- `8084` - Hello World Service
- `5432` - PostgreSQL
- `27017` - MongoDB
- `6379` - Redis
- `5672/15672` - RabbitMQ
- `3000` - Grafana
- `9091` - Prometheus

## 🚀 **OPCIONES DE DESPLIEGUE**

### **Opción 1: Inicio Rápido (Recomendado)**
```bash
# Windows
start-quick.bat

# Linux/Mac
chmod +x deploy-docker.sh
./deploy-docker.sh
```

### **Opción 2: Paso a Paso**
```bash
# 1. Clonar el repositorio
git clone <tu-repositorio>
cd arkavalenzuela

# 2. Construir las imágenes
docker-compose build --no-cache

# 3. Iniciar todos los servicios
docker-compose up -d

# 4. Verificar el estado
docker-compose ps
```

### **Opción 3: Con PowerShell (Windows)**
```powershell
# Ejecutar como Administrador
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser
.\deploy-docker.ps1
```

## 🏗️ **ARQUITECTURA DE MICROSERVICIOS**

```
┌─────────────────────────────────────────────────────────────┐
│                    ARKA E-commerce Platform                 │
└─────────────────────────────────────────────────────────────┘
                                │
                    ┌───────────▼───────────┐
                    │    API Gateway        │
                    │      (Port 8080)      │
                    └───────────┬───────────┘
                                │
            ┌───────────────────┼───────────────────┐
            │                   │                   │
    ┌───────▼───────┐  ┌────────▼────────┐  ┌──────▼──────┐
    │ Main App      │  │ Gestor          │  │ Cotizador   │
    │ (Port 8888)   │  │ Solicitudes     │  │ (Port 8083) │
    │               │  │ (Port 8082)     │  │             │
    └───────────────┘  └─────────────────┘  └─────────────┘
            │                   │                   │
    ┌───────▼───────────────────▼───────────────────▼───────┐
    │              Service Discovery                        │
    │              Eureka Server (Port 8761)               │
    └───────────────────────────────────────────────────────┘
```

## 🗄️ **BASES DE DATOS**

### **PostgreSQL (Principal)**
- **Host:** localhost:5432
- **Database:** arka_db
- **User:** arka
- **Password:** arka123

### **MongoDB (Notificaciones)**
- **Host:** localhost:27017
- **Database:** arka_notifications
- **User:** arka_admin
- **Password:** Arca2025*

### **Redis (Cache)**
- **Host:** localhost:6379
- **Password:** arka123

## 📊 **MONITOREO Y HERRAMIENTAS**

| Herramienta | URL | Credenciales |
|-------------|-----|--------------|
| Grafana | http://localhost:3000 | admin/admin123 |
| Prometheus | http://localhost:9091 | - |
| Mongo Express | http://localhost:8081 | - |
| MailHog | http://localhost:8025 | - |
| RabbitMQ | http://localhost:15672 | arka/arka123 |

## 🔗 **ENDPOINTS PRINCIPALES**

### **API Principal (Puerto 8888)**
```bash
# Health Check
GET http://localhost:8888/actuator/health

# Productos
GET http://localhost:8888/productos
POST http://localhost:8888/productos

# Autenticación JWT
POST http://localhost:8888/api/auth/login
POST http://localhost:8888/api/auth/refresh

# API de Terceros
GET http://localhost:8888/api/terceros/ObtenerDatos/productos
GET http://localhost:8888/api/terceros/ObtenerDatos/categorias
```

### **API Gateway (Puerto 8080)**
```bash
# Enrutar a través del Gateway
GET http://localhost:8080/gestor-solicitudes/solicitudes
GET http://localhost:8080/cotizador/cotizaciones
```

## 🛠️ **COMANDOS ÚTILES**

### **Gestión de Contenedores**
```bash
# Ver estado de todos los servicios
docker-compose ps

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f main-app

# Reiniciar un servicio
docker-compose restart main-app

# Detener todos los servicios
docker-compose down

# Detener y eliminar volúmenes
docker-compose down -v
```

### **Monitoreo**
```bash
# Ver uso de recursos
docker stats

# Inspeccionar un contenedor
docker inspect arka-main-app

# Ejecutar comando en contenedor
docker exec -it arka-main-app bash

# Ver health check
docker inspect --format='{{.State.Health.Status}}' arka-main-app
```

## 🔧 **SOLUCIÓN DE PROBLEMAS**

### **Problema: Puerto ocupado**
```bash
# Verificar qué proceso usa el puerto
netstat -ano | findstr :8888

# Matar proceso (Windows)
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8888 | xargs kill -9
```

### **Problema: Contenedor no inicia**
```bash
# Ver logs detallados
docker-compose logs [servicio]

# Verificar configuración
docker-compose config

# Reconstruir imagen
docker-compose build --no-cache [servicio]
```

### **Problema: Base de datos no conecta**
```bash
# Verificar que PostgreSQL esté corriendo
docker-compose ps postgres

# Conectar a la base de datos
docker exec -it arka-postgres psql -U arka -d arka_db
```

## 📈 **ESCALABILIDAD**

Para escalar servicios específicos:
```bash
# Escalar gestor de solicitudes a 3 instancias
docker-compose up -d --scale gestor-solicitudes=3

# Escalar cotizador a 2 instancias  
docker-compose up -d --scale cotizador=2
```

## 🔒 **SEGURIDAD**

### **Variables de Entorno Sensibles**
- Cambiar passwords por defecto en producción
- Usar Docker secrets para información sensible
- Configurar firewall para puertos específicos

### **JWT Configuration**
- Secret: `404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970`
- Access Token: 24 horas
- Refresh Token: 7 días

## 📝 **TESTING**

### **Health Checks**
```bash
# Verificar todos los servicios
curl http://localhost:8888/actuator/health
curl http://localhost:8080/actuator/health
curl http://localhost:8761/actuator/health
```

### **API Testing**
```bash
# Test sin autenticación
curl http://localhost:8888/api/terceros/ObtenerDatos/productos

# Test con autenticación
curl -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

## 📞 **SOPORTE**

Si encuentras problemas:
1. Revisa los logs: `docker-compose logs -f`
2. Verifica el estado: `docker-compose ps`
3. Consulta esta documentación
4. Contacta al equipo de desarrollo

---

**Versión:** 1.0  
**Última actualización:** Agosto 2025  
**Mantenido por:** ARKA Development Team
