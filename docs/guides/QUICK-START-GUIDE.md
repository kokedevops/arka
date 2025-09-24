# 🚀 ARKA E-COMMERCE - GUÍA RÁPIDA DE INICIO

<div align="center">
  <img src="https://img.shields.io/badge/Quick%20Start-Ready-brightgreen" alt="Quick Start"/>
  <img src="https://img.shields.io/badge/Setup%20Time-5%20min-blue" alt="Setup Time"/>
  <img src="https://img.shields.io/badge/Docker-Required-blue" alt="Docker"/>
</div>

---

## ⚡ **INICIO RÁPIDO (3 pasos - 5 minutos)**

### 🎯 **Prerequisitos**
- ✅ Docker & Docker Compose instalado
- ✅ Puertos libres: 8080, 8888, 8761, 3306, 27017, 8025
- ✅ 4GB+ RAM disponible

### 1️⃣ **Iniciar Plataforma Completa**
```bash
# Windows - E-commerce Completo
scripts\start-ecommerce-complete.bat

# Linux/Mac - E-commerce Completo
chmod +x scripts/start-ecommerce-complete.sh
./scripts/start-ecommerce-complete.sh

# Verificar servicios automáticamente
./scripts/check-services.sh
```

### 2️⃣ **Verificar Servicios (Auto Health Check)**
```bash
# Health checks principales
curl http://localhost:8888/health               # 🛒 E-commerce Main
curl http://localhost:8080/actuator/health      # 🌐 API Gateway
curl http://localhost:8761/actuator/health      # � Eureka Server

# BFF Services
curl http://localhost:8888/mobile/api/health    # � Mobile BFF
curl http://localhost:8888/web/api/health       # � Web BFF
```

### 3️⃣ **Testing E-commerce APIs**
```bash
# 🎯 Dashboard completo de negocio
curl http://localhost:8888/web/api/dashboard

# 📊 Estadísticas y analytics
curl http://localhost:8888/analytics/statistics

# 🛒 APIs principales
curl http://localhost:8888/productos            # Productos
curl http://localhost:8888/usuarios             # Clientes
curl http://localhost:8888/carritos             # Carritos
curl http://localhost:8888/pedidos              # Pedidos

# 🔍 Business Intelligence
curl http://localhost:8888/carritos/abandonados # Carritos abandonados
curl http://localhost:8888/productos/stock-bajo # Stock bajo
```

---

## 🌐 **URLS IMPORTANTES**

| 🎯 Servicio | 🌐 URL | 📝 Descripción | 🔑 Autenticación |
|-------------|--------|----------------|------------------|
| 🛒 **E-commerce Main** | http://localhost:8888 | API principal del e-commerce | JWT Required |
| � **API Gateway** | http://localhost:8080 | Gateway y load balancer | JWT Required |
| 🔍 **Eureka Server** | http://localhost:8761 | Service discovery | No auth |
| 🍃 **MongoDB Express** | http://localhost:8081 | Interface web para MongoDB | No auth |
| 📧 **MailHog Web UI** | http://localhost:8025 | Testing de emails | No auth |
| 📊 **Prometheus** | http://localhost:9090 | Métricas y monitoring | No auth |
| 📈 **Grafana** | http://localhost:3000 | Dashboards de analytics | admin/admin |
| 🐳 **Docker Status** | http://localhost:9000 | Portainer (si está activo) | admin/password |

---

## 🔐 **AUTENTICACIÓN RÁPIDA**

### � **Login de Test**
```bash
# Obtener JWT Token
curl -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'

# Respuesta esperada:
{
  "success": true,
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "roles": ["ADMIN"]
  }
}
```

### 🔑 **Usar Token en Requests**
```bash
# Guardar token
export JWT_TOKEN="eyJhbGciOiJIUzI1NiJ9..."

# Usar en requests
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8888/productos
```

---

## 🧪 **TESTING E-COMMERCE COMPLETO**

### 🛒 **Flow de E-commerce Completo**
```bash
# 1. Login
JWT_TOKEN=$(curl -s -X POST http://localhost:8888/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')

# 2. Crear producto
curl -X POST http://localhost:8888/productos \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "iPhone 15 Pro",
    "descripcion": "Último modelo de Apple",
    "precioUnitario": 1299.99,
    "stock": 50
  }'

# 3. Crear cliente
curl -X POST http://localhost:8888/usuarios \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan@email.com",
    "telefono": "+56912345678"
  }'

# 4. Crear carrito
curl -X POST http://localhost:8888/carritos \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "productIds": [1],
    "total": 1299.99
  }'

# 5. Crear pedido
curl -X POST http://localhost:8888/pedidos \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": 1,
    "productIds": [1],
    "total": 1299.99,
    "estado": "PENDIENTE"
  }'
```

### 📊 **Business Intelligence**
```bash
# Métricas de negocio
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8888/analytics/statistics

# Carritos abandonados
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8888/carritos/abandonados

# Productos más vendidos
curl -H "Authorization: Bearer $JWT_TOKEN" \
  http://localhost:8888/analytics/productos-mas-vendidos

# Ingresos por período
curl -H "Authorization: Bearer $JWT_TOKEN" \
  "http://localhost:8888/analytics/ingresos?from=2024-01-01&to=2024-12-31"
```

### 🛒 **Crear y Abandonar Carrito**
```bash
# 1. Crear carrito
curl -X POST http://localhost:8080/carritos \
  -H "Content-Type: application/json" \
  -d '{
    "clienteId": 1,
    "clienteNombre": "Juan Pérez",
    "estado": "ACTIVE"
  }'

# 2. Abandonar carrito
curl -X PUT http://localhost:8080/carritos/1/abandonar

# 3. Ver carritos abandonados
curl http://localhost:8080/carritos/abandonados
```

### 📱 **Mobile BFF**
```bash
# Productos destacados para móvil
curl http://localhost:8080/mobile/api/productos/destacados

# Búsqueda optimizada móvil
curl "http://localhost:8080/mobile/api/productos/buscar?query=laptop"
```

### 💻 **Web BFF**
```bash
# Dashboard con métricas completas
curl http://localhost:8080/web/api/dashboard

# Detalle producto para web
curl http://localhost:8080/web/api/productos/1/detalle
```

### 📊 **Analytics**
```bash
# Estadísticas de e-commerce
curl http://localhost:8080/analytics/statistics

# Insights de carritos abandonados
curl http://localhost:8080/analytics/abandoned-carts
```

---

## 🔔 **NOTIFICACIONES AUTOMÁTICAS**

### 📧 **Email Testing con MailHog**
1. Abrir http://localhost:8025
2. Crear carrito y abandonarlo
3. Esperar 5 minutos (demo) o 1 hora (producción)
4. Ver email recibido en MailHog

### 🍃 **MongoDB Logs**
1. Abrir http://localhost:8081
2. Usuario: `arka_admin` / Password: `Arca2025*`
3. Ver colección `notification_history`

---

## ⚙️ **CONFIGURACIÓN RÁPIDA**

### 📧 **Activar Emails Reales**
```properties
# En application-dev.properties
arka.mail.enabled=true
spring.mail.host=smtp.gmail.com
spring.mail.username=tu-email@gmail.com
spring.mail.password=tu-app-password
```

### 🍃 **Cambiar MongoDB**
```properties
# Para MongoDB remoto
spring.data.mongodb.uri=mongodb://usuario:password@host:27017/base_datos
```

---

## 🎯 **ENDPOINTS COMPLETOS**

### 🛒 **Carritos**
- `GET /carritos` - Listar carritos
- `POST /carritos` - Crear carrito
- `GET /carritos/abandonados` - ⭐ Carritos abandonados
- `PUT /carritos/{id}/abandonar` - Abandonar carrito

### 📋 **Pedidos**
- `GET /pedidos` - Listar pedidos
- `POST /pedidos` - Crear pedido
- `GET /pedidos/cliente/{id}` - Pedidos por cliente

### 📱 **Mobile BFF**
- `GET /mobile/api/productos/destacados` - Destacados móvil
- `GET /mobile/api/productos/buscar` - Búsqueda móvil
- `GET /mobile/api/carrito/{id}/resumen` - Resumen carrito

### 💻 **Web BFF**
- `GET /web/api/dashboard` - ⭐ Dashboard completo
- `GET /web/api/productos/{id}/detalle` - Detalle producto
- `GET /web/api/productos/completo` - Productos web

### 📊 **Analytics**
- `GET /analytics/statistics` - ⭐ Estadísticas completas
- `GET /analytics/abandoned-carts` - ⭐ Insights carritos

---

## 🎉 **¡LISTO PARA PRODUCCIÓN!**

### ✅ **FEATURES COMPLETADAS (100%)**
- 🛒 **E-commerce completo** con carritos y pedidos
- 📱💻 **BFF Architecture** para móvil y web
- 🔔 **Notificaciones automáticas** con scheduler
- 📧 **Sistema de emails** con SMTP real
- 🍃 **MongoDB** para logs y analytics
- 📊 **Business Intelligence** con métricas
- 🐳 **Docker** con toda la infraestructura
- 📈 **Monitoring** con Prometheus y Grafana
