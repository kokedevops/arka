# 📁 ESTRUCTURA DE CARPETAS - ESTRATEGIA Y ORGANIZACIÓN

## 🎯 **FILOSOFÍA DE ORGANIZACIÓN**

La estructura de carpetas del proyecto **Arka Valenzuela** implementa los principios de **Arquitectura Hexagonal** y **Domain-Driven Design**, priorizando la **separación de responsabilidades** y la **independencia del dominio** sobre la organización técnica tradicional.

---

## 🏗️ **ESTRATEGIA DE ORGANIZACIÓN**

### 📋 **Principios Fundamentales**

1. **🎯 Organización por Dominio** (no por tipo técnico)
2. **🔌 Separación de Puertos y Adaptadores**
3. **⬆️ Inversión de Dependencias hacia el dominio**
4. **📦 Cohesión alta, Acoplamiento bajo**
5. **🧪 Facilitar testing independiente por capas**

### 🎨 **Comparación: Antes vs Después**

```
❌ ORGANIZACIÓN TRADICIONAL (Por tipo técnico):
src/main/java/
├── controllers/     ← Todos los controladores
├── services/        ← Todos los servicios
├── repositories/    ← Todos los repositorios
├── entities/        ← Todas las entidades
├── dtos/           ← Todos los DTOs
└── configs/        ← Todas las configuraciones

✅ ORGANIZACIÓN HEXAGONAL (Por responsabilidad):
src/main/java/com/arka/[microservicio]/
├── domain/         ← Núcleo del negocio
├── application/    ← Casos de uso
├── infrastructure/ ← Detalles técnicos
└── config/        ← Configuración Spring
```

---

## 📂 **ESTRUCTURA DETALLADA POR MICROSERVICIO**

### 🧮 **MICROSERVICIO: ARCA-COTIZADOR**

```
arca-cotizador/
├── 📋 build.gradle                    ← Configuración del módulo
├── 🐳 Dockerfile                      ← Containerización
├── 📄 README.md                       ← Documentación específica
│
└── src/
    ├── main/
    │   ├── java/com/arka/cotizador/
    │   │   │
    │   │   ├── 🟡 domain/             ← CAPA DE DOMINIO
    │   │   │   ├── model/             ← Entidades de negocio puras
    │   │   │   │   ├── Cotizacion.java
    │   │   │   │   ├── Cliente.java
    │   │   │   │   ├── Producto.java
    │   │   │   │   ├── PrecioCalculado.java
    │   │   │   │   └── EstadoCotizacion.java
    │   │   │   │
    │   │   │   ├── service/           ← Servicios de dominio
    │   │   │   │   ├── PriceCalculatorDomainService.java
    │   │   │   │   ├── CotizacionValidatorService.java
    │   │   │   │   └── DiscountCalculatorService.java
    │   │   │   │
    │   │   │   ├── port/              ← Contratos/Interfaces
    │   │   │   │   ├── in/            ← Puertos de entrada (Use Cases)
    │   │   │   │   │   ├── CotizacionUseCase.java
    │   │   │   │   │   ├── ClienteUseCase.java
    │   │   │   │   │   └── ProductoUseCase.java
    │   │   │   │   │
    │   │   │   │   └── out/           ← Puertos de salida (Repositories)
    │   │   │   │       ├── CotizacionRepositoryPort.java
    │   │   │   │       ├── ClienteRepositoryPort.java
    │   │   │   │       ├── ProductoRepositoryPort.java
    │   │   │   │       ├── NotificationPort.java
    │   │   │   │       └── ExternalPricingPort.java
    │   │   │   │
    │   │   │   ├── event/             ← Eventos de dominio
    │   │   │   │   ├── CotizacionCreada.java
    │   │   │   │   ├── CotizacionAprobada.java
    │   │   │   │   └── CotizacionVencida.java
    │   │   │   │
    │   │   │   └── exception/         ← Excepciones de dominio
    │   │   │       ├── CotizacionNotFoundException.java
    │   │   │       ├── ClienteInvalidoException.java
    │   │   │       └── PrecioInvalidoException.java
    │   │   │
    │   │   ├── 🟢 application/        ← CAPA DE APLICACIÓN
    │   │   │   ├── usecase/           ← Implementación de casos de uso
    │   │   │   │   ├── CotizacionApplicationService.java
    │   │   │   │   ├── ClienteApplicationService.java
    │   │   │   │   └── ProductoApplicationService.java
    │   │   │   │
    │   │   │   ├── command/           ← Commands (CQRS)
    │   │   │   │   ├── CrearCotizacionCommand.java
    │   │   │   │   ├── AprobarCotizacionCommand.java
    │   │   │   │   └── ActualizarClienteCommand.java
    │   │   │   │
    │   │   │   ├── query/             ← Queries (CQRS)
    │   │   │   │   ├── BuscarCotizacionQuery.java
    │   │   │   │   ├── ListarClientesQuery.java
    │   │   │   │   └── ReporteCotizacionesQuery.java
    │   │   │   │
    │   │   │   └── event/             ← Manejadores de eventos
    │   │   │       ├── CotizacionEventHandler.java
    │   │   │       └── ClienteEventHandler.java
    │   │   │
    │   │   ├── 🔵 infrastructure/     ← CAPA DE INFRAESTRUCTURA
    │   │   │   ├── adapter/
    │   │   │   │   ├── in/            ← Adaptadores de entrada (Driving)
    │   │   │   │   │   ├── web/       ← REST Controllers
    │   │   │   │   │   │   ├── CotizacionController.java
    │   │   │   │   │   │   ├── ClienteController.java
    │   │   │   │   │   │   ├── ProductoController.java
    │   │   │   │   │   │   ├── ReportesController.java
    │   │   │   │   │   │   │
    │   │   │   │   │   │   ├── dto/   ← DTOs para web
    │   │   │   │   │   │   │   ├── CotizacionDTO.java
    │   │   │   │   │   │   │   ├── ClienteDTO.java
    │   │   │   │   │   │   │   ├── ProductoDTO.java
    │   │   │   │   │   │   │   ├── CrearCotizacionRequest.java
    │   │   │   │   │   │   │   └── CotizacionResponse.java
    │   │   │   │   │   │   │
    │   │   │   │   │   │   └── mapper/ ← Mappers web (DTO ↔ Domain)
    │   │   │   │   │   │       ├── CotizacionWebMapper.java
    │   │   │   │   │   │       ├── ClienteWebMapper.java
    │   │   │   │   │   │       └── ProductoWebMapper.java
    │   │   │   │   │   │
    │   │   │   │   │   ├── messaging/ ← Message Handlers
    │   │   │   │   │   │   ├── CotizacionMessageHandler.java
    │   │   │   │   │   │   └── EventMessageHandler.java
    │   │   │   │   │   │
    │   │   │   │   │   └── scheduler/ ← Scheduled Tasks
    │   │   │   │   │       ├── CotizacionVencimientoScheduler.java
    │   │   │   │   │       └── ReportsScheduler.java
    │   │   │   │   │
    │   │   │   │   └── out/           ← Adaptadores de salida (Driven)
    │   │   │   │       ├── persistence/ ← Persistencia
    │   │   │   │       │   ├── entity/ ← JPA Entities
    │   │   │   │       │   │   ├── CotizacionEntity.java
    │   │   │   │       │   │   ├── ClienteEntity.java
    │   │   │   │       │   │   ├── ProductoEntity.java
    │   │   │   │       │   │   └── RelCotizacionProducto.java
    │   │   │   │       │   │
    │   │   │   │       │   ├── repository/ ← JPA Repositories
    │   │   │   │       │   │   ├── CotizacionJpaRepository.java
    │   │   │   │       │   │   ├── ClienteJpaRepository.java
    │   │   │   │       │   │   └── ProductoJpaRepository.java
    │   │   │   │       │   │
    │   │   │   │       │   ├── mapper/ ← Mappers persistencia
    │   │   │   │       │   │   ├── CotizacionPersistenceMapper.java
    │   │   │   │       │   │   ├── ClientePersistenceMapper.java
    │   │   │   │       │   │   └── ProductoPersistenceMapper.java
    │   │   │   │       │   │
    │   │   │   │       │   └── CotizacionPersistenceAdapter.java
    │   │   │   │       │   └── ClientePersistenceAdapter.java
    │   │   │   │       │   └── ProductoPersistenceAdapter.java
    │   │   │   │       │
    │   │   │   │       ├── notification/ ← Notificaciones externas
    │   │   │   │       │   ├── EmailNotificationAdapter.java
    │   │   │   │       │   ├── SmsNotificationAdapter.java
    │   │   │   │       │   └── PushNotificationAdapter.java
    │   │   │   │       │
    │   │   │   │       ├── external/   ← APIs externas
    │   │   │   │       │   ├── ExternalPricingAdapter.java
    │   │   │   │       │   ├── CurrencyExchangeAdapter.java
    │   │   │   │       │   └── TaxCalculationAdapter.java
    │   │   │   │       │
    │   │   │   │       └── cache/      ← Caché
    │   │   │   │           ├── RedisCacheAdapter.java
    │   │   │   │           └── InMemoryCacheAdapter.java
    │   │   │   │
    │   │   │   ├── config/            ← Configuración Spring
    │   │   │   │   ├── BeanConfiguration.java
    │   │   │   │   ├── DatabaseConfiguration.java
    │   │   │   │   ├── SecurityConfiguration.java
    │   │   │   │   ├── WebConfiguration.java
    │   │   │   │   └── MessagingConfiguration.java
    │   │   │   │
    │   │   │   ├── security/          ← Seguridad específica
    │   │   │   │   ├── JwtTokenProvider.java
    │   │   │   │   ├── CustomUserDetailsService.java
    │   │   │   │   └── SecurityEventHandler.java
    │   │   │   │
    │   │   │   └── common/            ← Utilidades comunes
    │   │   │       ├── exception/     ← Exception Handlers
    │   │   │       │   ├── GlobalExceptionHandler.java
    │   │   │       │   └── CustomErrorResponse.java
    │   │   │       │
    │   │   │       ├── validation/    ← Validadores
    │   │   │       │   ├── CotizacionValidator.java
    │   │   │       │   └── ClienteValidator.java
    │   │   │       │
    │   │   │       └── util/         ← Utilidades
    │   │   │           ├── DateUtils.java
    │   │   │           ├── PriceUtils.java
    │   │   │           └── SecurityUtils.java
    │   │   │
    │   │   ├── CotizadorApplication.java  ← Main Class
    │   │   └── ServletInitializer.java    ← WAR deployment
    │   │
    │   └── resources/
    │       ├── application.yml        ← Configuración principal
    │       ├── bootstrap.yml          ← Config Server bootstrap
    │       ├── application-dev.yml    ← Perfil desarrollo
    │       ├── application-prod.yml   ← Perfil producción
    │       ├── logback-spring.xml     ← Configuración logging
    │       ├── data.sql              ← Datos iniciales
    │       ├── schema.sql            ← Schema inicial
    │       │
    │       ├── static/               ← Recursos estáticos
    │       │   ├── css/
    │       │   ├── js/
    │       │   └── images/
    │       │
    │       ├── templates/            ← Templates (si usa Thymeleaf)
    │       │   ├── cotizacion/
    │       │   └── error/
    │       │
    │       └── i18n/                 ← Internacionalización
    │           ├── messages.properties
    │           ├── messages_en.properties
    │           └── messages_es.properties
    │
    └── test/
        ├── java/com/arka/cotizador/
        │   ├── 🧪 domain/            ← Tests de dominio (unitarios)
        │   │   ├── CotizacionTest.java
        │   │   ├── PriceCalculatorTest.java
        │   │   └── CotizacionValidatorTest.java
        │   │
        │   ├── 🧪 application/       ← Tests de aplicación (integración)
        │   │   ├── CotizacionApplicationServiceTest.java
        │   │   └── ClienteApplicationServiceTest.java
        │   │
        │   ├── 🧪 infrastructure/    ← Tests de infraestructura
        │   │   ├── adapter/
        │   │   │   ├── in/
        │   │   │   │   └── web/
        │   │   │   │       └── CotizacionControllerTest.java
        │   │   │   └── out/
        │   │   │       └── persistence/
        │   │   │           └── CotizacionPersistenceAdapterTest.java
        │   │   └── config/
        │   │       └── TestConfiguration.java
        │   │
        │   ├── 🧪 integration/       ← Tests de integración completa
        │   │   ├── CotizacionIntegrationTest.java
        │   │   └── ContractTest.java
        │   │
        │   └── 🧪 architecture/      ← Tests de arquitectura
        │       ├── HexagonalArchitectureTest.java
        │       ├── DependencyRuleTest.java
        │       └── LayerArchitectureTest.java
        │
        └── resources/
            ├── application-test.yml  ← Config para tests
            ├── testdata/            ← Datos de prueba
            │   ├── cotizaciones.json
            │   └── clientes.json
            └── contracts/           ← Contract Testing
                ├── cotizacion-contract.json
                └── cliente-contract.json
```

---

## 📋 **EXPLICACIÓN DE LA ESTRATEGIA**

### 🎯 **1. SEPARACIÓN POR RESPONSABILIDAD (No por tipo técnico)**

#### ✅ **Ventajas de nuestra estructura:**

```
🟡 DOMAIN/
├── Todo lo relacionado con REGLAS DE NEGOCIO
├── Sin dependencias externas
├── Fácil de testear unitariamente
└── Independiente de frameworks

🟢 APPLICATION/
├── Todo lo relacionado con CASOS DE USO
├── Orquestación sin lógica de negocio
├── Coordinación entre servicios
└── Manejo de transacciones

🔵 INFRASTRUCTURE/
├── Todo lo relacionado con TECNOLOGÍA
├── Detalles de implementación
├── Frameworks y librerías externas
└── Adaptadores intercambiables
```

#### ❌ **Problemas de la estructura tradicional:**

```
controllers/     ← Mezclaba web con lógica
├── CotizacionController.java
├── ClienteController.java  ← ¿Dónde están las reglas?
└── ProductoController.java

services/        ← Mezclaba aplicación con dominio
├── CotizacionService.java  ← ¿Lógica de negocio o coordinación?
└── ClienteService.java

repositories/    ← Solo tecnología
├── CotizacionRepository.java
└── ClienteRepository.java  ← ¿Y las reglas del dominio?
```

### 🎯 **2. INVERSIÓN DE DEPENDENCIAS**

```
📁 Estructura que facilita DI:

infrastructure/adapter/out/persistence/
├── CotizacionPersistenceAdapter.java  ← Implementa
│                                        ↑
domain/port/out/                         │
├── CotizacionRepositoryPort.java       ← Interface (Puerto)
                                         ↑
application/usecase/                     │
├── CotizacionApplicationService.java   ← Usa (Inyectado)
```

### 🎯 **3. COHESIÓN ALTA POR DOMINIO**

```
🧮 COTIZADOR: Todo lo relacionado con cotizaciones
├── domain/model/Cotizacion.java
├── application/usecase/CotizacionApplicationService.java
├── infrastructure/adapter/in/web/CotizacionController.java
└── infrastructure/adapter/out/persistence/CotizacionPersistenceAdapter.java

📋 SOLICITUDES: Todo lo relacionado con solicitudes
├── domain/model/Solicitud.java
├── application/usecase/SolicitudApplicationService.java
├── infrastructure/adapter/in/web/SolicitudController.java
└── infrastructure/adapter/out/persistence/SolicitudPersistenceAdapter.java
```

### 🎯 **4. TESTING INDEPENDIENTE**

```
📁 Tests organizados por capas:

test/domain/         ← Tests unitarios puros (sin Spring)
├── CotizacionTest.java
├── PriceCalculatorTest.java
└── ClienteTest.java

test/application/    ← Tests con mocks de puertos
├── CotizacionApplicationServiceTest.java
└── ClienteApplicationServiceTest.java

test/infrastructure/ ← Tests de integración (con Spring)
├── CotizacionControllerTest.java
└── CotizacionPersistenceAdapterTest.java

test/integration/    ← Tests end-to-end
└── CotizacionIntegrationTest.java
```

---

## 🔄 **FLUJO DE DESARROLLO CON ESTA ESTRUCTURA**

### 📝 **Ejemplo: Agregar nueva funcionalidad "Descuentos VIP"**

#### **Paso 1: Dominio** 🟡
```java
// 1. Agregar al modelo de dominio
domain/model/Cliente.java
+ public boolean esClienteVip() { ... }

domain/model/Cotizacion.java
+ public void aplicarDescuentoVip() { ... }

domain/service/DescuentoVipDomainService.java  ← Nuevo
+ public BigDecimal calcularDescuentoVip(Cliente cliente) { ... }
```

#### **Paso 2: Aplicación** 🟢
```java
// 2. Agregar caso de uso
application/usecase/CotizacionApplicationService.java
+ public Mono<Cotizacion> aplicarDescuentoVip(CotizacionId id) { ... }

application/command/AplicarDescuentoVipCommand.java  ← Nuevo
+ CotizacionId cotizacionId;
+ ClienteId clienteId;
```

#### **Paso 3: Infraestructura** 🔵
```java
// 3. Agregar endpoint REST
infrastructure/adapter/in/web/CotizacionController.java
+ @PostMapping("/{id}/descuento-vip")
+ public Mono<ResponseEntity<CotizacionDTO>> aplicarDescuentoVip(...) { ... }

// 4. Agregar DTO si es necesario
infrastructure/adapter/in/web/dto/AplicarDescuentoVipRequest.java  ← Nuevo
+ CotizacionId cotizacionId;

// 5. Actualizar mapper si es necesario
infrastructure/adapter/in/web/mapper/CotizacionWebMapper.java
+ public AplicarDescuentoVipCommand toCommand(AplicarDescuentoVipRequest request) { ... }
```

### 🧪 **Paso 4: Tests**
```java
// Tests en orden de desarrollo
test/domain/CotizacionTest.java
+ void deberia_aplicar_descuento_vip_correctamente()

test/application/CotizacionApplicationServiceTest.java
+ void deberia_aplicar_descuento_vip_y_persistir()

test/infrastructure/CotizacionControllerTest.java
+ void POST_descuento_vip_deberia_retornar_200()
```

---

## 🎯 **BENEFICIOS DE ESTA ORGANIZACIÓN**

### ✅ **1. Navegación Intuitiva**
```
📁 Buscar funcionalidad de cotizaciones:
├── domain/model/Cotizacion.java           ← Reglas de negocio
├── application/usecase/CotizacionApplicationService.java  ← Casos de uso
├── infrastructure/adapter/in/web/CotizacionController.java ← API REST
└── infrastructure/adapter/out/persistence/CotizacionPersistenceAdapter.java ← BD

🎯 Todo relacionado está junto, organizando por FUNCIÓN no por TECNOLOGÍA
```

### ✅ **2. Facilita Refactoring**
```
🔄 Cambiar de MySQL a MongoDB:
├── Cambiar: infrastructure/adapter/out/persistence/
├── Sin tocar: domain/ (reglas de negocio)
├── Sin tocar: application/ (casos de uso)
└── Sin tocar: infrastructure/adapter/in/ (controllers)

🔄 Cambiar de REST a GraphQL:
├── Cambiar: infrastructure/adapter/in/web/
├── Sin tocar: domain/ (reglas de negocio)
├── Sin tocar: application/ (casos de uso)
└── Sin tocar: infrastructure/adapter/out/ (persistencia)
```

### ✅ **3. Testing Efectivo**
```
🧪 Tests unitarios (domain):
├── Sin Spring Context ⚡ Rápidos
├── Sin base de datos ⚡ Aislados
└── Solo lógica de negocio ⚡ Focalizados

🧪 Tests de integración (infrastructure):
├── Con Spring Context 🔧 Realistas
├── Con base de datos 🔧 Completos
└── End-to-end 🔧 Confiables
```

### ✅ **4. Onboarding de Nuevos Desarrolladores**
```
📚 Estructura predecible:
├── domain/ ← "¿Qué hace el sistema?" (Lógica de negocio)
├── application/ ← "¿Cómo se coordina?" (Casos de uso)
└── infrastructure/ ← "¿Con qué tecnologías?" (Detalles técnicos)

🎯 Desarrollador nuevo:
1. Lee domain/ para entender el negocio
2. Lee application/ para entender los casos de uso
3. Ve infrastructure/ para entender la implementación
```

---

## 🚨 **ANTIPATRONES EVITADOS**

### ❌ **Organización por Tipo Técnico**
```
❌ MAL:
├── controllers/    ← ¿Qué hacen? ¿Dónde están las reglas?
├── services/       ← ¿Aplicación o dominio?
├── repositories/   ← Solo tecnología
└── entities/       ← ¿JPA o dominio?

✅ BIEN:
├── domain/         ← Reglas de negocio claras
├── application/    ← Casos de uso explicitos
└── infrastructure/ ← Tecnología separada
```

### ❌ **Mezclar Responsabilidades**
```
❌ MAL:
CotizacionService.java  ← ¿Qué hace?
├── Lógica de negocio ❌
├── Persistencia ❌
├── Validación ❌
├── Notificación ❌
└── Serialización ❌

✅ BIEN:
├── domain/model/Cotizacion.java                    ← Solo negocio
├── application/usecase/CotizacionApplicationService.java ← Solo coordinación
├── infrastructure/adapter/out/persistence/CotizacionPersistenceAdapter.java ← Solo BD
└── infrastructure/adapter/out/notification/EmailNotificationAdapter.java ← Solo email
```

### ❌ **Dependencias Incorrectas**
```
❌ MAL:
domain/Cotizacion.java
├── import org.springframework...  ❌
├── import javax.persistence...    ❌
└── import com.fasterxml...       ❌

✅ BIEN:
domain/Cotizacion.java
├── import java.time.LocalDateTime ✅
├── import java.math.BigDecimal    ✅
└── Solo clases del propio dominio ✅
```

---

## 🎯 **GUIDELINES PARA NUEVOS DESARROLLADORES**

### 📋 **Reglas de Ubicación**

#### 🟡 **¿Cuándo va en DOMAIN?**
- ✅ Reglas de negocio puras
- ✅ Entidades y Value Objects
- ✅ Validaciones de dominio
- ✅ Cálculos de negocio
- ❌ Nada de frameworks
- ❌ Nada de persistencia
- ❌ Nada de presentación

#### 🟢 **¿Cuándo va en APPLICATION?**
- ✅ Casos de uso y orquestación
- ✅ Transacciones
- ✅ Coordinación entre servicios
- ✅ Manejo de eventos
- ❌ Reglas de negocio complejas
- ❌ Detalles de persistencia
- ❌ Detalles de presentación

#### 🔵 **¿Cuándo va en INFRASTRUCTURE?**
- ✅ Controllers y DTOs
- ✅ JPA Entities y Repositories
- ✅ Configuración de Spring
- ✅ APIs externas
- ✅ Notificaciones
- ✅ Caché
- ❌ Lógica de negocio

### 📋 **Convenciones de Nombres**

```
📁 Archivos por tipo:
├── *UseCase.java          ← Interfaces de casos de uso (domain/port/in)
├── *RepositoryPort.java   ← Interfaces de repositorio (domain/port/out)
├── *ApplicationService.java ← Implementación casos de uso (application)
├── *Controller.java       ← REST Controllers (infrastructure/in/web)
├── *PersistenceAdapter.java ← Implementación repositorio (infrastructure/out)
├── *Entity.java          ← JPA Entities (infrastructure/out/persistence/entity)
├── *DTO.java            ← Data Transfer Objects (infrastructure/in/web/dto)
├── *Mapper.java         ← Mappers entre capas (infrastructure/.../mapper)
└── *Configuration.java   ← Configuración Spring (infrastructure/config)
```

---

## 🏆 **RESULTADO FINAL**

### ✅ **Arquitectura Lograda**
- 🎯 **Clara separación de responsabilidades**
- 🔌 **Puertos y adaptadores bien definidos**
- ⬆️ **Inversión de dependencias correcta**
- 🧪 **Testing independiente por capas**
- 🔄 **Facilidad para cambios tecnológicos**
- 📖 **Código autoexplicativo y navegable**

### ✅ **Principios SOLID Cumplidos**
- **S** - Single Responsibility: Cada clase una responsabilidad
- **O** - Open/Closed: Abierto para extensión, cerrado para modificación
- **L** - Liskov Substitution: Adaptadores intercambiables
- **I** - Interface Segregation: Puertos específicos y pequeños
- **D** - Dependency Inversion: Dependencias hacia abstracciones

### ✅ **Facilita Evolución**
```
🔄 Nuevos requerimientos:
├── ¿Nueva regla de negocio? → domain/
├── ¿Nuevo caso de uso? → application/
├── ¿Nueva API externa? → infrastructure/adapter/out/
├── ¿Nuevo endpoint? → infrastructure/adapter/in/
└── ¿Nueva tecnología? → Intercambiar adaptador
```

---

*Documentación de Estructura de Carpetas*  
*Proyecto: Arka Valenzuela*  
*Estrategia: Arquitectura Hexagonal + DDD*  
*Fecha: 8 de Septiembre de 2025*
