# 🏗️ ARQUITECTURA HEXAGONAL - DIAGRAMAS Y ESTRUCTURA

## 🎯 **INTRODUCCIÓN A LA ARQUITECTURA HEXAGONAL**

La **Arquitectura Hexagonal** (también conocida como Ports & Adapters) permite crear aplicaciones que pueden ser igualmente conducidas por usuarios, programas, tests automatizados o scripts, y desarrolladas y testeadas aisladamente de sus bases de datos y servidores.

---

## 📐 **DIAGRAMA GENERAL DE LA ARQUITECTURA**

```
                    🌐 CLIENTES EXTERNOS
                           │
                    ┌──────┴──────┐
                    │             │
                📱 Mobile       💻 Web
                    │             │
                    └──────┬──────┘
                           │
              ┌─────────────────────────┐
              │     🚪 API GATEWAY      │ ← Punto de entrada único
              │    (Port: 8080)         │
              └─────────────────────────┘
                           │
              ┌─────────────────────────┐
              │   🗂️ EUREKA SERVER      │ ← Service Discovery
              │    (Port: 8761)         │
              └─────────────────────────┘
                           │
        ┌──────────────────┼──────────────────┐
        │                  │                  │
┌───────────────┐ ┌────────────────┐ ┌───────────────┐
│🧮 COTIZADOR   │ │📋 GESTOR       │ │👋 HELLO       │
│(Port: 8081)   │ │SOLICITUDES     │ │WORLD          │
│               │ │(Port: 8082)    │ │(Port: 8083)   │
└───────────────┘ └────────────────┘ └───────────────┘
        │                  │                  │
        └──────────────────┼──────────────────┘
                           │
              ┌─────────────────────────┐
              │   ⚙️ CONFIG SERVER      │ ← Configuración centralizada
              │    (Port: 8888)         │
              └─────────────────────────┘
                           │
              ┌─────────────────────────┐
              │  🗄️ BASES DE DATOS      │
              │   MySQL + MongoDB       │
              └─────────────────────────┘
```

---

## 🎯 **ARQUITECTURA HEXAGONAL POR MICROSERVICIO**

### 🧮 **MICROSERVICIO: ARCA COTIZADOR**

```
                🌐 HTTP Requests
                      │
        ┌─────────────┼─────────────┐
        │             │             │
  📱 Mobile API   💻 Web API   🔧 Admin API
        │             │             │
        └─────────────┼─────────────┘
                      │
    ╔═══════════════════════════════════════╗
    ║           🔵 INFRASTRUCTURE            ║
    ║                                       ║
    ║  📋 Controllers (Adaptadores Entrada) ║
    ║  ├── CotizacionController.java        ║
    ║  ├── ProductoController.java          ║
    ║  └── ClienteController.java           ║
    ║                                       ║
    ║  🔄 Web Mappers                       ║
    ║  ├── CotizacionMapper.java            ║
    ║  └── ClienteMapper.java               ║
    ╚═══════════════════════════════════════╝
                      │
                      ▼
    ╔═══════════════════════════════════════╗
    ║            🟢 APPLICATION              ║
    ║                                       ║
    ║  ⚙️ Use Case Services                  ║
    ║  ├── CotizacionApplicationService     ║
    ║  ├── ClienteApplicationService        ║
    ║  └── ProductoApplicationService       ║
    ║                                       ║
    ║  🔄 Orchestration Logic               ║
    ╚═══════════════════════════════════════╝
                      │
                      ▼
    ╔═══════════════════════════════════════╗
    ║             🟡 DOMAIN                  ║
    ║                                       ║
    ║  📋 Domain Models                     ║
    ║  ├── Cotizacion.java                  ║
    ║  ├── Cliente.java                     ║
    ║  └── Producto.java                    ║
    ║                                       ║
    ║  🔌 Ports (Interfaces)                ║
    ║  ├── CotizacionUseCase (in)           ║
    ║  ├── CotizacionRepository (out)       ║
    ║  └── NotificationService (out)        ║
    ║                                       ║
    ║  ⚡ Domain Services                    ║
    ║  ├── PriceCalculator.java             ║
    ║  └── CotizacionValidator.java         ║
    ╚═══════════════════════════════════════╝
                      │
                      ▼
    ╔═══════════════════════════════════════╗
    ║         🔵 INFRASTRUCTURE              ║
    ║                                       ║
    ║  🗄️ Persistence Adapters              ║
    ║  ├── CotizacionPersistenceAdapter     ║
    ║  ├── ClientePersistenceAdapter        ║
    ║  └── ProductoPersistenceAdapter       ║
    ║                                       ║
    ║  🗃️ JPA Entities                      ║
    ║  ├── CotizacionEntity.java            ║
    ║  ├── ClienteEntity.java               ║
    ║  └── ProductoEntity.java              ║
    ║                                       ║
    ║  🔄 Persistence Mappers               ║
    ║  └── CotizacionPersistenceMapper      ║
    ╚═══════════════════════════════════════╝
                      │
                      ▼
              🗄️ MySQL Database
```

### 📋 **MICROSERVICIO: GESTOR SOLICITUDES**

```
                🌐 HTTP/WebFlux
                      │
        ┌─────────────┼─────────────┐
        │             │             │
  🔒 Secured API  📊 Analytics   🔔 Notifications
        │             │             │
        └─────────────┼─────────────┘
                      │
    ╔═══════════════════════════════════════╗
    ║           🔵 INFRASTRUCTURE            ║
    ║                                       ║
    ║  🎮 Reactive Controllers               ║
    ║  ├── SolicitudReactiveController      ║
    ║  ├── PedidoReactiveController         ║
    ║  └── CarritoReactiveController        ║
    ║                                       ║
    ║  🔒 Security Filters                   ║
    ║  ├── JwtAuthenticationFilter          ║
    ║  └── RoleAuthorizationFilter          ║
    ╚═══════════════════════════════════════╝
                      │
                      ▼
    ╔═══════════════════════════════════════╗
    ║            🟢 APPLICATION              ║
    ║                                       ║
    ║  ⚡ Reactive Use Cases                 ║
    ║  ├── SolicitudReactiveService         ║
    ║  ├── PedidoReactiveService            ║
    ║  └── CarritoReactiveService           ║
    ║                                       ║
    ║  🔄 Event Handling                    ║
    ║  ├── SolicitudEventHandler            ║
    ║  └── PedidoEventHandler               ║
    ╚═══════════════════════════════════════╝
                      │
                      ▼
    ╔═══════════════════════════════════════╗
    ║             🟡 DOMAIN                  ║
    ║                                       ║
    ║  📦 Aggregates                        ║
    ║  ├── Solicitud (Root)                 ║
    ║  ├── Pedido (Root)                    ║
    ║  └── Carrito (Root)                   ║
    ║                                       ║
    ║  🎭 Value Objects                     ║
    ║  ├── SolicitudId                      ║
    ║  ├── ClienteInfo                      ║
    ║  └── EstadoSolicitud                  ║
    ║                                       ║
    ║  🔌 Domain Ports                      ║
    ║  ├── SolicitudRepository              ║
    ║  ├── PedidoRepository                 ║
    ║  └── NotificationPort                 ║
    ╚═══════════════════════════════════════╝
                      │
                      ▼
    ╔═══════════════════════════════════════╗
    ║         🔵 INFRASTRUCTURE              ║
    ║                                       ║
    ║  🔌 External Adapters                 ║
    ║  ├── EmailNotificationAdapter         ║
    ║  ├── SmsNotificationAdapter           ║
    ║  └── ApiTercerosAdapter               ║
    ║                                       ║
    ║  🌊 Reactive Repositories             ║
    ║  ├── SolicitudReactiveRepository      ║
    ║  └── PedidoReactiveRepository         ║
    ╚═══════════════════════════════════════╝
                      │
                      ▼
        🗄️ MySQL + 📧 Email + 📱 SMS
```

---

## 🔌 **PATRÓN PORTS & ADAPTERS DETALLADO**

### 🚪 **PUERTOS (PORTS)**

```
🔌 PUERTOS DE ENTRADA (DRIVING PORTS)
├── CotizacionUseCase.java
│   ├── generarCotizacion(SolicitudCotizacion)
│   ├── aprobarCotizacion(CotizacionId)
│   └── consultarCotizacion(CotizacionId)
│
├── SolicitudUseCase.java
│   ├── crearSolicitud(DatosSolicitud)
│   ├── actualizarEstado(SolicitudId, Estado)
│   └── consultarSolicitudes(FiltroSolicitud)
│
└── CarritoUseCase.java
    ├── agregarProducto(CarritoId, ProductoId)
    ├── actualizarCantidad(CarritoId, ProductoId, Cantidad)
    └── confirmarPedido(CarritoId)

🔌 PUERTOS DE SALIDA (DRIVEN PORTS)
├── CotizacionRepositoryPort.java
│   ├── save(Cotizacion): Mono<Cotizacion>
│   ├── findById(CotizacionId): Mono<Cotizacion>
│   └── findByCliente(ClienteId): Flux<Cotizacion>
│
├── NotificationPort.java
│   ├── enviarEmail(Email, Contenido): Mono<Void>
│   ├── enviarSms(Telefono, Mensaje): Mono<Void>
│   └── enviarNotificacionPush(Usuario, Notificacion): Mono<Void>
│
└── ExternalApiPort.java
    ├── obtenerDatos(Tabla): Flux<DatosExternos>
    ├── guardarDatos(Tabla, Datos): Mono<DatosExternos>
    └── eliminarDatos(Tabla, Id): Mono<Void>
```

### ⚙️ **ADAPTADORES (ADAPTERS)**

```
🔵 ADAPTADORES DE ENTRADA (DRIVING ADAPTERS)
├── web/
│   ├── CotizacionController.java      ← REST API
│   ├── SolicitudWebController.java    ← Web Interface
│   └── CarritoMobileController.java   ← Mobile API
│
├── messaging/
│   ├── SolicitudMessageHandler.java   ← Message Queue
│   └── EventBusHandler.java           ← Event Bus
│
└── scheduler/
    ├── CotizacionScheduler.java       ← Cron Jobs
    └── CarritoAbandonoScheduler.java  ← Background Tasks

🔵 ADAPTADORES DE SALIDA (DRIVEN ADAPTERS)
├── persistence/
│   ├── CotizacionJpaAdapter.java      ← MySQL
│   ├── SolicitudMongoAdapter.java     ← MongoDB
│   └── CacheRedisAdapter.java         ← Redis
│
├── notification/
│   ├── EmailSmtpAdapter.java          ← SMTP
│   ├── SmsHttpAdapter.java            ← SMS Service
│   └── PushNotificationAdapter.java   ← Firebase
│
└── external/
    ├── ApiTercerosHttpAdapter.java     ← External API
    ├── PaymentGatewayAdapter.java      ← Payment
    └── ShippingServiceAdapter.java     ← Shipping
```

---

## 🌊 **FLUJO DE DATOS REACTIVO**

### ⚡ **Flujo Completo: Crear Solicitud**

```
1. 🌐 HTTP Request
   POST /solicitudes
   Content-Type: application/json
   {
     "clienteId": "12345",
     "productos": [...],
     "observaciones": "Urgente"
   }
   
   ↓

2. 🎮 SolicitudController (Infrastructure/In)
   @PostMapping("/solicitudes")
   public Mono<ResponseEntity<SolicitudDTO>> crear(
       @RequestBody @Valid CrearSolicitudDTO dto
   ) {
       return solicitudUseCase.crearSolicitud(dto)
           .map(solicitud -> webMapper.toDTO(solicitud))
           .map(ResponseEntity::ok)
           .onErrorReturn(ResponseEntity.badRequest().build());
   }
   
   ↓

3. 🔄 Web Mapper (DTO → Domain)
   public Solicitud toDomain(CrearSolicitudDTO dto) {
       return Solicitud.builder()
           .clienteId(new ClienteId(dto.getClienteId()))
           .productos(mapProductos(dto.getProductos()))
           .observaciones(dto.getObservaciones())
           .build();
   }
   
   ↓

4. 🟢 SolicitudApplicationService (Application)
   @Override
   public Mono<Solicitud> crearSolicitud(CrearSolicitudDTO dto) {
       return Mono.fromCallable(() -> webMapper.toDomain(dto))
           .flatMap(this::validarSolicitud)
           .flatMap(this::asignarNumeroSolicitud)
           .flatMap(solicitudRepository::save)
           .flatMap(this::enviarNotificacionCreacion);
   }
   
   ↓

5. 🟡 Domain Validation (Domain)
   private Mono<Solicitud> validarSolicitud(Solicitud solicitud) {
       return Mono.fromCallable(() -> {
           if (!solicitud.esValida()) {
               throw new SolicitudInvalidaException();
           }
           return solicitud.cambiarEstado(EstadoSolicitud.EN_REVISION);
       });
   }
   
   ↓

6. 🔌 Repository Port (Domain/Out)
   public interface SolicitudRepositoryPort {
       Mono<Solicitud> save(Solicitud solicitud);
       Mono<Solicitud> findById(SolicitudId id);
       Flux<Solicitud> findByEstado(EstadoSolicitud estado);
   }
   
   ↓

7. 🔵 SolicitudPersistenceAdapter (Infrastructure/Out)
   @Override
   public Mono<Solicitud> save(Solicitud solicitud) {
       return Mono.fromCallable(() -> persistenceMapper.toEntity(solicitud))
           .flatMap(entity -> solicitudJpaRepository.save(entity))
           .map(savedEntity -> persistenceMapper.toDomain(savedEntity));
   }
   
   ↓

8. 🔄 Persistence Mapper (Domain → Entity)
   public SolicitudEntity toEntity(Solicitud solicitud) {
       return SolicitudEntity.builder()
           .id(solicitud.getId().getValue())
           .clienteId(solicitud.getClienteId().getValue())
           .estado(solicitud.getEstado().name())
           .fechaCreacion(solicitud.getFechaCreacion())
           .build();
   }
   
   ↓

9. 🗄️ MySQL Database
   INSERT INTO solicitudes (id, cliente_id, estado, fecha_creacion, ...)
   VALUES (?, ?, ?, ?, ...)
   
   ↓

10. 📧 Notification Service (Async)
    private Mono<Solicitud> enviarNotificacionCreacion(Solicitud solicitud) {
        return notificationPort
            .enviarEmail(solicitud.getClienteEmail(), 
                        "Solicitud creada: " + solicitud.getNumero())
            .thenReturn(solicitud);
    }
    
    ↓

11. 📱 Response to Client
    {
      "id": "SOL-2025-001",
      "estado": "EN_REVISION",
      "fechaCreacion": "2025-09-08T10:30:00Z",
      "cliente": {...},
      "productos": [...]
    }
```

---

## 🎯 **SEPARACIÓN DE RESPONSABILIDADES**

### 🟡 **CAPA DE DOMINIO (DOMAIN)**

```
📋 RESPONSABILIDADES:
✅ Reglas de negocio puras
✅ Entidades de dominio
✅ Value Objects
✅ Domain Services
✅ Domain Events
✅ Invariantes del dominio

❌ NO DEBE CONTENER:
❌ Anotaciones de JPA/MongoDB
❌ Referencias a Spring Framework
❌ Lógica de persistencia
❌ Lógica de presentación
❌ Detalles de infraestructura

📁 ESTRUCTURA:
domain/
├── model/
│   ├── Cotizacion.java          ← Entidad raíz
│   ├── Cliente.java             ← Entidad
│   ├── ProductoId.java          ← Value Object
│   └── EstadoCotizacion.java    ← Enum/Value Object
├── service/
│   ├── CotizacionDomainService.java
│   └── PrecioCalculatorService.java
├── port/
│   ├── in/                      ← Use Cases
│   └── out/                     ← Repository Ports
└── event/
    ├── CotizacionCreada.java    ← Domain Event
    └── CotizacionAprobada.java  ← Domain Event
```

### 🟢 **CAPA DE APLICACIÓN (APPLICATION)**

```
📋 RESPONSABILIDADES:
✅ Orquestación de casos de uso
✅ Coordinación entre servicios
✅ Transacciones
✅ Manejo de eventos
✅ Validaciones de aplicación

❌ NO DEBE CONTENER:
❌ Reglas de negocio complejas
❌ Detalles de persistencia
❌ Lógica de presentación
❌ Detalles de infraestructura

📁 ESTRUCTURA:
application/
├── usecase/
│   ├── CotizacionApplicationService.java
│   ├── SolicitudApplicationService.java
│   └── CarritoApplicationService.java
├── event/
│   ├── CotizacionEventHandler.java
│   └── SolicitudEventHandler.java
└── dto/
    ├── CrearCotizacionCommand.java
    └── ActualizarSolicitudCommand.java
```

### 🔵 **CAPA DE INFRAESTRUCTURA (INFRASTRUCTURE)**

```
📋 RESPONSABILIDADES:
✅ Detalles técnicos
✅ Frameworks (Spring, JPA)
✅ Bases de datos
✅ APIs externas
✅ Configuración
✅ Adaptadores

❌ NO DEBE CONTENER:
❌ Reglas de negocio
❌ Lógica de aplicación

📁 ESTRUCTURA:
infrastructure/
├── adapter/
│   ├── in/                      ← Driving Adapters
│   │   ├── web/
│   │   ├── messaging/
│   │   └── scheduler/
│   └── out/                     ← Driven Adapters
│       ├── persistence/
│       ├── notification/
│       └── external/
├── config/
│   ├── BeanConfiguration.java
│   ├── SecurityConfiguration.java
│   └── DatabaseConfiguration.java
└── common/
    ├── exception/
    ├── validation/
    └── mapper/
```

---

## 🔒 **PRINCIPIOS DE INVERSIÓN DE DEPENDENCIAS**

### ⬆️ **Dependencias Apuntan hacia el Dominio**

```
🔵 INFRASTRUCTURE
        ↑
        │ (implementa)
        │
🟢 APPLICATION
        ↑
        │ (usa)
        │
🟡 DOMAIN ← ¡Núcleo estable!
```

### 🔌 **Inyección de Dependencias**

```java
// 🏗️ BeanConfiguration.java (Infrastructure)
@Configuration
public class BeanConfiguration {
    
    // 🔌 Definir adaptadores de salida
    @Bean
    public CotizacionRepositoryPort cotizacionRepository(
            CotizacionJpaRepository jpaRepository,
            CotizacionPersistenceMapper mapper) {
        return new CotizacionPersistenceAdapter(jpaRepository, mapper);
    }
    
    // 🟢 Definir servicios de aplicación
    @Bean
    public CotizacionUseCase cotizacionUseCase(
            CotizacionRepositoryPort repository,
            NotificationPort notificationPort) {
        return new CotizacionApplicationService(repository, notificationPort);
    }
    
    // 📧 Definir adaptadores de notificación
    @Bean
    public NotificationPort notificationService() {
        return new EmailNotificationAdapter();
    }
}
```

---

## 🧪 **BENEFICIOS DE LA ARQUITECTURA**

### ✅ **Testabilidad**

```java
// 🧪 Test unitario del dominio (sin dependencias)
@Test
void deberia_generar_cotizacion_con_descuento() {
    // Given
    Cliente cliente = ClienteTestData.clienteVip();
    List<Producto> productos = ProductoTestData.productosEstandar();
    
    // When
    Cotizacion cotizacion = new Cotizacion(cliente, productos);
    cotizacion.aplicarDescuentoVip();
    
    // Then
    assertThat(cotizacion.getPrecioTotal())
        .isLessThan(cotizacion.getPrecioSinDescuento());
}

// 🧪 Test de aplicación (con mocks)
@Test
void deberia_crear_solicitud_y_enviar_notificacion() {
    // Given
    SolicitudRepositoryPort mockRepository = mock(SolicitudRepositoryPort.class);
    NotificationPort mockNotification = mock(NotificationPort.class);
    
    when(mockRepository.save(any()))
        .thenReturn(Mono.just(solicitudGuardada));
    when(mockNotification.enviarEmail(any(), any()))
        .thenReturn(Mono.empty());
    
    SolicitudApplicationService service = 
        new SolicitudApplicationService(mockRepository, mockNotification);
    
    // When & Then
    StepVerifier.create(service.crearSolicitud(comando))
        .expectNext(solicitudEsperada)
        .verifyComplete();
}
```

### ✅ **Flexibilidad Tecnológica**

```java
// 🔄 Cambio de MySQL a PostgreSQL
@Bean
@Profile("postgresql")
public CotizacionRepositoryPort cotizacionRepositoryPostgres() {
    return new CotizacionPostgreSQLAdapter();
}

// 🔄 Cambio de Email SMTP a SendGrid
@Bean
@Profile("sendgrid")
public NotificationPort notificationSendGrid() {
    return new SendGridNotificationAdapter();
}

// 🔄 Cambio a API de terceros
@Bean
@Profile("external-api")
public CotizacionRepositoryPort cotizacionRepositoryApi() {
    return new CotizacionApiTercerosAdapter();
}
```

### ✅ **Evolución Independiente**

```
🔵 Infrastructure: Puede cambiar sin afectar dominio
├── MySQL → PostgreSQL ✅
├── REST → GraphQL ✅
├── SMTP → SendGrid ✅
└── Sync → Async ✅

🟢 Application: Puede reorganizarse sin afectar dominio
├── Nuevos casos de uso ✅
├── Orquestación diferente ✅
├── Transacciones distintas ✅
└── Eventos adicionales ✅

🟡 Domain: Evoluciona solo por cambios de negocio
├── Nuevas reglas ✅
├── Entidades adicionales ✅
├── Value Objects nuevos ✅
└── Servicios de dominio ✅
```

---

## 🎯 **VALIDACIÓN DE LA ARQUITECTURA**

### ✅ **Checklist de Cumplimiento**

#### 🟡 **Dominio**
- [x] Sin dependencias a frameworks
- [x] Reglas de negocio encapsuladas
- [x] Entidades con invariantes
- [x] Puertos bien definidos
- [x] Value Objects inmutables

#### 🟢 **Aplicación**
- [x] Servicios implementan casos de uso
- [x] Orquestación sin lógica de negocio
- [x] Manejo de transacciones
- [x] Coordinación de eventos

#### 🔵 **Infraestructura**
- [x] Adaptadores implementan puertos
- [x] Separación entrada/salida
- [x] Configuración centralizada
- [x] Mappers entre capas

### 🔍 **Tests de Arquitectura**

```java
@Test
void domain_no_debe_depender_de_infrastructure() {
    JavaClasses classes = new ClassFileImporter()
        .importPackages("com.arka.arka");
    
    ArchRule rule = classes()
        .that().resideInAPackage("..domain..")
        .should().onlyDependOnClassesInPackages(
            "..domain..", "java..", "javax.."
        );
    
    rule.check(classes);
}

@Test
void application_no_debe_depender_de_infrastructure() {
    JavaClasses classes = new ClassFileImporter()
        .importPackages("com.arka.arka");
    
    ArchRule rule = classes()
        .that().resideInAPackage("..application..")
        .should().onlyDependOnClassesInPackages(
            "..domain..", "..application..", "java..", "javax.."
        );
    
    rule.check(classes);
}
```

---

*Documentación de Arquitectura Hexagonal*  
*Proyecto: Arka Valenzuela*  
*Implementado en 6+ microservicios*  
*Fecha: 8 de Septiembre de 2025*
