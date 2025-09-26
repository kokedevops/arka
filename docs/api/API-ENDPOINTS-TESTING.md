# 🚀 ARKA E-commerce Platform - API Endpoints Testing Guide

## 📋 Índice
- [Endpoints Principales](#endpoints-principales)
- [Endpoints de Productos](#endpoints-de-productos)
- [Endpoints de Usuarios/Clientes](#endpoints-de-usuariosclientes)
- [Endpoints de Pedidos](#endpoints-de-pedidos)
- [Endpoints de Categorías](#endpoints-de-categorías)
- [Endpoints de Carritos](#endpoints-de-carritos)
- [Endpoints BFF Web](#endpoints-bff-web)
- [Endpoints BFF Mobile](#endpoints-bff-mobile)
- [Endpoints API de Terceros](#endpoints-api-de-terceros)
- [Endpoints de Microservicios](#endpoints-de-microservicios)
- [Ejemplos de Payloads](#ejemplos-de-payloads)

---

## 🏠 Endpoints Principales

### 1. Página de Inicio
```http
GET /
```
**Descripción:** Página principal de la aplicación ARKA
**Respuesta:** Información general de la aplicación

### 2. Health Check
```http
GET /health
```
**Descripción:** Estado de salud de la aplicación
**Respuesta:** Estado de conexiones a bases de datos y servicios

### 3. Información de Login
```http
GET /login
```
**Descripción:** Información sobre el endpoint de autenticación
**Respuesta:** Documentación del proceso de login

### 4. Información de la API
```http
GET /api
```
**Descripción:** Documentación general de la API
**Respuesta:** Lista de endpoints disponibles

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

---

## 📱 Endpoints BFF Mobile

### Base URL: `/mobile/api`

#### 1. Productos móvil
```http
GET /mobile/api/productos
```
**Respuesta:** Array de MobileProductDto optimizados

#### 2. Dashboard móvil
```http
GET /mobile/api/dashboard
```
**Respuesta:** Dashboard optimizado para móvil

#### 3. Resumen de carrito móvil
```http
GET /mobile/api/carrito/{cartId}/resumen
```
**Parámetros:**
- `cartId` (Long): ID del carrito
**Respuesta:** MobileCartDto

#### 4. Info rápida de producto
```http
GET /mobile/api/productos/{id}/quick
```
**Parámetros:**
- `id` (Long): ID del producto
**Respuesta:** MobileProductDto con datos mínimos

#### 5. Health check móvil
```http
GET /mobile/api/health
```
**Respuesta:** Estado del BFF Mobile

---

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
