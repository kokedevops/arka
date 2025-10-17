# 🏢 ARKA - PLATAFORMA E-COMMERCE MICROSERVICIOS

<div align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Java-21-orange" alt="Java"/>
  <img src="https://img.shields.io/badge/Docker-Enabled-blue" alt="Docker"/>
  <img src="https://img.shields.io/badge/Architecture-Hexagonal-orange" alt="Hexagonal"/>
  <img src="https://img.shields.io/badge/Security-JWT-red" alt="JWT"/>
  <img src="https://img.shields.io/badge/Cloud-AWS%20Ready-yellow" alt="AWS"/>
  <img src="https://img.shields.io/badge/License-MIT-green" alt="MIT"/>
</div>

---

## 📋 **ÍNDICE DE DOCUMENTACIÓN**

### 📚 **Documentación Esencial**
| Documento | Descripción | Uso |
|-----------|-------------|-----|
| **[🚀 Inicio Rápido](QUICK-START-GUIDE.md)** | Levanta el proyecto en 5 minutos | Desarrollo |
| **[📖 Guía Completa](GUIA_PASO_A_PASO_COMPLETA.md)** | Documentación técnica detallada | Desarrollo |
| **[🐳 Despliegue Docker](DOCKER-DEPLOYMENT-GUIDE.md)** | Containerización completa | DevOps |

### 🧪 **Testing y APIs**
| Documento | Descripción | Uso |
|-----------|-------------|-----|
| **[🔍 API Testing](API-ENDPOINTS-TESTING.md)** | Todos los endpoints con ejemplos | Testing |
| **[📮 Postman Guide](POSTMAN-GUIA-COMPLETA.md)** | Colección completa de Postman | Testing |
| **[✅ Testing Guide](GUIA-PRUEBAS-COMPLETA.md)** | Tests unitarios e integración | QA |

### 🛠️ **Operaciones**
| Documento | Descripción | Uso |
|-----------|-------------|-----|
| **[⚙️ Scripts](scripts/README.md)** | Automatización completa | DevOps |
| **[🏃 Ejecución](GUIA_EJECUCION_COMPLETA.md)** | Comandos y operaciones | Desarrollo |

---

**ARKA** es una **plataforma e-commerce empresarial** construida con **arquitectura de microservicios**, implementando patrones modernos de desarrollo, seguridad robusta y tecnologías cloud-native para máxima escalabilidad y mantenibilidad.

✅ **Arquitectura Hexagonal + DDD** - Separación clara de responsabilidades  
✅ **Programación Reactiva** - WebFlux con Mono/Flux  
✅ **Spring Cloud** - Eureka, Gateway, Config, Circuit Breakers  
✅ **Docker** - Containerización completa con Docker Compose  
✅ **Spring Security + JWT** - Autenticación y autorización robusta  
✅ **API de Terceros** - Endpoints CRUD según especificaciones  
✅ **Testing** - Pruebas unitarias, integración y API  

---

### 🎯 **Tecnologías Core**
- ✅ **Spring Boot 3.2.3** - Framework principal
- ✅ **Spring WebFlux** - Programación reactiva
- ✅ **Spring Cloud** - Microservicios y service discovery
- ✅ **Spring Security + JWT** - Autenticación y autorización
- ✅ **Arquitectura Hexagonal** - Clean Architecture
- ✅ **Domain-Driven Design** - Modelado del dominio

### 🛒 **Funcionalidades E-Commerce**
- 🛍️ **Gestión de productos** y categorías
- 🛒 **Carrito de compras** con detección de abandono
- 📋 **Sistema de pedidos** completo
- 👥 **Gestión de clientes** y usuarios
- 🔔 **Notificaciones automáticas** por email
- 📊 **Analytics** y reportes de negocio

### 📱💻 **BFF (Backend for Frontend)**
- 📱 **Mobile BFF** - API optimizada para móviles
- 💻 **Web BFF** - API optimizada para web
- 🎨 **DTOs especializados** por plataforma
- ⚡ **Responses optimizadas** según dispositivo

### 🔧 **Tecnologías Avanzadas**
- 🗄️ **Multi-Database**: MySQL + MongoDB
- 📧 **Sistema de emails** con MailHog para testing
- ⏰ **Scheduled tasks** para procesos automáticos
- 🐳 **Docker Compose** para orquestación
- 📈 **Monitoreo** y métricas
- 🔐 **Seguridad robusta** con JWT y roles

---

## 🏗️ **ARQUITECTURA DEL SISTEMA**

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Mobile App    │    │     Web App      │    │   Third Party   │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
                    ┌─────────────────────┐
                    │    API Gateway      │ ←── Load Balancer
                    │  (Port: 8080)       │
                    └─────────────────────┘
                                 │
                    ┌─────────────────────┐
                    │   Eureka Server     │ ←── Service Discovery
                    │  (Port: 8761)       │
                    └─────────────────────┘
                                 │
         ┌───────────────────────┼───────────────────────┐
         │                       │                       │
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  Cotizador      │    │ Gestor Solicitudes│  │ Hello World     │
│ (Port: 8081)    │    │  (Port: 8082)     │    │ (Port: 8083)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         └───────────────────────┼───────────────────────┘
                                 │
              ┌─────────────────────────────────────┐
              │          Databases & Tools          │
              │  MySQL + MongoDB + MailHog         │
              └─────────────────────────────────────┘
```

---

## 📋 **MICROSERVICIOS**

| Servicio | Puerto | Descripción | Tecnologías |
|----------|--------|-------------|-------------|
| **API Gateway** | 8080 | Punto de entrada, balanceador | Spring Cloud Gateway, JWT |
| **Eureka Server** | 8761 | Descubrimiento de servicios | Netflix Eureka |
| **Arca Cotizador** | 8081 | Cotizaciones y precios | WebFlux, MySQL, Reactive |
| **Gestor Solicitudes** | 8082 | Gestión de pedidos y clientes | WebFlux, MySQL, Security |
| **Hello World** | 8083 | Servicio de demostración | WebFlux, Load Balancing |
| **Security Common** | - | Librería compartida de seguridad | JWT, OAuth2, BCrypt |

---

## 🛒 **APIS E-COMMERCE DISPONIBLES**

### 🛍️ **Productos**
```bash
GET    /productos                    # Listar productos
GET    /productos/{id}               # Obtener producto
POST   /productos                    # Crear producto
PUT    /productos/{id}               # Actualizar producto
DELETE /productos/{id}               # Eliminar producto
GET    /productos/categoria/{cat}    # Productos por categoría
```

### 🛒 **Carrito de Compras**
```bash
GET    /carritos                     # Listar carritos
GET    /carritos/{id}                # Obtener carrito
POST   /carritos                     # Crear carrito
PUT    /carritos/{id}                # Actualizar carrito
DELETE /carritos/{id}                # Eliminar carrito
GET    /carritos/abandonados         # 🎯 Carritos abandonados
PUT    /carritos/{id}/activar        # Activar carrito
PUT    /carritos/{id}/abandonar      # Abandonar carrito
```

### 📋 **Pedidos**
```bash
GET    /pedidos                      # Listar pedidos
GET    /pedidos/{id}                 # Obtener pedido
POST   /pedidos                      # Crear pedido
PUT    /pedidos/{id}                 # Actualizar pedido
DELETE /pedidos/{id}                 # Eliminar pedido
GET    /pedidos/cliente/{customerId} # Pedidos por cliente
GET    /pedidos/producto/{productId} # Pedidos por producto
GET    /pedidos/rango-fechas         # Pedidos por fechas
```

### 👥 **Clientes**
```bash
GET    /clientes                     # Listar clientes
GET    /clientes/{id}                # Obtener cliente
POST   /clientes                     # Crear cliente
PUT    /clientes/{id}                # Actualizar cliente
DELETE /clientes/{id}                # Eliminar cliente
GET    /clientes/email/{email}       # Cliente por email
```

### 📱 **Mobile BFF**
```bash
GET    /mobile/api/productos/destacados    # Productos destacados móvil
GET    /mobile/api/productos/buscar        # Búsqueda optimizada móvil
GET    /mobile/api/carrito/{id}/resumen    # Resumen carrito móvil
```

### 💻 **Web BFF**
```bash
GET    /web/api/dashboard                  # Dashboard completo web
GET    /web/api/productos/{id}/detalle     # Detalle producto web
GET    /web/api/analytics/ventas           # Analytics ventas web
```

### 📊 **Analytics**
```bash
GET    /analytics/dashboard                # Dashboard principal
GET    /analytics/ventas                   # Métricas de ventas
GET    /analytics/productos-populares     # Productos más vendidos
```

---

## ⚡ **INICIO RÁPIDO**

### 📋 **Prerrequisitos**
- ☕ **Java 21+**
- 🐳 **Docker & Docker Compose**
- 🐘 **PostgreSQL/MySQL** (opcional, incluido en Docker)
- 🍃 **MongoDB** (opcional, incluido en Docker)

### 🚀 **Instalación y Ejecución**

#### 1️⃣ **Clonar el repositorio**
```bash
git clone https://github.com/kokedevops/arka.git
cd arka
```

#### 2️⃣ **Ejecutar con Docker Compose (RECOMENDADO)**
```bash
# Iniciar toda la infraestructura
docker-compose up -d

# Verificar que todos los servicios estén corriendo
docker-compose ps
```

#### 3️⃣ **Ejecutar manualmente**
```bash
# 1. Iniciar Eureka Server
cd eureka-server && ./gradlew bootRun &

# 2. Iniciar API Gateway
cd api-gateway && ./gradlew bootRun &

# 3. Iniciar microservicios
cd arca-cotizador && ./gradlew bootRun &
cd arca-gestor-solicitudes && ./gradlew bootRun &
cd hello-world-service && ./gradlew bootRun &
```

#### 4️⃣ **Scripts de automatización**
```bash
# Windows
scripts\start-spring-cloud.bat
scripts\start-all-services.bat
scripts\start-ecommerce-complete.bat

# Linux/Mac
scripts/start-spring-cloud.sh
scripts/start-all-services.sh
scripts/start-ecommerce-complete.sh
```

---

## 🌐 **URLS DE ACCESO**

| Servicio | URL | Descripción |
|----------|-----|-------------|
| **API Gateway** | http://localhost:8080 | Punto de entrada principal |
| **Eureka Dashboard** | http://localhost:8761 | Consola de servicios |
| **Arca Cotizador** | http://localhost:8081 | API de cotizaciones |
| **Gestor Solicitudes** | http://localhost:8082 | API de pedidos |
| **Hello World** | http://localhost:8083 | API de prueba |
| **MailHog UI** | http://localhost:8025 | Interfaz de emails |
| **MongoDB Express** | http://localhost:8081 | Interfaz MongoDB |

---

## 🔔 **SISTEMA DE NOTIFICACIONES**

### 📧 **Tipos de Notificaciones**
- ✅ **Carritos abandonados** - Recordatorios automáticos cada hora
- ✅ **Confirmación de pedidos** - Email inmediato al crear pedido
- ✅ **Alertas de stock** - Notificación cuando stock < 10
- ✅ **Promociones** - Emails personalizados a clientes

### ⏰ **Scheduler Automático**
```java
@Scheduled(fixedRate = 3600000) // Cada hora
public void detectAbandonedCarts() {
    // Detección y envío automático de emails
}

@Scheduled(fixedRate = 300000) // Demo cada 5 minutos
public void demoScheduledNotifications() {
    // Proceso de demostración
}
```

### 📧 **Configuración de Email**
```yaml
spring:
  mail:
    host: localhost
    port: 1025
    properties:
      mail.smtp.auth: false
      mail.smtp.starttls.enable: false
```

---

## 📊 **BASES DE DATOS**

### 🐬 **MySQL** - Datos principales
- 👥 Clientes
- 🛍️ Productos
- 🛒 Carritos
- 📋 Pedidos

### 🍃 **MongoDB** - Datos analíticos
- 📧 Historial de notificaciones
- 📊 Logs de eventos
- 📈 Métricas de uso

---

## 🔐 **SEGURIDAD**

### 🛡️ **Características de Seguridad**
- ✅ **JWT Tokens** con expiración configurable
- ✅ **OAuth2 Resource Server**
- ✅ **Roles y permisos** (USER, ADMIN, MODERATOR)
- ✅ **BCrypt** para encriptación de contraseñas
- ✅ **CORS** configurado para desarrollo
- ✅ **Rate limiting** en API Gateway

### 🔑 **Autenticación**
```bash
# Obtener token
POST /auth/login
{
  "username": "admin",
  "password": "admin123"
}

# Usar token en headers
Authorization: Bearer <jwt_token>
```

---

## 🧪 **TESTING**

### 🧩 **Tests Incluidos**
- ✅ **Unit Tests** con JUnit 5 y Mockito
- ✅ **Integration Tests** con TestContainers
- ✅ **Security Tests** para JWT y endpoints
- ✅ **Reactive Tests** con StepVerifier
- ✅ **Load Tests** con scripts de automatización

### 🏃‍♂️ **Ejecutar Tests**
```bash
# Todos los tests
./gradlew test

# Tests específicos de un módulo
./gradlew :arca-cotizador:test

# Tests de integración
./gradlew integrationTest

# Tests de seguridad
./gradlew :arka-security-common:test
```

---

## 📚 **DOCUMENTACIÓN COMPLETA**

### 📋 **Navegación Rápida**
- **[📚 ÍNDICE MAESTRO](DOCUMENTATION-INDEX.md)** - Toda la documentación organizada
- **[⚡ Quick Start](QUICK-START-GUIDE.md)** - Inicio en 5 minutos
- **[📝 API Testing](API-ENDPOINTS-TESTING.md)** - Testing completo de endpoints
- **[📮 Postman Guide](POSTMAN-GUIA-COMPLETA.md)** - Colección completa
- **[🐳 Docker Guide](DOCKER-DEPLOYMENT-GUIDE.md)** - Containerización
- **[🛠️ Scripts](scripts/README.md)** - Automatización y deployment

### 📖 **Documentación Detallada**

| Archivo | Descripción | Audiencia |
|---------|-------------|-----------|
| **[📚 ÍNDICE MAESTRO](DOCUMENTATION-INDEX.md)** | Navegación completa de toda la documentación | Todos |
| **[🛒 E-commerce Implementation](ECOMMERCE-COMPLETE-IMPLEMENTATION.md)** | Funcionalidades completas del e-commerce | Desarrolladores |
| **[📚 Guía Paso a Paso](GUIA_PASO_A_PASO_COMPLETA.md)** | Tutorial técnico completo | Desarrollo |
| **[🚀 Guía de Ejecución](GUIA_EJECUCION_COMPLETA.md)** | Deployment en producción | DevOps |
| **[🧪 Testing Completo](README-TESTING-COMPLETO.md)** | Validación y testing | QA/Testing |

---

## 🛠️ **DESARROLLO**

### 🏗️ **Estructura del Proyecto**
```
arkajvalenzuela/
├── api-gateway/              # Gateway principal
├── eureka-server/            # Descubrimiento de servicios
├── arca-cotizador/          # Servicio de cotizaciones
├── arca-gestor-solicitudes/ # Gestión de pedidos
├── hello-world-service/     # Servicio de prueba
├── arka-security-common/    # Librería de seguridad
├── config-repository/       # Configuraciones centralizadas
├── scripts/                 # Scripts de automatización
├── k8s/                     # Manifiestos Kubernetes
├── monitoring/              # Configuración de monitoreo
└── docker-compose.yml       # Orquestación Docker
```

### 🔧 **Variables de Entorno**
```bash
# Desarrollo
cp .env.dev .env

# Producción
cp .env.prod .env
```

### 📦 **Build del Proyecto**
```bash
# Build completo
./gradlew build

# Build sin tests
./gradlew build -x test

# Build específico
./gradlew :arca-cotizador:build
```

---

## 🐳 **DOCKER**

### 🚀 **Docker Compose Servicios**
- 🌐 **api-gateway**: Puerto 8080
- 🔍 **eureka-server**: Puerto 8761
- 💰 **arca-cotizador**: Puerto 8081
- 📋 **arca-gestor-solicitudes**: Puerto 8082
- 👋 **hello-world-service**: Puerto 8083
- 🐬 **mysql**: Puerto 3306
- 🍃 **mongodb**: Puerto 27017
- 📧 **mailhog**: Puerto 1025/8025

### 🎛️ **Comandos útiles**
```bash
# Iniciar servicios
docker-compose up -d

# Ver logs
docker-compose logs -f [servicio]

# Escalar servicio
docker-compose up -d --scale hello-world-service=3

# Parar servicios
docker-compose down

# Rebuild y restart
docker-compose up -d --build
```

---

## 🚀 **PRODUCCIÓN**

### 🏗️ **Deploy con Kubernetes**
```bash
# Aplicar manifiestos
kubectl apply -f k8s/

# Verificar deployment
kubectl get pods
kubectl get services
```

### 📊 **Monitoreo**
- ✅ **Prometheus** para métricas
- ✅ **Grafana** para dashboards
- ✅ **ELK Stack** para logs
- ✅ **Health checks** en todos los servicios

---

## 🤝 **CONTRIBUCIÓN**

### 🛠️ **Cómo Contribuir**
1. 🍴 Fork del proyecto
2. 🌿 Crear rama feature (`git checkout -b feature/AmazingFeature`)
3. 💾 Commit cambios (`git commit -m 'Add some AmazingFeature'`)
4. 📤 Push a la rama (`git push origin feature/AmazingFeature`)
5. 🔄 Abrir Pull Request

### 📋 **Convenciones**
- ✅ Seguir **Conventional Commits**
- ✅ Incluir **tests** para nuevas funcionalidades
- ✅ Documentar **APIs** con OpenAPI/Swagger
- ✅ Mantener **cobertura de tests** > 80%

---

## 🐛 **TROUBLESHOOTING**

### ❌ **Problemas Comunes**

#### 🔴 Servicios no se registran en Eureka
```bash
# Verificar que Eureka esté corriendo
curl http://localhost:8761/eureka/apps

# Reiniciar servicios en orden
docker-compose restart eureka-server
docker-compose restart api-gateway
```

#### 🔴 Errores de base de datos
```bash
# Verificar conexiones
docker-compose logs mysql
docker-compose logs mongodb

# Reset de datos
docker-compose down -v
docker-compose up -d
```

#### 🔴 Problemas de JWT
```bash
# Verificar configuración
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

---

## 📝 **CHANGELOG**

### 🆕 **v1.0.0** (Actual)
- ✅ Arquitectura completa de microservicios
- ✅ Sistema de e-commerce funcional
- ✅ BFF para mobile y web
- ✅ Sistema de notificaciones
- ✅ Analytics y reportes
- ✅ Docker Compose completo
- ✅ Seguridad JWT robusta

### 🔮 **Próximas Funcionalidades**
- 🚀 **Kafka** para eventos
- 🛒 **Payment Gateway** integration
- 📱 **Push Notifications**
- 🔍 **Elasticsearch** para búsquedas
- 🤖 **AI/ML** para recomendaciones

---

## 📄 **LICENCIA**

Este proyecto está licenciado bajo la Licencia MIT - ver el archivo [LICENSE](LICENSE) para más detalles.

---

## 👨‍💻 **AUTOR**

**Arka Valenzuela**
- 🌐 GitHub: [@kokedevops](https://github.com/kokedevops)
- 📧 Email: desarrollo@arka.com
- 💼 LinkedIn: [arka-valenzuela](https://linkedin.com/in/arka-valenzuela)

---

## 🙏 **AGRADECIMIENTOS**

- 🍃 **Spring Team** por el excelente framework
- 🐳 **Docker** por la containerización
- 🌥️ **Spring Cloud** por los patrones de microservicios
- 👥 **Comunidad Open Source** por las herramientas y librerías

---

<div align="center">

### ⭐ **¡Si te gusta el proyecto, dale una estrella!** ⭐

**[⬆ Volver al inicio](#-arka---sistema-de-microservicios-e-commerce)**

</div>
