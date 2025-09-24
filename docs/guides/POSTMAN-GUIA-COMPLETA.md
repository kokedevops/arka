# � POSTMAN - GUÍA COMPLETA ARKA E-COMMERCE

<div align="center">
  <img src="https://img.shields.io/badge/Postman-Collection-orange" alt="Postman"/>
  <img src="https://img.shields.io/badge/API-REST-blue" alt="REST API"/>
  <img src="https://img.shields.io/badge/Testing-Automated-green" alt="Testing"/>
</div>

---

## 📋 **ÍNDICE**

- [🚀 Instalación y Configuración](#-instalación-y-configuración)
- [📦 Colección de APIs](#-colección-de-apis)
- [🔐 Autenticación JWT](#-autenticación-jwt)
- [🛍️ Endpoints E-commerce](#️-endpoints-e-commerce)
- [📱 Endpoints BFF](#-endpoints-bff)
- [🔗 API de Terceros](#-api-de-terceros)
- [🧪 Tests Automatizados](#-tests-automatizados)
- [📊 Monitoreo y Health](#-monitoreo-y-health)
- [🛠️ Troubleshooting](#️-troubleshooting)

---

## 🚀 **INSTALACIÓN Y CONFIGURACIÓN**

### 📋 **Prerequisitos**
- ✅ Postman Desktop App o Web
- ✅ ARKA Platform ejecutándose
- ✅ Acceso a puertos 8080-8888

### 📥 **Importar Colección**

#### **Método 1: Archivo JSON**
```bash
# 1. Descargar colección desde:
./docs/postman/ARKA-E-commerce-Collection.json

# 2. En Postman: Import > File > Seleccionar archivo
```

#### **Método 2: URL directa**
```bash
# Importar desde GitHub (cuando esté disponible)
https://raw.githubusercontent.com/kokedevops/arkavalenzuela/main/docs/postman/collection.json
```

### ⚙️ **Configurar Environment**

```json
{
  "ARKA_BASE_URL": "http://localhost:8888",
  "API_GATEWAY_URL": "http://localhost:8080",
  "EUREKA_URL": "http://localhost:8761",
  "JWT_TOKEN": "",
  "REFRESH_TOKEN": "",
  "USER_ID": "",
  "PRODUCT_ID": "",
  "ORDER_ID": ""
}
```

---

## 📦 **COLECCIÓN DE APIs**

### 🏗️ **Estructura de la Colección**

```
📁 ARKA E-commerce Platform
├── 📁 01 - Health & Info
│   ├── GET Health Check
│   ├── GET Application Info
│   └── GET API Documentation
├── 📁 02 - Authentication
│   ├── POST Login
│   ├── POST Refresh Token
│   └── POST Logout
├── 📁 03 - Products Management
│   ├── GET All Products
│   ├── GET Product by ID
│   ├── POST Create Product
│   ├── PUT Update Product
│   ├── DELETE Product
│   └── GET Products by Category
├── 📁 04 - Customer Management
│   ├── GET All Customers
│   ├── GET Customer by ID
│   ├── POST Create Customer
│   ├── PUT Update Customer
│   └── DELETE Customer
├── 📁 05 - Order Management
│   ├── GET All Orders
│   ├── GET Order by ID
│   ├── POST Create Order
│   ├── PUT Update Order
│   └── DELETE Order
├── 📁 06 - Category Management
│   ├── GET All Categories
│   ├── POST Create Category
│   └── PUT Update Category
├── 📁 07 - Cart Management
│   ├── GET All Carts
│   ├── POST Create Cart
│   └── GET Abandoned Carts
├── 📁 08 - BFF Web APIs
│   ├── GET Dashboard Data
│   ├── GET Product Details
│   └── GET Products by Price Range
├── 📁 09 - BFF Mobile APIs
│   ├── GET Mobile Products
│   ├── GET Quick Product Info
│   └── GET Cart Summary
├── 📁 10 - Third Party APIs
│   ├── GET Data by Table
│   ├── GET Specific Record
│   ├── POST Save Data
│   └── DELETE Remove Data
├── 📁 11 - Microservices
│   ├── 📁 Eureka Server
│   ├── 📁 API Gateway
│   ├── 📁 Gestor Solicitudes
│   └── 📁 Cotizador
└── 📁 12 - Monitoring
    ├── GET Metrics
    ├── GET Health Checks
    └── GET Circuit Breaker Status
```

---

## 🔐 **AUTENTICACIÓN JWT**

### 🔑 **Login Flow**

#### **1. Login Request**
```http
POST {{ARKA_BASE_URL}}/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

#### **2. Response**
```json
{
  "success": true,
  "message": "Login exitoso",
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "admin",
    "roles": ["ADMIN"]
  }
}
```

#### **3. Auto-Config Environment**
```javascript
// Pre-request Script (ya incluido en colección)
pm.test("Login Success", function () {
    const response = pm.response.json();
    if (response.success && response.token) {
        pm.environment.set("JWT_TOKEN", response.token);
        pm.environment.set("REFRESH_TOKEN", response.refreshToken);
        pm.environment.set("USER_ID", response.user.id);
    }
});
```

### 🔄 **Token Refresh**
```http
POST {{ARKA_BASE_URL}}/api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "{{REFRESH_TOKEN}}"
}
```

---

## 🛍️ **ENDPOINTS E-COMMERCE**

### 📦 **Products API**

#### **Listar Productos**
```http
GET {{ARKA_BASE_URL}}/productos
Authorization: Bearer {{JWT_TOKEN}}
```

#### **Crear Producto**
```http
POST {{ARKA_BASE_URL}}/productos
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
  "nombre": "Smartphone Galaxy S24",
  "descripcion": "Último modelo con cámara de 200MP",
  "precioUnitario": 899.99,
  "stock": 50,
  "categoria": {
    "id": 1,
    "nombre": "Electrónicos"
  }
}
```

#### **Buscar Productos**
```http
GET {{ARKA_BASE_URL}}/productos/buscar?term=smartphone
Authorization: Bearer {{JWT_TOKEN}}
```

### 👥 **Customers API**

#### **Crear Cliente**
```http
POST {{ARKA_BASE_URL}}/usuarios
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
  "nombre": "Juan Pérez",
  "email": "juan.perez@email.com",
  "telefono": "+56912345678",
  "direccion": "Av. Principal 123, Santiago"
}
```

### 📋 **Orders API**

#### **Crear Pedido**
```http
POST {{ARKA_BASE_URL}}/pedidos
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
  "customerId": 1,
  "productIds": [1, 2, 3],
  "total": 1599.97,
  "estado": "PENDIENTE"
}
```

---

## 📱 **ENDPOINTS BFF**

### 💻 **Web BFF**

#### **Dashboard Completo**
```http
GET {{ARKA_BASE_URL}}/web/api/dashboard
Authorization: Bearer {{JWT_TOKEN}}
```

#### **Productos con Detalles Web**
```http
GET {{ARKA_BASE_URL}}/web/api/productos/completo
Authorization: Bearer {{JWT_TOKEN}}
```

#### **Productos por Rango de Precio**
```http
GET {{ARKA_BASE_URL}}/web/api/productos/rango-precio?min=100&max=500
Authorization: Bearer {{JWT_TOKEN}}
```

### �📱 **Mobile BFF**

#### **Productos Móvil**
```http
GET {{ARKA_BASE_URL}}/mobile/api/productos
Authorization: Bearer {{JWT_TOKEN}}
```

#### **Info Rápida de Producto**
```http
GET {{ARKA_BASE_URL}}/mobile/api/productos/{{PRODUCT_ID}}/quick
Authorization: Bearer {{JWT_TOKEN}}
```

---

## 🔗 **API DE TERCEROS**

### 📊 **Endpoints Estándar**

#### **Obtener Datos por Tabla**
```http
GET {{ARKA_BASE_URL}}/api/terceros/ObtenerDatos/productos
Authorization: Bearer {{JWT_TOKEN}}
```

#### **Obtener Registro Específico**
```http
GET {{ARKA_BASE_URL}}/api/terceros/ObtenerDatos/productos/1
Authorization: Bearer {{JWT_TOKEN}}
```

#### **Guardar Datos**
```http
POST {{ARKA_BASE_URL}}/api/terceros/GuardarDatos/productos
Authorization: Bearer {{JWT_TOKEN}}
Content-Type: application/json

{
  "nombre": "Producto Terceros",
  "descripcion": "Creado vía API de terceros",
  "precioUnitario": 199.99,
  "stock": 25
}
```

#### **Eliminar Datos**
```http
DELETE {{ARKA_BASE_URL}}/api/terceros/BorrarDatos/productos/1
Authorization: Bearer {{JWT_TOKEN}}
```

---

## 🧪 **TESTS AUTOMATIZADOS**

### ✅ **Tests Incluidos en Colección**

#### **Test Template Global**
```javascript
// Test común en todas las requests
pm.test("Status code is success", function () {
    pm.expect(pm.response.code).to.be.oneOf([200, 201, 204]);
});

pm.test("Response time is less than 2000ms", function () {
    pm.expect(pm.response.responseTime).to.be.below(2000);
});

pm.test("Content-Type is JSON", function () {
    pm.expect(pm.response.headers.get("Content-Type")).to.include("application/json");
});
```

#### **Test Específicos por Endpoint**

**Products Tests:**
```javascript
pm.test("Products array is returned", function () {
    const response = pm.response.json();
    pm.expect(response).to.be.an('array');
    pm.expect(response.length).to.be.greaterThan(0);
});

pm.test("Product has required fields", function () {
    const response = pm.response.json();
    const product = response[0];
    pm.expect(product).to.have.property('id');
    pm.expect(product).to.have.property('nombre');
    pm.expect(product).to.have.property('precioUnitario');
    pm.expect(product).to.have.property('stock');
});
```

**Auth Tests:**
```javascript
pm.test("Login returns JWT token", function () {
    const response = pm.response.json();
    pm.expect(response).to.have.property('success', true);
    pm.expect(response).to.have.property('token');
    pm.expect(response.token).to.match(/^eyJ/); // JWT format
});
```

### 🔄 **Test Runner Automation**

#### **Collection Runner Setup**
```javascript
// Environment setup for testing
const testData = {
    testProduct: {
        nombre: "Test Product " + Date.now(),
        descripcion: "Product for testing",
        precioUnitario: 99.99,
        stock: 10
    },
    testCustomer: {
        nombre: "Test Customer " + Date.now(),
        email: "test" + Date.now() + "@test.com",
        telefono: "+56900000000"
    }
};

pm.globals.set("testData", JSON.stringify(testData));
```

---

## 📊 **MONITOREO Y HEALTH**

### 🏥 **Health Checks**

#### **Application Health**
```http
GET {{ARKA_BASE_URL}}/health
```

**Expected Response:**
```json
{
  "status": "UP",
  "application": "arkajvalenzuela",
  "port": "8888",
  "timestamp": 1693123456789,
  "database": {
    "mysql": "Connected to AWS RDS",
    "mongodb": "Connected to AWS DocumentDB"
  }
}
```

#### **Microservices Health**
```http
GET {{API_GATEWAY_URL}}/actuator/health
GET {{EUREKA_URL}}/actuator/health
GET {{ARKA_BASE_URL}}/actuator/health
```

### 📈 **Métricas**

#### **Prometheus Metrics**
```http
GET {{ARKA_BASE_URL}}/actuator/prometheus
```

#### **Custom Metrics**
```http
GET {{ARKA_BASE_URL}}/actuator/metrics/http.server.requests
GET {{ARKA_BASE_URL}}/actuator/metrics/jvm.memory.used
```

---

## 🎯 **ESCENARIOS DE TESTING**

### 🛒 **E-commerce Complete Flow**

#### **Scenario 1: Customer Journey**
```javascript
// 1. Register/Login Customer
// 2. Browse Products
// 3. Add to Cart
// 4. Create Order
// 5. Check Order Status

const customerJourney = [
    "POST /api/auth/login",
    "GET /productos",
    "POST /carritos",
    "POST /pedidos",
    "GET /pedidos/{{ORDER_ID}}"
];
```

#### **Scenario 2: Admin Operations**
```javascript
// 1. Admin Login
// 2. Create Product
// 3. Manage Categories
// 4. View Dashboard
// 5. Monitor Orders

const adminFlow = [
    "POST /api/auth/login",
    "POST /productos",
    "POST /categorias",
    "GET /web/api/dashboard",
    "GET /pedidos"
];
```

### 🔄 **Circuit Breaker Testing**

#### **Scenario 3: Resilience Testing**
```javascript
// 1. Normal Operation
// 2. Simulate Service Failure
// 3. Verify Fallback
// 4. Check Circuit State
// 5. Recovery Testing

const resilienceTest = [
    "GET /api/cotizaciones/1",
    "POST /api/circuit-breaker/forzar-apertura/cotizador",
    "GET /api/cotizaciones/1", // Should get fallback
    "GET /api/circuit-breaker/estado",
    "POST /api/circuit-breaker/reiniciar/cotizador"
];
```

---

## 🛠️ **CONFIGURACIÓN AVANZADA**

### 🔧 **Pre-request Scripts**

#### **Global Authentication**
```javascript
// Auto-refresh token si está expirado
const token = pm.environment.get("JWT_TOKEN");
if (token) {
    const payload = JSON.parse(atob(token.split('.')[1]));
    const now = Math.floor(Date.now() / 1000);
    
    if (payload.exp < now) {
        // Token expirado, refresh automático
        pm.sendRequest({
            url: pm.environment.get("ARKA_BASE_URL") + "/api/auth/refresh",
            method: 'POST',
            header: { 'Content-Type': 'application/json' },
            body: {
                mode: 'raw',
                raw: JSON.stringify({
                    refreshToken: pm.environment.get("REFRESH_TOKEN")
                })
            }
        }, (err, res) => {
            if (!err && res.json().success) {
                pm.environment.set("JWT_TOKEN", res.json().token);
            }
        });
    }
}
```

#### **Dynamic Data Generation**
```javascript
// Generar datos dinámicos para tests
const faker = {
    name: "Test User " + Date.now(),
    email: "test" + Math.random().toString(36).substr(2, 9) + "@test.com",
    phone: "+569" + Math.floor(Math.random() * 100000000),
    price: Math.floor(Math.random() * 1000) + 10
};

pm.environment.set("dynamicName", faker.name);
pm.environment.set("dynamicEmail", faker.email);
pm.environment.set("dynamicPhone", faker.phone);
pm.environment.set("dynamicPrice", faker.price);
```

### 📊 **Test Results Export**

#### **Newman CLI Integration**
```bash
# Instalar Newman
npm install -g newman

# Ejecutar colección
newman run ARKA-Collection.json -e ARKA-Environment.json

# Con reportes
newman run ARKA-Collection.json -e ARKA-Environment.json \
  --reporters cli,html --reporter-html-export report.html

# Tests específicos
newman run ARKA-Collection.json -e ARKA-Environment.json \
  --folder "Products Management"
```

---

## 🛠️ **TROUBLESHOOTING**

### ❌ **Problemas Comunes**

#### **401 Unauthorized**
```javascript
// Verificar token en Environment
console.log("JWT Token:", pm.environment.get("JWT_TOKEN"));

// Re-login si es necesario
if (!pm.environment.get("JWT_TOKEN")) {
    console.log("Token not found, please login first");
}
```

#### **Connection Refused**
```javascript
// Verificar URLs en Environment
const baseUrl = pm.environment.get("ARKA_BASE_URL");
console.log("Base URL:", baseUrl);

// Health check automático
pm.sendRequest(baseUrl + "/health", (err, res) => {
    if (err) {
        console.log("Service not available:", err);
    } else {
        console.log("Service status:", res.json().status);
    }
});
```

#### **Timeout Errors**
```javascript
// Configurar timeout personalizado
pm.request.timeout = 10000; // 10 segundos

// Retry logic
let retryCount = 0;
const maxRetries = 3;

function executeWithRetry() {
    // Logic para retry automático
}
```

### 🔍 **Debug Mode**

#### **Request/Response Logging**
```javascript
// Pre-request: Log request details
console.log("Request URL:", pm.request.url.toString());
console.log("Request Method:", pm.request.method);
console.log("Request Headers:", pm.request.headers.toString());

// Test: Log response details
console.log("Response Status:", pm.response.code);
console.log("Response Time:", pm.response.responseTime + "ms");
console.log("Response Body:", pm.response.text());
```

---

## 📈 **MÉTRICAS Y REPORTES**

### 📊 **Dashboard de Testing**
```javascript
// Metrics collection
const metrics = {
    totalRequests: pm.globals.get("totalRequests") || 0,
    successfulRequests: pm.globals.get("successfulRequests") || 0,
    failedRequests: pm.globals.get("failedRequests") || 0,
    averageResponseTime: pm.globals.get("averageResponseTime") || 0
};

// Update metrics
if (pm.response.code >= 200 && pm.response.code < 300) {
    metrics.successfulRequests++;
} else {
    metrics.failedRequests++;
}

metrics.totalRequests++;
metrics.averageResponseTime = 
    (metrics.averageResponseTime + pm.response.responseTime) / metrics.totalRequests;

// Save metrics
pm.globals.set("totalRequests", metrics.totalRequests);
pm.globals.set("successfulRequests", metrics.successfulRequests);
pm.globals.set("failedRequests", metrics.failedRequests);
pm.globals.set("averageResponseTime", metrics.averageResponseTime);
```

---

## 📞 **SOPORTE Y RECURSOS**

### 🔗 **Enlaces Útiles**
- **Documentación API**: [API-ENDPOINTS-TESTING.md](API-ENDPOINTS-TESTING.md)
- **Guía Docker**: [DOCKER-DEPLOYMENT-GUIDE.md](DOCKER-DEPLOYMENT-GUIDE.md)
- **Postman Learning**: https://learning.postman.com/

### 💬 **Comunidad**
- **Issues**: https://github.com/kokedevops/arkavalenzuela/issues
- **Discussions**: https://github.com/kokedevops/arkavalenzuela/discussions

---

<div align="center">
  <strong>📮 Testing API completo con Postman</strong><br/>
  <em>ARKA Platform - API Testing Excellence</em>
</div>

## 🚀 **CONFIGURACIÓN INICIAL DE POSTMAN**

### **1️⃣ Crear Nueva Colección**
1. Abrir Postman
2. Click "New" → "Collection"
3. Nombre: `ARKA Valenzuela E-commerce`
4. Descripción: `API completa con JWT y API de Terceros`

### **2️⃣ Variables de Colección**
```json
{
  "baseUrl": "http://3.134.244.104:8888",
  "accessToken": "",
  "refreshToken": ""
}
```

**Cómo agregar variables:**
1. Click en tu colección
2. Tab "Variables"
3. Agregar:
   - `baseUrl`: `http://3.134.244.104:8888`
   - `accessToken`: (dejar vacío)
   - `refreshToken`: (dejar vacío)

---

## 🔑 **PARTE 1: AUTENTICACIÓN JWT**

### **📝 Request 1: Login JWT**

**Método:** `POST`
**URL:** `{{baseUrl}}/api/auth/login`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "identifier": "admin",
  "password": "admin123"
}
```

**Tests (Script para guardar token automáticamente):**
```javascript
// Verificar respuesta exitosa
pm.test("Login exitoso", function () {
    pm.response.to.have.status(200);
});

// Extraer y guardar token
if (pm.response.code === 200) {
    const responseJson = pm.response.json();
    
    // Guardar tokens en variables de colección
    pm.collectionVariables.set("accessToken", responseJson.accessToken);
    pm.collectionVariables.set("refreshToken", responseJson.refreshToken);
    
    console.log("✅ Access Token guardado:", responseJson.accessToken.substring(0, 20) + "...");
    console.log("✅ Refresh Token guardado:", responseJson.refreshToken.substring(0, 20) + "...");
}
```

### **📝 Request 2: Validar Token**

**Método:** `POST`
**URL:** `{{baseUrl}}/api/auth/validate`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "token": "{{accessToken}}"
}
```

### **📝 Request 3: Información de Usuario Demo**

**Método:** `GET`
**URL:** `{{baseUrl}}/api/auth/demo-users`

**Headers:** (ninguno necesario)

---

## 🌍 **PARTE 2: API DE TERCEROS (SIN AUTENTICACIÓN)**

### **📁 Folder: "API Terceros - Especificaciones"**

#### **📝 GET - Obtener Todas las Categorías**
**Método:** `GET`
**URL:** `{{baseUrl}}/api/terceros/ObtenerDatos/categorias`
**Headers:** (ninguno)

#### **📝 GET - Obtener Categoría por ID**
**Método:** `GET`
**URL:** `{{baseUrl}}/api/terceros/ObtenerDatos/categorias/1`
**Headers:** (ninguno)

#### **📝 POST - Guardar Nueva Categoría**
**Método:** `POST`
**URL:** `{{baseUrl}}/api/terceros/GuardarDatos/categorias`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nombre": "Electrónicos Postman",
  "descripcion": "Categoría creada desde Postman"
}
```

#### **📝 DELETE - Borrar Categoría**
**Método:** `DELETE`
**URL:** `{{baseUrl}}/api/terceros/BorrarDatos/categorias/1`
**Headers:** (ninguno)

#### **📝 GET - Obtener Todos los Productos**
**Método:** `GET`
**URL:** `{{baseUrl}}/api/terceros/ObtenerDatos/productos`
**Headers:** (ninguno)

#### **📝 POST - Guardar Nuevo Producto**
**Método:** `POST`
**URL:** `{{baseUrl}}/api/terceros/GuardarDatos/productos`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nombre": "iPhone 15 Pro",
  "descripcion": "Smartphone Apple última generación",
  "precio": 1199.99,
  "stock": 25
}
```

#### **📝 GET - Obtener Todos los Usuarios**
**Método:** `GET`
**URL:** `{{baseUrl}}/api/terceros/ObtenerDatos/usuarios`
**Headers:** (ninguno)

#### **📝 POST - Guardar Nuevo Usuario**
**Método:** `POST`
**URL:** `{{baseUrl}}/api/terceros/GuardarDatos/usuarios`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nombre": "Carlos Mendoza",
  "email": "carlos@postman.com",
  "telefono": "+56912345678"
}
```

---

## 🛒 **PARTE 3: API E-COMMERCE PRINCIPAL (CON JWT)**

### **⚙️ Configuración de Autorización**

**Para TODOS los requests de la API principal:**

1. Tab "Authorization"
2. Type: `Bearer Token`
3. Token: `{{accessToken}}`

### **📁 Folder: "Categorías"**

#### **📝 GET - Listar Categorías**
**Método:** `GET`
**URL:** `{{baseUrl}}/categorias`
**Authorization:** `Bearer {{accessToken}}`

#### **📝 GET - Categoría por ID**
**Método:** `GET`
**URL:** `{{baseUrl}}/categorias/1`
**Authorization:** `Bearer {{accessToken}}`

#### **📝 POST - Crear Categoría**
**Método:** `POST`
**URL:** `{{baseUrl}}/categorias`
**Authorization:** `Bearer {{accessToken}}`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nombre": "Gaming",
  "descripcion": "Productos para gaming"
}
```

#### **📝 PUT - Actualizar Categoría**
**Método:** `PUT`
**URL:** `{{baseUrl}}/categorias/1`
**Authorization:** `Bearer {{accessToken}}`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nombre": "Gaming Pro",
  "descripcion": "Productos profesionales para gaming"
}
```

#### **📝 DELETE - Eliminar Categoría**
**Método:** `DELETE`
**URL:** `{{baseUrl}}/categorias/1`
**Authorization:** `Bearer {{accessToken}}`

### **📁 Folder: "Productos"**

#### **📝 GET - Listar Productos (Público)**
**Método:** `GET`
**URL:** `{{baseUrl}}/productos`
**Authorization:** (ninguna - es público)

#### **📝 GET - Producto por ID (Público)**
**Método:** `GET`
**URL:** `{{baseUrl}}/productos/1`
**Authorization:** (ninguna - es público)

#### **📝 POST - Crear Producto**
**Método:** `POST`
**URL:** `{{baseUrl}}/productos`
**Authorization:** `Bearer {{accessToken}}`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nombre": "RTX 4090",
  "descripcion": "Tarjeta gráfica NVIDIA GeForce RTX 4090",
  "precio": 1599.99,
  "stock": 10,
  "categoria": {
    "id": 1
  }
}
```

#### **📝 PUT - Actualizar Producto**
**Método:** `PUT`
**URL:** `{{baseUrl}}/productos/1`
**Authorization:** `Bearer {{accessToken}}`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nombre": "RTX 4090 Ti",
  "descripcion": "Tarjeta gráfica NVIDIA GeForce RTX 4090 Ti",
  "precio": 1799.99,
  "stock": 15
}
```

#### **📝 DELETE - Eliminar Producto**
**Método:** `DELETE`
**URL:** `{{baseUrl}}/productos/1`
**Authorization:** `Bearer {{accessToken}}`

#### **📝 GET - Buscar Productos**
**Método:** `GET`
**URL:** `{{baseUrl}}/productos/buscar?nombre=rtx`
**Authorization:** (ninguna)

### **📁 Folder: "Usuarios"**

#### **📝 GET - Listar Usuarios**
**Método:** `GET`
**URL:** `{{baseUrl}}/usuarios`
**Authorization:** `Bearer {{accessToken}}`

#### **📝 POST - Crear Usuario**
**Método:** `POST`
**URL:** `{{baseUrl}}/usuarios`
**Authorization:** `Bearer {{accessToken}}`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "nombre": "Ana García",
  "email": "ana@postman.com",
  "telefono": "+56987654321"
}
```

### **📁 Folder: "Carritos"**

#### **📝 GET - Listar Carritos**
**Método:** `GET`
**URL:** `{{baseUrl}}/carritos`
**Authorization:** `Bearer {{accessToken}}`

#### **📝 POST - Crear Carrito**
**Método:** `POST`
**URL:** `{{baseUrl}}/carritos`
**Authorization:** `Bearer {{accessToken}}`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "cliente": {
    "id": 1
  },
  "estado": "ACTIVE"
}
```

#### **📝 GET - Carritos Abandonados**
**Método:** `GET`
**URL:** `{{baseUrl}}/carritos/abandonados`
**Authorization:** `Bearer {{accessToken}}`

### **📁 Folder: "Pedidos"**

#### **📝 GET - Listar Pedidos**
**Método:** `GET`
**URL:** `{{baseUrl}}/pedidos`
**Authorization:** `Bearer {{accessToken}}`

#### **📝 POST - Crear Pedido**
**Método:** `POST`
**URL:** `{{baseUrl}}/pedidos`
**Authorization:** `Bearer {{accessToken}}`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "cliente": {
    "id": 1
  },
  "productos": ["RTX 4090", "Gaming Mouse"],
  "total": 1699.99,
  "estado": "PENDING"
}
```

---

## 🔧 **PARTE 4: MICROSERVICIOS**

### **📁 Folder: "Microservicios"**

#### **📝 API Gateway**
**Método:** `GET`
**URL:** `http://3.134.244.104:8080/`

#### **📝 Eureka Server**
**Método:** `GET`
**URL:** `http://3.134.244.104:8761/`

#### **📝 Cotizador - Health**
**Método:** `GET`
**URL:** `http://3.134.244.104:8082/health`

#### **📝 Cotizador - Crear Cotización**
**Método:** `POST`
**URL:** `http://3.134.244.104:8082/api/cotizacion`

**Headers:**
```
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "producto": "Laptop Gaming",
  "cantidad": 1,
  "precioUnitario": 2500.00
}
```

#### **📝 Gestor Solicitudes - Health**
**Método:** `GET`
**URL:** `http://3.134.244.104:8083/api/solicitudes/health`

#### **📝 Hello World Service**
**Método:** `GET`
**URL:** `http://3.134.244.104:8084/hello`

---

## 📱 **PARTE 5: BFF (Backend for Frontend)**

### **📁 Folder: "BFF"**

#### **📝 Web BFF - Health**
**Método:** `GET`
**URL:** `{{baseUrl}}/web/api/health`

#### **📝 Web BFF - Productos Completos**
**Método:** `GET`
**URL:** `{{baseUrl}}/web/api/productos/completo`

#### **📝 Mobile BFF - Health**
**Método:** `GET`
**URL:** `{{baseUrl}}/mobile/api/health`

#### **📝 Mobile BFF - Producto Quick**
**Método:** `GET`
**URL:** `{{baseUrl}}/mobile/api/productos/1/quick`

---

## 🎯 **SOLUCIÓN A TU ERROR 401**

**El error que recibiste:**
```json
{
    "error": true,
    "message": "Authentication required",
    "status": 401
}
```

**Significa que estás usando un endpoint que requiere autenticación.**

### **✅ SOLUCIONES:**

#### **Opción 1: Usar API de Terceros (SIN autenticación)**
```
GET http://3.134.244.104:8888/api/terceros/ObtenerDatos/categorias/1
```

#### **Opción 2: Autenticarte primero**
1. **Ejecutar Login:** `POST /api/auth/login`
2. **Copiar accessToken** de la respuesta
3. **Usar en Authorization:** `Bearer {token}`

### **🔧 Configuración Rápida en Postman:**

1. **Crear request de Login**
2. **Agregar script de Tests:**
```javascript
pm.collectionVariables.set("accessToken", pm.response.json().accessToken);
```
3. **En otros requests usar:** `{{accessToken}}`

---

## 📋 **EXPORT DE COLECCIÓN POSTMAN**

```json
{
  "info": {
    "name": "ARKA Valenzuela E-commerce",
    "description": "API completa con JWT y especificaciones de terceros",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://3.134.244.104:8888"
    },
    {
      "key": "accessToken",
      "value": ""
    },
    {
      "key": "refreshToken",
      "value": ""
    }
  ],
  "auth": {
    "type": "bearer",
    "bearer": [
      {
        "key": "token",
        "value": "{{accessToken}}"
      }
    ]
  }
}
```

**🎉 Con esta configuración tendrás TODO funcionando perfectamente en Postman!**
