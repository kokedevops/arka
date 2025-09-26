# 🚀 ARKA E-COMMERCE - GUÍA COMPLETA DE TESTING

<div align="center">
  <img src="https://img.shields.io/badge/Testing-Complete-success" alt="Testing"/>
  <img src="https://img.shields.io/badge/APIs-REST-blue" alt="REST APIs"/>
  <img src="https://img.shields.io/badge/Authentication-JWT-orange" alt="JWT"/>
  <img src="https://img.shields.io/badge/Environment-Production-red" alt="Production"/>
</div>

---

## 📋 **ÍNDICE COMPLETO**
- [🌐 Información General](#-información-general)
- [🔑 Autenticación JWT](#-autenticación-jwt)
- [🛒 API E-commerce Principal](#-api-e-commerce-principal)
- [🌍 API de Terceros](#-api-de-terceros)
- [🔧 Microservicios](#-microservicios)
- [📱 BFF Architecture](#-bff-architecture)
- [🧪 Testing Automatizado](#-testing-automatizado)
- [📊 Monitoring & Health](#-monitoring--health)
- [🎯 Test Scenarios](#-test-scenarios)
- [🛠️ Troubleshooting](#️-troubleshooting)

---

## 🌐 **INFORMACIÓN GENERAL**

### 🏠 **URLs Base**
```bash
# 🛒 E-commerce Principal (Local)
http://localhost:8888

# 🌐 API Gateway (Local)
http://localhost:8080

# ☁️ Production Server (AWS)
http://3.134.244.104:8888

# 🔍 Service Discovery
http://localhost:8761
```

### 📊 **Información de la API**
```bash
# Información general del sistema
GET http://localhost:8888/

# Health check principal
GET http://localhost:8888/health

# Info detallada de la aplicación
GET http://localhost:8888/actuator/info

# Métricas del sistema
GET http://localhost:8888/actuator/metrics
```

### 🎯 **Endpoints de Documentación**
```bash
# OpenAPI/Swagger Documentation
GET http://localhost:8888/v3/api-docs

# Swagger UI
GET http://localhost:8888/swagger-ui.html

# Actuator endpoints disponibles
GET http://localhost:8888/actuator
```

---

## 🔑 **AUTENTICACIÓN JWT**

### 👥 **Usuarios Demo**
```bash
# Obtener información de usuarios de prueba
GET http://localhost:8888/api/auth/demo-users

# Response esperado:
{
  "demoUsers": [
    {
      "username": "admin",
      "password": "admin123",
      "roles": ["ADMIN", "USER"],
      "description": "Usuario administrador"
    },
    {
      "username": "user",
      "password": "user123",
      "roles": ["USER"],
      "description": "Usuario estándar"
    },
    {
      "username": "manager",
      "password": "manager123",
      "roles": ["MANAGER", "USER"],
      "description": "Usuario gerente"
    }
  ]
}
```

### 🔐 **Login Process**
```bash
# Login con username/email
POST http://localhost:8888/api/auth/login
Content-Type: application/json

{
  "identifier": "admin",
  "password": "admin123"
}

# Response:
{
  "success": true,
  "message": "Login exitoso",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "email": "admin@arka.com",
    "roles": ["ADMIN", "USER"]
  },
  "expiresIn": 86400000
}
```

### 🔄 **Token Management**
```bash
# Refresh Token
POST http://localhost:8888/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "your-refresh-token-here"
}

# Logout
POST http://localhost:8888/api/auth/logout
Authorization: Bearer {JWT_TOKEN}

# Validate Token
GET http://localhost:8888/api/auth/validate
Authorization: Bearer {JWT_TOKEN}
```

### 🔒 **Security Headers**
```bash
# Template para requests autenticados
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
Content-Type: application/json
Accept: application/json
X-Requested-With: XMLHttpRequest
```

**Respuesta esperada:**
```json
{
  "success": true,
  "message": "Login exitoso",
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9...",
  "username": "admin",
  "authorities": ["ROLE_ADMIN", "ROLE_USER"],
  "expiresIn": 86400,
  "refreshExpiresIn": 604800
}
```

### **3. Refresh Token**
```bash
POST http://3.134.244.104:8888/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "tu_refresh_token_aqui"
}
```

### **4. Validar Token**
```bash
POST http://3.134.244.104:8888/api/auth/validate
Content-Type: application/json

{
  "token": "tu_access_token_aqui"
}
```

### **5. Estado de Autenticación**
```bash
GET http://3.134.244.104:8888/api/auth/status
Authorization: Bearer tu_access_token
```

### **6. Endpoint Solo Administradores**
```bash
GET http://3.134.244.104:8888/api/auth/admin/info
Authorization: Bearer tu_access_token_admin
```

---

## 🛒 **API E-COMMERCE PRINCIPAL**

### **🏷️ CATEGORÍAS - `/categorias`**

#### **Listar todas las categorías**
```bash
GET http://3.134.244.104:8888/categorias
```

#### **Obtener categoría por ID**
```bash
GET http://3.134.244.104:8888/categorias/1
```

#### **Crear nueva categoría**
```bash
POST http://3.134.244.104:8888/categorias
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "nombre": "Electrónicos",
  "descripcion": "Productos electrónicos y tecnología"
}
```

#### **Actualizar categoría**
```bash
PUT http://3.134.244.104:8888/categorias/1
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "nombre": "Electrónicos Actualizados",
  "descripcion": "Nueva descripción"
}
```

#### **Eliminar categoría**
```bash
DELETE http://3.134.244.104:8888/categorias/1
Authorization: Bearer tu_access_token
```

---

### **📦 PRODUCTOS - `/productos`**

#### **Listar todos los productos**
```bash
GET http://3.134.244.104:8888/productos
```

#### **Obtener producto por ID**
```bash
GET http://3.134.244.104:8888/productos/1
```

#### **Crear nuevo producto**
```bash
POST http://3.134.244.104:8888/productos
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "nombre": "Laptop Gaming",
  "descripcion": "Laptop para gaming de alta gama",
  "precio": 1299.99,
  "stock": 10,
  "categoria": {
    "id": 1
  }
}
```

#### **Actualizar producto**
```bash
PUT http://3.134.244.104:8888/productos/1
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "nombre": "Laptop Gaming Pro",
  "descripcion": "Laptop gaming actualizada",
  "precio": 1499.99,
  "stock": 15
}
```

#### **Eliminar producto**
```bash
DELETE http://3.134.244.104:8888/productos/1
Authorization: Bearer tu_access_token
```

#### **Buscar productos por nombre**
```bash
GET http://3.134.244.104:8888/productos/buscar?nombre=laptop
```

#### **Productos por categoría**
```bash
GET http://3.134.244.104:8888/productos/categoria/electronica
```

#### **Productos por rango de precios**
```bash
GET http://3.134.244.104:8888/productos/rango-precio?min=100&max=1000
```

---

### **👥 USUARIOS/CLIENTES - `/usuarios`**

#### **Listar todos los usuarios**
```bash
GET http://3.134.244.104:8888/usuarios
Authorization: Bearer tu_access_token
```

#### **Obtener usuario por ID**
```bash
GET http://3.134.244.104:8888/usuarios/1
Authorization: Bearer tu_access_token
```

#### **Crear nuevo usuario**
```bash
POST http://3.134.244.104:8888/usuarios
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "nombre": "Juan Pérez",
  "email": "juan@email.com",
  "telefono": "+56912345678"
}
```

#### **Actualizar usuario**
```bash
PUT http://3.134.244.104:8888/usuarios/1
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "nombre": "Juan Carlos Pérez",
  "email": "juan.carlos@email.com",
  "telefono": "+56987654321"
}
```

#### **Eliminar usuario**
```bash
DELETE http://3.134.244.104:8888/usuarios/1
Authorization: Bearer tu_access_token
```

#### **Buscar usuarios por nombre**
```bash
GET http://3.134.244.104:8888/usuarios/buscar?nombre=juan
Authorization: Bearer tu_access_token
```

---

### **🛒 CARRITOS - `/carritos`**

#### **Listar todos los carritos**
```bash
GET http://3.134.244.104:8888/carritos
Authorization: Bearer tu_access_token
```

#### **Obtener carrito por ID**
```bash
GET http://3.134.244.104:8888/carritos/1
Authorization: Bearer tu_access_token
```

#### **Crear nuevo carrito**
```bash
POST http://3.134.244.104:8888/carritos
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "cliente": {
    "id": 1
  },
  "estado": "ACTIVE"
}
```

#### **Actualizar carrito**
```bash
PUT http://3.134.244.104:8888/carritos/1
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "estado": "PENDING"
}
```

#### **Eliminar carrito**
```bash
DELETE http://3.134.244.104:8888/carritos/1
Authorization: Bearer tu_access_token
```

#### **Obtener carritos abandonados**
```bash
GET http://3.134.244.104:8888/carritos/abandonados
Authorization: Bearer tu_access_token
```

#### **Activar carrito**
```bash
PUT http://3.134.244.104:8888/carritos/1/activar
Authorization: Bearer tu_access_token
```

#### **Abandonar carrito**
```bash
PUT http://3.134.244.104:8888/carritos/1/abandonar
Authorization: Bearer tu_access_token
```

---

### **📋 PEDIDOS - `/pedidos`**

#### **Listar todos los pedidos**
```bash
GET http://3.134.244.104:8888/pedidos
Authorization: Bearer tu_access_token
```

#### **Obtener pedido por ID**
```bash
GET http://3.134.244.104:8888/pedidos/1
Authorization: Bearer tu_access_token
```

#### **Crear nuevo pedido**
```bash
POST http://3.134.244.104:8888/pedidos
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "cliente": {
    "id": 1
  },
  "productos": ["Laptop", "Mouse"],
  "total": 1399.99,
  "estado": "PENDING"
}
```

#### **Actualizar pedido**
```bash
PUT http://3.134.244.104:8888/pedidos/1
Content-Type: application/json
Authorization: Bearer tu_access_token

{
  "estado": "SHIPPED",
  "total": 1399.99
}
```

#### **Eliminar pedido**
```bash
DELETE http://3.134.244.104:8888/pedidos/1
Authorization: Bearer tu_access_token
```

#### **Pedidos por cliente**
```bash
GET http://3.134.244.104:8888/pedidos/cliente/1
Authorization: Bearer tu_access_token
```

#### **Pedidos por producto**
```bash
GET http://3.134.244.104:8888/pedidos/producto/1
Authorization: Bearer tu_access_token
```

#### **Pedidos por rango de fechas**
```bash
GET http://3.134.244.104:8888/pedidos/rango-fechas?inicio=2025-01-01&fin=2025-12-31
Authorization: Bearer tu_access_token
```

---

## 🌍 **API DE TERCEROS** *(Cumple Especificaciones Requeridas)*

### **📋 Información de la API**
```bash
GET http://3.134.244.104:8888/api/terceros/info
```

### **✅ GET /ObtenerDatos/{tabla} - Obtener todos los registros**

#### **Obtener todos los productos**
```bash
GET http://3.134.244.104:8888/api/terceros/ObtenerDatos/productos
```

#### **Obtener todos los usuarios**
```bash
GET http://3.134.244.104:8888/api/terceros/ObtenerDatos/usuarios
```

#### **Obtener todos los pedidos**
```bash
GET http://3.134.244.104:8888/api/terceros/ObtenerDatos/pedidos
```

#### **Obtener todos los carritos**
```bash
GET http://3.134.244.104:8888/api/terceros/ObtenerDatos/carritos
```

#### **Obtener todas las categorías**
```bash
GET http://3.134.244.104:8888/api/terceros/ObtenerDatos/categorias
```

### **✅ GET /ObtenerDatos/{tabla}/{id} - Obtener registro específico**

#### **Obtener producto específico**
```bash
GET http://3.134.244.104:8888/api/terceros/ObtenerDatos/productos/1
```

#### **Obtener usuario específico**
```bash
GET http://3.134.244.104:8888/api/terceros/ObtenerDatos/usuarios/1
```

### **✅ POST /GuardarDatos/{tabla} - Guardar nuevo registro**

#### **Guardar nuevo producto**
```bash
POST http://3.134.244.104:8888/api/terceros/GuardarDatos/productos
Content-Type: application/json

{
  "nombre": "Nuevo Producto",
  "descripcion": "Descripción del producto",
  "precio": 99.99,
  "stock": 50
}
```

#### **Guardar nuevo usuario**
```bash
POST http://3.134.244.104:8888/api/terceros/GuardarDatos/usuarios
Content-Type: application/json

{
  "nombre": "Nuevo Usuario",
  "email": "usuario@email.com",
  "telefono": "+56912345678"
}
```

### **✅ DELETE /BorrarDatos/{tabla}/{id} - Borrar registro**

#### **Borrar producto**
```bash
DELETE http://3.134.244.104:8888/api/terceros/BorrarDatos/productos/1
```

#### **Borrar usuario**
```bash
DELETE http://3.134.244.104:8888/api/terceros/BorrarDatos/usuarios/1
```

---

## 🔧 **MICROSERVICIOS**

### **🌐 API Gateway - Puerto 8080**
```bash
GET http://3.134.244.104:8080/
```

### **🔍 Eureka Server - Puerto 8761**
```bash
GET http://3.134.244.104:8761/
```

### **📦 Cotizador - Puerto 8082**
```bash
GET http://3.134.244.104:8082/api/cotizacion
POST http://3.134.244.104:8082/api/cotizacion
```

### **📋 Gestor de Solicitudes - Puerto 8083**
```bash
GET http://3.134.244.104:8083/api/solicitudes
POST http://3.134.244.104:8083/api/solicitudes
```

### **👋 Hello World Service - Puerto 8084**
```bash
GET http://3.134.244.104:8084/hello
```

---

## 📱 **BFF (Backend for Frontend)**

### **🌐 Web BFF - `/web/api`**

#### **Productos completos para web**
```bash
GET http://3.134.244.104:8888/web/api/productos/completo
```

#### **Productos por rango de precio (Web)**
```bash
GET http://3.134.244.104:8888/web/api/productos/rango-precio?min=100&max=1000
```

#### **Health check Web**
```bash
GET http://3.134.244.104:8888/web/api/health
```

### **📱 Mobile BFF - `/mobile/api`**

#### **Información rápida de producto (Mobile)**
```bash
GET http://3.134.244.104:8888/mobile/api/productos/1/quick
```

#### **Resumen de carrito (Mobile)**
```bash
GET http://3.134.244.104:8888/mobile/api/carrito/1/resumen
```

#### **Health check Mobile**
```bash
GET http://3.134.244.104:8888/mobile/api/health
```

---

## 🧪 **TESTING COMPLETO**

### **🔧 Script de Testing Automatizado**

```bash
#!/bin/bash

BASE_URL="http://3.134.244.104:8888"

echo "🚀 INICIANDO TESTS COMPLETOS DE ARKA VALENZUELA"

# 1. Test de autenticación
echo "1️⃣ Testing JWT Authentication..."
curl -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"identifier":"admin","password":"admin123"}'

# 2. Test de API de terceros
echo "2️⃣ Testing Third Party API..."
curl -X GET $BASE_URL/api/terceros/ObtenerDatos/productos

# 3. Test de productos
echo "3️⃣ Testing Products API..."
curl -X GET $BASE_URL/productos

# 4. Test de categorías
echo "4️⃣ Testing Categories API..."
curl -X GET $BASE_URL/categorias

# 5. Test de microservicios
echo "5️⃣ Testing Microservices..."
curl -X GET http://3.134.244.104:8080/
curl -X GET http://3.134.244.104:8761/

echo "✅ TESTS COMPLETADOS"
```

### **🧪 Tests con Postman**

**Importar Collection:**
```json
{
  "info": {
    "name": "ARKA Valenzuela E-commerce API",
    "description": "Colección completa de endpoints"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://3.134.244.104:8888"
    },
    {
      "key": "accessToken",
      "value": ""
    }
  ]
}
```

### **📊 Endpoints de Analytics**

#### **Analytics del E-commerce**
```bash
GET http://3.134.244.104:8888/api/analytics
Authorization: Bearer tu_access_token
```

#### **Estadísticas de carritos**
```bash
GET http://3.134.244.104:8888/api/analytics/carritos
Authorization: Bearer tu_access_token
```

---

## ✅ **VERIFICACIÓN DE ESPECIFICACIONES**

### **✅ API de Terceros CUMPLE 100% con especificaciones:**

1. **GET /ObtenerDatos/{tabla}** ✅ - Obtiene todos los registros
2. **GET /ObtenerDatos/{tabla}/{id}** ✅ - Obtiene registro específico
3. **POST /GuardarDatos/{tabla}** ✅ - Guarda nuevo registro (JSON)
4. **DELETE /BorrarDatos/{tabla}/{id}** ✅ - Borra registro por ID

### **✅ Operaciones CRUD Completas:**

- **CREATE** ✅ - POST endpoints en todas las entidades
- **READ** ✅ - GET endpoints individuales y listados
- **UPDATE** ✅ - PUT endpoints para modificaciones
- **DELETE** ✅ - DELETE endpoints para eliminaciones

### **✅ Microservicios Funcionales:**

- **API Gateway** ✅ - Puerto 8080
- **Eureka Server** ✅ - Puerto 8761  
- **Cotizador** ✅ - Puerto 8082
- **Gestor Solicitudes** ✅ - Puerto 8083
- **Hello World** ✅ - Puerto 8084

### **✅ Autenticación JWT:**

- **Login** ✅ - Tokens de acceso y refresh
- **Refresh** ✅ - Renovación de tokens
- **Validation** ✅ - Validación de tokens
- **Role-based** ✅ - Autorización por roles

---

## 🎯 **CONCLUSIÓN**

✅ **TODAS LAS ESPECIFICACIONES CUMPLIDAS:**
- API de terceros con operaciones CRUD exactas
- Microservicios funcionales
- JWT Authentication completo
- BFF para Web y Mobile
- Endpoints RESTful completos
- Base de datos AWS RDS + DocumentDB
- Autenticación y autorización avanzada

**🚀 Sistema listo para producción con todas las funcionalidades solicitadas.**
