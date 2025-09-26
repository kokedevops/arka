# 🚀 ARKA Platform - API Endpoints Testing Guide

## 📋 Configuración de Servicios

| Servicio | Puerto | Base URL | Descripción |
|----------|--------|----------|-------------|
| **API Gateway** | `8085` | http://localhost:8085 | 🚪 Entrada principal del sistema |
| **Arca Cotizador** | `8081` | http://localhost:8081 | 💰 Servicio de cotizaciones (Reactive) |
| **Arca Gestor Solicitudes** | `8082` | http://localhost:8082 | 📝 Gestión de solicitudes y autenticación |
| **Hello World Service** | `8083-8084` | http://localhost:8083 | 🌍 Servicio de prueba |
| **Aplicación Principal** | `8090` | http://localhost:8090 | 🏠 Aplicación principal E-commerce |
| **Eureka Server** | `8761` | http://localhost:8761 | 🔍 Descubrimiento de servicios |
| **Config Server** | `8888` | http://localhost:8888 | ⚙️ Servidor de configuración |

## 📋 Índice
- [API Gateway (Puerto 8085)](#api-gateway-puerto-8085)
- [Aplicación Principal (Puerto 8090)](#aplicación-principal-puerto-8090)
- [Arca Cotizador (Puerto 8081)](#arca-cotizador-puerto-8081)
- [Arca Gestor Solicitudes (Puerto 8082)](#arca-gestor-solicitudes-puerto-8082)
- [Hello World Service (Puerto 8083)](#hello-world-service-puerto-8083)
- [Servicios de Infraestructura](#servicios-de-infraestructura)
- [Ejemplos de Testing con cURL](#ejemplos-de-testing-con-curl)

---

## 🚪 API Gateway (Puerto 8085)

### Base URL: `http://localhost:8085`

El API Gateway actúa como punto de entrada único para todos los servicios del sistema. Todas las rutas están configuradas con balanceadores de carga automáticos.

#### Rutas de Autenticación (Públicas)
```http
# Registro de usuarios
POST /auth/register
# Login de usuarios  
POST /auth/login
# Refresh token
POST /auth/refresh
```

#### Rutas de Administración (Solo Admin)
```http
# Gestión administrativa
GET /api/admin/**
```

#### Rutas de Microservicios (Con Autenticación)
```http
# Cotizaciones
GET /api/cotizaciones/**
# Gestión de solicitudes
GET /api/solicitudes/**
# Hello World (testing)
GET /hello/**
```

#### Health Check del Gateway
```http
GET /actuator/health
```

---

## 🏠 Aplicación Principal (Puerto 8090)

### Base URL: `http://localhost:8090`

#### Endpoint Principal
```http
GET /
```
**Respuesta:**
```json
{
    "application": "ARKA E-commerce Platform",
    "version": "1.0.0",
    "status": "Running",
    "port": "8090",
    "environment": "AWS"
}
```

#### Health Check
```http
GET /health
```

#### Información de API
```http
GET /api
```

#### Información de Login
```http
GET /login
```

---

## 💰 Arca Cotizador (Puerto 8081)

### Base URL: `http://localhost:8081`
**Tecnología:** Spring WebFlux (Reactive)

#### Endpoint Principal (Reactive)
```http
GET /
```
**Respuesta:** `"Arca Cotizador Service - Reactive with WebFlux!"`

#### Health Check (Reactive)
```http
GET /health
```
**Respuesta:** `"Cotizador Service is UP! Running on {hostname}:8081 (Reactive)"`

#### Información del Servicio
```http
GET /info
```
**Respuesta:**
```json
{
    "serviceName": "Arca Cotizador",
    "version": "1.0.0",
    "port": "8081",
    "technology": "Spring WebFlux (Reactive)",
    "hostname": "{hostname}"
}
```

#### Endpoints de Cotización
```http
# Listar cotizaciones
GET /cotizaciones

# Obtener cotización por ID
GET /cotizaciones/{id}

# Crear nueva cotización
POST /cotizaciones
Content-Type: application/json
{
    "producto": "Producto ejemplo",
    "cantidad": 10,
    "precioUnitario": 99.99
}

# Stream de cotizaciones (Reactive)
GET /cotizaciones/stream
```

---

## 📝 Arca Gestor Solicitudes (Puerto 8082)

### Base URL: `http://localhost:8082`
**Tecnología:** Spring WebFlux (Reactive)

#### Autenticación (`/auth`)

##### Registro de Usuario
```http
POST /auth/register
Content-Type: application/json

{
    "username": "usuario@ejemplo.com",
    "password": "password123",
    "email": "usuario@ejemplo.com",
    "fullName": "Nombre Usuario"
}
```
**Respuesta:**
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "refresh-token-aqui",
    "expiresIn": 3600,
    "user": {
        "id": 1,
        "username": "usuario@ejemplo.com",
        "email": "usuario@ejemplo.com"
    }
}
```

##### Login
```http
POST /auth/login
Content-Type: application/json

{
    "username": "usuario@ejemplo.com", 
    "password": "password123"
}
```

##### Refresh Token
```http
POST /auth/refresh
Content-Type: application/json

{
    "refreshToken": "refresh-token-aqui"
}
```

##### Logout
```http
POST /auth/logout
Authorization: Bearer {token}
```

#### Gestión Administrativa (`/api/admin`)

##### Listar Usuarios
```http
GET /api/admin/users
Authorization: Bearer {admin-token}
```

##### Obtener Usuario por ID
```http
GET /api/admin/users/{id}
Authorization: Bearer {admin-token}
```

##### Actualizar Usuario
```http
PUT /api/admin/users/{id}
Authorization: Bearer {admin-token}
Content-Type: application/json

{
    "username": "nuevo-usuario",
    "email": "nuevo@ejemplo.com",
    "enabled": true,
    "roles": ["USER", "ADMIN"]
}
```

##### Eliminar Usuario
```http
DELETE /api/admin/users/{id}
Authorization: Bearer {admin-token}
```

#### Endpoints de Testing
```http
# Test endpoint
GET /test

# Info del servicio
GET /info
```

---

## 🌍 Hello World Service (Puerto 8083)

### Base URL: `http://localhost:8083`

#### Endpoint Principal
```http
GET /
```
**Respuesta:** `"Hello World from {hostname} on port 8083!"`

#### Información del Servicio
```http
GET /info
```
**Respuesta:**
```json
{
    "serviceName": "Hello World Service",
    "version": "1.0.0",
    "hostname": "{hostname}",
    "port": "8083",
    "status": "Running"
}
```

#### Health Check
```http
GET /health
```

---

## 🔧 Servicios de Infraestructura

### 🔍 Eureka Server (Puerto 8761)
```http
# Dashboard de Eureka
GET http://localhost:8761

# API de aplicaciones registradas
GET http://localhost:8761/eureka/apps

# Información específica de una app
GET http://localhost:8761/eureka/apps/{APP-NAME}
```

### ⚙️ Config Server (Puerto 8888)
```http
# Configuración de una aplicación
GET http://localhost:8888/{application}/{profile}

# Ejemplo: configuración de api-gateway con perfil aws
GET http://localhost:8888/api-gateway/aws
```

---

## 🛍️ Endpoints de Productos

### Base URL: `/productos`

#### 1. Obtener todos los productos
```http
GET /productos
```
**Descripción:** Lista todos los productos disponibles
**Respuesta:** Array de ProductDto

#### 2. Obtener producto por ID
```http
GET /productos/{id}
```
**Parámetros:**
- `id` (Long): ID del producto
**Respuesta:** ProductDto o 404 si no existe

#### 3. Crear nuevo producto
```http
POST /productos
Content-Type: application/json

{
    "nombre": "Producto Ejemplo",
    "descripcion": "Descripción del producto",
    "precioUnitario": 99.99,
    "stock": 100,
    "categoria": {
        "id": 1,
        "nombre": "Electrónicos"
    }
}
```
**Respuesta:** ProductDto creado o error 400

#### 4. Actualizar producto
```http
PUT /productos/{id}
Content-Type: application/json

{
    "id": 1,
    "nombre": "Producto Actualizado",
    "descripcion": "Nueva descripción",
    "precioUnitario": 129.99,
    "stock": 80
}
```
**Respuesta:** ProductDto actualizado o 404

#### 5. Eliminar producto
```http
DELETE /productos/{id}
```
**Respuesta:** 204 No Content o 404

#### 6. Buscar productos por nombre
```http
GET /productos/buscar?term={searchTerm}
```
**Parámetros:**
- `term` (String): Término de búsqueda
**Respuesta:** Array de ProductDto

#### 7. Productos por categoría
```http
GET /productos/categoria/{nombre}
```
**Parámetros:**
- `nombre` (String): Nombre de la categoría
**Respuesta:** Array de ProductDto

#### 8. Productos ordenados
```http
GET /productos/ordenados
```
**Respuesta:** Array de ProductDto ordenados alfabéticamente

---

## 👥 Endpoints de Usuarios/Clientes

### Base URL: `/usuarios`

#### 1. Obtener todos los usuarios
```http
GET /usuarios
```
**Respuesta:** Array de CustomerDto

#### 2. Obtener usuario por ID
```http
GET /usuarios/{id}
```
**Parámetros:**
- `id` (Long): ID del usuario
**Respuesta:** CustomerDto o 404

#### 3. Crear nuevo usuario
```http
POST /usuarios
Content-Type: application/json

{
    "nombre": "Juan Pérez",
    "email": "juan.perez@email.com",
    "telefono": "+56912345678",
    "direccion": "Av. Principal 123"
}
```
**Respuesta:** CustomerDto creado

#### 4. Actualizar usuario
```http
PUT /usuarios/{id}
Content-Type: application/json

{
    "id": 1,
    "nombre": "Juan Carlos Pérez",
    "email": "juan.carlos@email.com",
    "telefono": "+56987654321",
    "direccion": "Nueva Dirección 456"
}
```
**Respuesta:** CustomerDto actualizado o 404

#### 5. Eliminar usuario
```http
DELETE /usuarios/{id}
```
**Respuesta:** 204 No Content o 404

#### 6. Buscar usuarios por nombre
```http
GET /usuarios/buscar?nombre={searchName}
```
**Parámetros:**
- `nombre` (String): Nombre a buscar
**Respuesta:** Array de CustomerDto

#### 7. Usuarios ordenados
```http
GET /usuarios/ordenados
```
**Respuesta:** Array de CustomerDto ordenados alfabéticamente

---

## 📦 Endpoints de Pedidos

### Base URL: `/pedidos`

#### 1. Obtener todos los pedidos
```http
GET /pedidos
```
**Respuesta:** Array de OrderDto

#### 2. Obtener pedido por ID
```http
GET /pedidos/{id}
```
**Parámetros:**
- `id` (Long): ID del pedido
**Respuesta:** OrderDto o 404

#### 3. Crear nuevo pedido
```http
POST /pedidos
Content-Type: application/json

{
    "customerId": 1,
    "productIds": [1, 2, 3],
    "total": 299.97,
    "estado": "PENDIENTE"
}
```
**Respuesta:** OrderDto creado

#### 4. Actualizar pedido
```http
PUT /pedidos/{id}
Content-Type: application/json

{
    "id": 1,
    "customerId": 1,
    "productIds": [1, 2],
    "total": 199.98,
    "estado": "CONFIRMADO"
}
```
**Respuesta:** OrderDto actualizado o 404

#### 5. Eliminar pedido
```http
DELETE /pedidos/{id}
```
**Respuesta:** 204 No Content o 404

#### 6. Pedidos por cliente
```http
GET /pedidos/cliente/{customerId}
```
**Parámetros:**
- `customerId` (Long): ID del cliente
**Respuesta:** Array de OrderDto

#### 7. Pedidos por producto
```http
GET /pedidos/producto/{productId}
```
**Parámetros:**
- `productId` (Long): ID del producto
**Respuesta:** Array de OrderDto

---

## 🏷️ Endpoints de Categorías

### Base URL: `/categorias`

#### 1. Obtener todas las categorías
```http
GET /categorias
```
**Respuesta:** Array de CategoryDto

#### 2. Obtener categoría por ID
```http
GET /categorias/{id}
```
**Parámetros:**
- `id` (Long): ID de la categoría
**Respuesta:** CategoryDto o 404

#### 3. Crear nueva categoría
```http
POST /categorias
Content-Type: application/json

{
    "nombre": "Nueva Categoría",
    "descripcion": "Descripción de la categoría"
}
```
**Respuesta:** CategoryDto creado

#### 4. Actualizar categoría
```http
PUT /categorias/{id}
Content-Type: application/json

{
    "id": 1,
    "nombre": "Categoría Actualizada",
    "descripcion": "Nueva descripción"
}
```
**Respuesta:** CategoryDto actualizado o 404

#### 5. Eliminar categoría
```http
DELETE /categorias/{id}
```
**Respuesta:** 204 No Content o 404

---

## 🛒 Endpoints de Carritos

### Base URL: `/carritos`

#### 1. Obtener todos los carritos
```http
GET /carritos
```
**Respuesta:** Array de CartDto

#### 2. Obtener carrito por ID
```http
GET /carritos/{id}
```
**Parámetros:**
- `id` (Long): ID del carrito
**Respuesta:** CartDto o 404

#### 3. Crear nuevo carrito
```http
POST /carritos
Content-Type: application/json

{
    "clienteId": 1,
    "estado": "ACTIVO"
}
```
**Respuesta:** CartDto creado

#### 4. Actualizar carrito
```http
PUT /carritos/{id}
Content-Type: application/json

{
    "id": 1,
    "clienteId": 1,
    "estado": "FINALIZADO"
}
```
**Respuesta:** CartDto actualizado o 404

#### 5. Eliminar carrito
```http
DELETE /carritos/{id}
```
**Respuesta:** 204 No Content o 404

---

## 💻 Endpoints BFF Web

### Base URL: `/web/api`

#### 1. Dashboard web
```http
GET /web/api/dashboard
```
**Respuesta:** WebDashboardDto con estadísticas completas

#### 2. Detalle de producto para web
```http
GET /web/api/productos/{id}/detalle
```
**Parámetros:**
- `id` (Long): ID del producto
**Respuesta:** WebProductDetailDto con información extendida

#### 3. Productos completos para web
```http
GET /web/api/productos/completo
```
**Respuesta:** Array de WebProductDetailDto

#### 4. Productos por rango de precio
```http
GET /web/api/productos/rango-precio?min={min}&max={max}
```
**Parámetros:**
- `min` (BigDecimal): Precio mínimo
- `max` (BigDecimal): Precio máximo
**Respuesta:** Array de WebProductDetailDto

#### 5. Health check web
```http
GET /web/api/health
```
**Respuesta:** Estado del BFF Web

## 🧪 Ejemplos de Testing con cURL

### � Testing Completo del Sistema

#### 1️⃣ Verificar Servicios de Infraestructura
```bash
# Eureka Server
curl http://localhost:8761/

# Config Server
curl http://localhost:8888/actuator/health

# Verificar aplicaciones registradas en Eureka
curl http://localhost:8761/eureka/apps
```

#### 2️⃣ Testing de Servicios Principales

##### Hello World Service
```bash
# Endpoint principal
curl http://localhost:8083/

# Información del servicio
curl http://localhost:8083/info

# Health check
curl http://localhost:8083/health
```

##### Arca Cotizador (Reactive)
```bash
# Endpoint principal
curl http://localhost:8081/

# Health check
curl http://localhost:8081/health

# Información del servicio
curl http://localhost:8081/info

# Listar cotizaciones
curl http://localhost:8081/cotizaciones

# Crear cotización
curl -X POST http://localhost:8081/cotizaciones \
  -H "Content-Type: application/json" \
  -d '{
    "producto": "Laptop Dell",
    "cantidad": 5,
    "precioUnitario": 1299.99
  }'

# Stream de cotizaciones (reactive)
curl http://localhost:8081/cotizaciones/stream
```

##### Arca Gestor Solicitudes (Autenticación)
```bash
# Registro de usuario
curl -X POST http://localhost:8082/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser@ejemplo.com",
    "password": "password123",
    "email": "testuser@ejemplo.com",
    "fullName": "Usuario de Prueba"
  }'

# Login
curl -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser@ejemplo.com",
    "password": "password123"
  }'

# Health check
curl http://localhost:8082/health

# Test endpoint
curl http://localhost:8082/test

# Info del servicio
curl http://localhost:8082/info
```

##### Aplicación Principal (E-commerce)
```bash
# Página principal
curl http://localhost:8090/

# Health check
curl http://localhost:8090/health

# API info
curl http://localhost:8090/api

# Login info
curl http://localhost:8090/login

# Productos (ejemplo)
curl http://localhost:8090/productos

# Buscar productos
curl "http://localhost:8090/productos/buscar?term=laptop"

# Categorías
curl http://localhost:8090/categorias

# Usuarios (requiere autenticación)
curl http://localhost:8090/usuarios \
  -H "Authorization: Bearer {token}"
```

#### 3️⃣ Testing a través del API Gateway
```bash
# A través del gateway (puerto 8085)
curl http://localhost:8085/hello/

# Autenticación a través del gateway
curl -X POST http://localhost:8085/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser@ejemplo.com",
    "password": "password123"
  }'

# Cotizaciones a través del gateway
curl http://localhost:8085/api/cotizaciones/

# Health check del gateway
curl http://localhost:8085/actuator/health
```

---

## 📋 Lista Completa de Endpoints por Servicio

### 🚪 API Gateway - http://localhost:8085
```
GET    http://localhost:8085/actuator/health
POST   http://localhost:8085/auth/register
POST   http://localhost:8085/auth/login
POST   http://localhost:8085/auth/refresh
POST   http://localhost:8085/auth/logout
GET    http://localhost:8085/api/admin/**
GET    http://localhost:8085/api/cotizaciones/**
GET    http://localhost:8085/api/solicitudes/**
GET    http://localhost:8085/hello/**
```

### 🏠 Aplicación Principal - http://localhost:8090
```
GET    http://localhost:8090/
GET    http://localhost:8090/health
GET    http://localhost:8090/api
GET    http://localhost:8090/login
GET    http://localhost:8090/productos
GET    http://localhost:8090/productos/{id}
POST   http://localhost:8090/productos
PUT    http://localhost:8090/productos/{id}
DELETE http://localhost:8090/productos/{id}
GET    http://localhost:8090/productos/buscar?term={searchTerm}
GET    http://localhost:8090/productos/categoria/{nombre}
GET    http://localhost:8090/productos/ordenados
GET    http://localhost:8090/usuarios
GET    http://localhost:8090/usuarios/{id}
POST   http://localhost:8090/usuarios
PUT    http://localhost:8090/usuarios/{id}
DELETE http://localhost:8090/usuarios/{id}
GET    http://localhost:8090/categorias
GET    http://localhost:8090/categorias/{id}
POST   http://localhost:8090/categorias
PUT    http://localhost:8090/categorias/{id}
DELETE http://localhost:8090/categorias/{id}
GET    http://localhost:8090/carritos/{customerId}
POST   http://localhost:8090/carritos/{customerId}/items
PUT    http://localhost:8090/carritos/{customerId}/items/{itemId}
DELETE http://localhost:8090/carritos/{customerId}/items/{itemId}
GET    http://localhost:8090/pedidos
GET    http://localhost:8090/pedidos/{id}
POST   http://localhost:8090/pedidos
PUT    http://localhost:8090/pedidos/{id}/status
```

### 💰 Arca Cotizador - http://localhost:8081
```
GET    http://localhost:8081/
GET    http://localhost:8081/health
GET    http://localhost:8081/info
GET    http://localhost:8081/cotizaciones
GET    http://localhost:8081/cotizaciones/{id}
POST   http://localhost:8081/cotizaciones
PUT    http://localhost:8081/cotizaciones/{id}
DELETE http://localhost:8081/cotizaciones/{id}
GET    http://localhost:8081/cotizaciones/stream
GET    http://localhost:8081/cotizaciones/cliente/{clienteId}
GET    http://localhost:8081/actuator/health
```

### 📝 Arca Gestor Solicitudes - http://localhost:8082
```
POST   http://localhost:8082/auth/register
POST   http://localhost:8082/auth/login
POST   http://localhost:8082/auth/refresh
POST   http://localhost:8082/auth/logout
GET    http://localhost:8082/api/admin/users
GET    http://localhost:8082/api/admin/users/{id}
PUT    http://localhost:8082/api/admin/users/{id}
DELETE http://localhost:8082/api/admin/users/{id}
GET    http://localhost:8082/test
GET    http://localhost:8082/info
GET    http://localhost:8082/health
GET    http://localhost:8082/actuator/health
```

### 🌍 Hello World Service - http://localhost:8083
```
GET    http://localhost:8083/
GET    http://localhost:8083/info
GET    http://localhost:8083/health
GET    http://localhost:8083/actuator/health
```

### 🔍 Eureka Server - http://localhost:8761
```
GET    http://localhost:8761/
GET    http://localhost:8761/eureka/apps
GET    http://localhost:8761/eureka/apps/{APP-NAME}
GET    http://localhost:8761/actuator/health
```

### ⚙️ Config Server - http://localhost:8888
```
GET    http://localhost:8888/{application}/{profile}
GET    http://localhost:8888/api-gateway/aws
GET    http://localhost:8888/arca-cotizador/aws
GET    http://localhost:8888/arca-gestor-solicitudes/aws
GET    http://localhost:8888/hello-world-service/aws
GET    http://localhost:8888/actuator/health
```

---

## 🔧 Scripts de Testing Automatizado

### Script de Verificación Completa
```bash
#!/bin/bash
echo "🔍 Verificando todos los servicios..."

# Servicios de infraestructura
echo "📡 Eureka Server:"
curl -s http://localhost:8761/actuator/health | jq -r '.status // "DOWN"'

echo "⚙️ Config Server:"
curl -s http://localhost:8888/actuator/health | jq -r '.status // "DOWN"'

# Servicios principales
echo "🚪 API Gateway:"
curl -s http://localhost:8085/actuator/health | jq -r '.status // "DOWN"'

echo "🏠 Aplicación Principal:"
curl -s http://localhost:8090/health || echo "DOWN"

echo "💰 Arca Cotizador:"
curl -s http://localhost:8081/health || echo "DOWN"

echo "📝 Gestor Solicitudes:"
curl -s http://localhost:8082/health || echo "DOWN"

echo "🌍 Hello World:"
curl -s http://localhost:8083/health || echo "DOWN"

echo "✅ Verificación completada"
```

### Script de Testing de Autenticación
```bash
#!/bin/bash
echo "🔐 Testing de autenticación..."

# Registro
echo "📝 Registrando usuario de prueba..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8082/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser@test.com",
    "password": "test123",
    "email": "testuser@test.com",
    "fullName": "Test User"
  }')

echo "Respuesta de registro: $REGISTER_RESPONSE"

# Login
echo "🔑 Haciendo login..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8082/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser@test.com",
    "password": "test123"
  }')

echo "Respuesta de login: $LOGIN_RESPONSE"

# Extraer token (requiere jq)
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token // empty')

if [ ! -z "$TOKEN" ]; then
    echo "✅ Token obtenido: ${TOKEN:0:20}..."
    
    # Testing con token
    echo "🧪 Testing endpoint protegido..."
    curl -s http://localhost:8082/api/admin/users \
      -H "Authorization: Bearer $TOKEN" || echo "❌ Acceso denegado"
else
    echo "❌ No se pudo obtener token"
fi
```

---

## 📚 Notas Importantes

### 🔒 Autenticación
- Los endpoints `/auth/**` son públicos
- Los endpoints `/api/admin/**` requieren rol ADMIN
- Usar header `Authorization: Bearer {token}` para endpoints protegidos

### ⚡ Tecnologías
- **Arca Cotizador**: Spring WebFlux (Reactive)
- **Arca Gestor**: Spring WebFlux (Reactive) 
- **Aplicación Principal**: Spring Boot MVC
- **Hello World**: Spring Boot MVC

### 🔄 Orden de Testing
1. Verificar servicios de infraestructura (Eureka, Config)
2. Probar servicios individuales
3. Testing a través del API Gateway
4. Verificar flujos de autenticación
5. Testing de endpoints de negocio

### 📊 Monitoreo
- Todos los servicios exponen `/actuator/health`
- Eureka dashboard: http://localhost:8761
- Logs disponibles en cada servicio

---

💡 **Tip**: Usar herramientas como Postman o Insomnia para crear colecciones de testing más avanzadas.

## 🔗 Endpoints API de Terceros

### Base URL: `/api/terceros`

#### 1. Obtener datos por tabla
```http
GET /api/terceros/ObtenerDatos/{tabla}
```
**Parámetros:**
- `tabla` (String): productos, usuarios, pedidos, carritos, categorias
**Respuesta:** Datos en formato estándar de terceros

#### 2. Obtener dato específico
```http
GET /api/terceros/ObtenerDatos/{tabla}/{id}
```
**Parámetros:**
- `tabla` (String): Nombre de la tabla
- `id` (Long): ID del registro
**Respuesta:** Registro específico o error

#### 3. Guardar datos
```http
POST /api/terceros/GuardarDatos/{tabla}
Content-Type: application/json

{
    "nombre": "Ejemplo",
    "descripcion": "Datos de ejemplo"
}
```
**Respuesta:** Registro guardado con ID asignado

#### 4. Eliminar datos
```http
DELETE /api/terceros/BorrarDatos/{tabla}/{id}
```
**Parámetros:**
- `tabla` (String): Nombre de la tabla
- `id` (Long): ID del registro
**Respuesta:** Confirmación de eliminación

#### 5. Información de la API
```http
GET /api/terceros/info
```
**Respuesta:** Documentación de la API de terceros

---

## 🌐 Endpoints de Microservicios

### API Gateway (Puerto 8080)
```http
GET /
GET /health
GET /routes
GET /actuator/health
```

### Eureka Server (Puerto 8761)
```http
GET /
GET /eureka/apps
```

### Arca Cotizador (Puerto 8082)
```http
GET /api/cotizacion/health
POST /api/cotizacion/calcular
```

### Arca Gestor Solicitudes (Puerto 8083)
```http
GET /api/solicitudes/health
POST /api/solicitudes/procesar
GET /api/solicitudes/{id}
```

### Hello World Service (Puerto 8084)
```http
GET /hello
GET /hello/health
```

---

## 📝 Ejemplos de Payloads

### ProductDto
```json
{
    "id": 1,
    "nombre": "Smartphone Galaxy",
    "descripcion": "Teléfono inteligente de última generación",
    "precioUnitario": 599.99,
    "stock": 50,
    "categoria": {
        "id": 1,
        "nombre": "Electrónicos"
    },
    "available": true
}
```

### CustomerDto
```json
{
    "id": 1,
    "nombre": "María González",
    "email": "maria.gonzalez@email.com",
    "telefono": "+56912345678",
    "direccion": "Av. Libertador 456, Santiago"
}
```

### OrderDto
```json
{
    "id": 1,
    "customerId": 1,
    "customerName": "María González",
    "productIds": [1, 2],
    "productNames": ["Smartphone Galaxy", "Auriculares Bluetooth"],
    "total": 699.98,
    "fechaPedido": "2025-08-29 10:30:00",
    "estado": "CONFIRMADO"
}
```

### CategoryDto
```json
{
    "id": 1,
    "nombre": "Electrónicos",
    "descripcion": "Dispositivos electrónicos y tecnología"
}
```

### CartDto
```json
{
    "id": 1,
    "clienteId": 1,
    "clienteNombre": "María González",
    "fechaCreacion": "2025-08-29 09:15:00",
    "estado": "ACTIVO"
}
```

---

## 🧪 Testing con curl

### Ejemplo GET
```bash
curl -X GET "http://localhost:8888/productos" \
     -H "Content-Type: application/json"
```

### Ejemplo POST
```bash
curl -X POST "http://localhost:8888/productos" \
     -H "Content-Type: application/json" \
     -d '{
       "nombre": "Nuevo Producto",
       "descripcion": "Descripción del producto",
       "precioUnitario": 99.99,
       "stock": 100
     }'
```

### Ejemplo PUT
```bash
curl -X PUT "http://localhost:8888/productos/1" \
     -H "Content-Type: application/json" \
     -d '{
       "id": 1,
       "nombre": "Producto Actualizado",
       "descripcion": "Nueva descripción",
       "precioUnitario": 129.99,
       "stock": 80
     }'
```

### Ejemplo DELETE
```bash
curl -X DELETE "http://localhost:8888/productos/1" \
     -H "Content-Type: application/json"
```

---

## 🔧 Testing con Postman

### Configuración de Environment
```json
{
    "ARKA_BASE_URL": "http://localhost:8888",
    "API_GATEWAY_URL": "http://localhost:8080",
    "EUREKA_URL": "http://localhost:8761"
}
```

### Headers comunes
```
Content-Type: application/json
Accept: application/json
```

---

## 📊 Códigos de Respuesta

- **200 OK**: Operación exitosa
- **201 Created**: Recurso creado exitosamente
- **204 No Content**: Eliminación exitosa
- **400 Bad Request**: Error en los datos enviados
- **404 Not Found**: Recurso no encontrado
- **500 Internal Server Error**: Error interno del servidor

---

## 🚀 Cómo ejecutar la aplicación

1. **Ejecutar con Docker:**
   ```bash
   docker-compose up -d
   ```

2. **Ejecutar manualmente:**
   ```bash
   ./gradlew clean build -x test
   java -jar build/libs/arkajvalenzuela-0.0.1-SNAPSHOT.war --spring.profiles.active=aws
   ```

3. **Verificar estado:**
   ```bash
   curl http://localhost:8888/health
   ```

---

## 📞 URLs de Acceso

- **Aplicación Principal:** http://localhost:8888
- **API Gateway:** http://localhost:8080
- **Eureka Server:** http://localhost:8761
- **Grafana:** http://localhost:3000
- **Prometheus:** http://localhost:9091

---

## 📋 Notas Importantes

1. Todos los endpoints soportan CORS para desarrollo
2. Los endpoints de terceros usan formato de respuesta estándar
3. Los BFF están optimizados para web y mobile respectivamente
4. La autenticación JWT está implementada en endpoints protegidos
5. Todos los servicios tienen health checks implementados

---

**Creado por:** ARKA Development Team  
**Versión:** 1.0.0  
**Fecha:** Agosto 2025
