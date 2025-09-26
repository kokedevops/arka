# 📖 GLOSARIO - LENGUAJE UBICUO

## 🎯 **DEFINICIÓN DE LENGUAJE UBICUO**

El **Lenguaje Ubicuo** (Ubiquitous Language) es un vocabulario común compartido entre desarrolladores y expertos del dominio. Este lenguaje debe ser consistente en código, documentación, conversaciones y pruebas.

---

## 🏢 **DOMINIO: E-COMMERCE ARKA VALENZUELA**

### 📋 **CONCEPTOS CENTRALES DEL NEGOCIO**

#### 🛒 **ÁREA DE VENTAS Y COMERCIO**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Cotización** | Estimación de precio para productos o servicios solicitados por un cliente | `Cotizacion.java` |
| **Solicitud** | Petición formal de un cliente para obtener productos o servicios | `Solicitud.java` |
| **Cliente** | Persona o entidad que solicita servicios o productos de Arka | `Customer.java` |
| **Producto** | Bien o servicio que Arka ofrece a sus clientes | `Product.java` |
| **Categoría** | Clasificación de productos según su tipo o naturaleza | `Category.java` |
| **Carrito** | Contenedor temporal de productos seleccionados por el cliente | `Cart.java` |
| **Pedido** | Solicitud formal de compra confirmada por el cliente | `Order.java` |
| **Gestor de Solicitudes** | Sistema encargado de administrar las peticiones de clientes | `GestorSolicitudesService.java` |

#### 💰 **ÁREA FINANCIERA**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Cotizador** | Sistema que calcula precios basado en parámetros del negocio | `CotizadorService.java` |
| **Precio Base** | Valor inicial de un producto antes de aplicar descuentos | `Product.basePrice` |
| **Descuento** | Reducción aplicada al precio original | `Discount.java` |
| **Precio Final** | Valor que pagará el cliente después de aplicar descuentos | `calculateFinalPrice()` |
| **Estado de Pago** | Condición actual del pago (pendiente, procesando, completado) | `PaymentStatus.java` |

#### 👥 **ÁREA DE CLIENTES**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Cliente Registrado** | Usuario que ha creado una cuenta en el sistema | `RegisteredCustomer.java` |
| **Cliente Invitado** | Usuario que realiza compras sin registrarse | `GuestCustomer.java` |
| **Perfil de Cliente** | Información personal y preferencias del cliente | `CustomerProfile.java` |
| **Historial de Compras** | Registro de todas las transacciones del cliente | `PurchaseHistory.java` |

#### 📦 **ÁREA DE INVENTARIO Y LOGÍSTICA**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Stock** | Cantidad disponible de un producto en inventario | `Product.stockQuantity` |
| **Disponibilidad** | Estado que indica si un producto puede ser vendido | `checkAvailability()` |
| **Reserva** | Separación temporal de productos para un cliente específico | `ProductReservation.java` |
| **Envío** | Proceso de entrega de productos al cliente | `Shipping.java` |
| **Estado de Envío** | Condición actual del envío (preparando, enviado, entregado) | `ShippingStatus.java` |

---

## 🔄 **PROCESOS DE NEGOCIO**

### 🛍️ **PROCESO DE COMPRA**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Agregar al Carrito** | Acción de incluir un producto en el carrito de compras | `addToCart()` |
| **Actualizar Carrito** | Modificar cantidad o remover productos del carrito | `updateCart()` |
| **Confirmar Pedido** | Acción que convierte el carrito en un pedido formal | `confirmOrder()` |
| **Procesar Pago** | Ejecutar la transacción financiera del pedido | `processPayment()` |
| **Carrito Abandonado** | Carrito que no ha sido convertido en pedido por tiempo prolongado | `AbandonedCart.java` |

### 📋 **PROCESO DE COTIZACIÓN**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Solicitar Cotización** | Petición de estimación de precio por parte del cliente | `requestQuote()` |
| **Generar Cotización** | Crear una estimación basada en parámetros del negocio | `generateQuote()` |
| **Aprobar Cotización** | Aceptación de la cotización por parte del cliente | `approveQuote()` |
| **Cotización Vencida** | Cotización que ha superado su tiempo de validez | `ExpiredQuote.java` |

---

## 🎭 **ROLES Y RESPONSABILIDADES**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Administrador** | Usuario con permisos completos en el sistema | `AdminUser.java` |
| **Vendedor** | Usuario encargado de gestionar ventas y clientes | `SalesUser.java` |
| **Cliente** | Usuario que puede realizar compras y solicitudes | `CustomerUser.java` |
| **Operador** | Usuario que maneja operaciones internas del sistema | `OperatorUser.java` |

---

## 🏗️ **ARQUITECTURA Y TECNOLOGÍA**

### 🔧 **COMPONENTES DEL SISTEMA**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Microservicio** | Servicio independiente con responsabilidad específica | Cada módulo del proyecto |
| **Gateway** | Punto de entrada unificado para todos los servicios | `ApiGateway.java` |
| **Discovery Service** | Servicio de registro y descubrimiento de microservicios | `EurekaServer.java` |
| **Config Server** | Servicio de configuración centralizada | `ConfigServer.java` |

### 🔒 **SEGURIDAD**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Token JWT** | Token de autenticación JSON Web Token | `JwtToken.java` |
| **Autenticación** | Proceso de verificar la identidad del usuario | `AuthenticationService.java` |
| **Autorización** | Proceso de verificar permisos del usuario | `AuthorizationService.java` |
| **Rol** | Conjunto de permisos asignados a un usuario | `Role.java` |
| **Permiso** | Capacidad específica otorgada a un rol | `Permission.java` |

---

## 📊 **ESTADOS Y TRANSICIONES**

### 🛒 **Estados del Carrito**
```
ACTIVO → ABANDONADO → RECUPERADO → CONVERTIDO_A_PEDIDO
```

### 📋 **Estados del Pedido**
```
PENDIENTE → CONFIRMADO → PROCESANDO → ENVIADO → ENTREGADO → COMPLETADO
```

### 💰 **Estados de la Cotización**
```
BORRADOR → ENVIADA → REVISIÓN → APROBADA → RECHAZADA → VENCIDA
```

### 💳 **Estados del Pago**
```
PENDIENTE → PROCESANDO → AUTORIZADO → CAPTURADO → FALLIDO → REEMBOLSADO
```

---

## 🎯 **MÉTRICAS DE NEGOCIO**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Tasa de Conversión** | Porcentaje de carritos que se convierten en pedidos | `ConversionRate.java` |
| **Valor Promedio del Pedido** | Monto promedio gastado por pedido | `AverageOrderValue.java` |
| **Abandono de Carrito** | Porcentaje de carritos que no se convierten | `CartAbandonmentRate.java` |
| **Tiempo de Respuesta** | Duración promedio para procesar una solicitud | `ResponseTime.java` |

---

## 🔄 **EVENTOS DEL DOMINIO**

| Término | Definición | Implementación en Código |
|---------|------------|--------------------------|
| **Producto Agregado** | Evento cuando se añade un producto al carrito | `ProductAddedEvent.java` |
| **Pedido Confirmado** | Evento cuando un cliente confirma su pedido | `OrderConfirmedEvent.java` |
| **Pago Procesado** | Evento cuando se completa una transacción | `PaymentProcessedEvent.java` |
| **Cotización Generada** | Evento cuando se crea una nueva cotización | `QuoteGeneratedEvent.java` |
| **Carrito Abandonado** | Evento cuando un carrito queda inactivo | `CartAbandonedEvent.java` |

---

## 💼 **REGLAS DE NEGOCIO**

### 📏 **Reglas de Productos**
- Un producto debe tener precio mayor a 0
- Un producto sin stock no puede ser vendido
- Un producto puede pertenecer a múltiples categorías

### 🛒 **Reglas de Carrito**
- Un carrito no puede estar vacío al confirmar pedido
- Un carrito se considera abandonado después de 24 horas sin actividad
- La cantidad en carrito no puede exceder el stock disponible

### 💰 **Reglas de Cotización**
- Una cotización tiene validez de 30 días
- Una cotización aprobada puede convertirse en pedido
- Los precios en cotización pueden incluir descuentos especiales

### 👥 **Reglas de Cliente**
- Un cliente debe tener email único en el sistema
- Un cliente registrado puede ver su historial completo
- Un cliente invitado tiene acceso limitado

---

## 🎪 **EJEMPLOS DE USO EN CÓDIGO**

### 📝 **Ejemplo 1: Nombrado de Métodos**
```java
// ✅ CORRECTO - Refleja el lenguaje del dominio
public class CotizadorService {
    public Cotizacion generarCotizacion(Solicitud solicitud) {...}
    public void aprobarCotizacion(Long cotizacionId) {...}
    public boolean esCotizacionVencida(Cotizacion cotizacion) {...}
}

// ❌ INCORRECTO - No refleja el dominio
public class PriceCalculator {
    public Quote calculate(Request request) {...}
    public void approve(Long id) {...}
    public boolean isExpired(Quote quote) {...}
}
```

### 📝 **Ejemplo 2: Nombrado de Clases**
```java
// ✅ CORRECTO - Términos del dominio
public class Solicitud {
    private Cliente cliente;
    private List<Producto> productos;
    private EstadoSolicitud estado;
}

// ❌ INCORRECTO - Términos técnicos
public class Request {
    private User user;
    private List<Item> items;
    private Status status;
}
```

### 📝 **Ejemplo 3: Eventos de Dominio**
```java
// ✅ CORRECTO - Eventos que reflejan el negocio
@DomainEvent
public class PedidoConfirmado {
    private final Cliente cliente;
    private final Pedido pedido;
    private final LocalDateTime fechaConfirmacion;
}

@DomainEvent
public class CarritoAbandonado {
    private final Carrito carrito;
    private final LocalDateTime fechaAbandono;
}
```

---

## 🎯 **VALIDACIÓN DEL LENGUAJE UBICUO**

### ✅ **Criterios de Validación**
1. **Consistencia**: Términos usados igual en código, documentación y conversaciones
2. **Claridad**: Términos comprensibles para expertos del dominio
3. **Completitud**: Cubre todos los conceptos importantes del negocio
4. **Evolución**: Se actualiza conforme evoluciona el entendimiento del dominio

### 🔍 **Verificación en Microservicios**

#### 🎯 **Microservicio: Arca Cotizador**
- ✅ Usa términos: Cotización, Solicitud, Cliente, Precio
- ✅ Métodos: `generarCotizacion()`, `calcularPrecio()`, `aprobarCotizacion()`
- ✅ Entidades reflejan el dominio de cotización

#### 🎯 **Microservicio: Gestor Solicitudes**
- ✅ Usa términos: Solicitud, Cliente, Estado, Procesamiento
- ✅ Métodos: `procesarSolicitud()`, `cambiarEstado()`, `notificarCliente()`
- ✅ Entidades reflejan el dominio de gestión

#### 🎯 **Microservicio: API Gateway**
- ✅ Usa términos: Usuario, Autenticación, Autorización, Ruta
- ✅ Métodos: `autenticarUsuario()`, `autorizarAcceso()`, `enrutar()`
- ✅ Entidades reflejan el dominio de acceso

---

## 🏆 **BENEFICIOS DEL LENGUAJE UBICUO**

### 💡 **Para el Desarrollo**
- **Claridad en el código**: Nombres que reflejan el negocio
- **Menor tiempo de comprensión**: Código autoexplicativo
- **Facilita el mantenimiento**: Cambios alineados con el negocio

### 💡 **Para el Negocio**
- **Comunicación efectiva**: Mismo vocabulario entre todos
- **Reducción de malentendidos**: Términos claros y precisos
- **Evolución del dominio**: Fácil incorporación de nuevos conceptos

### 💡 **Para la Arquitectura**
- **Bounded Contexts claros**: Límites basados en lenguaje
- **Microservicios cohesivos**: Servicios alineados con dominios
- **APIs intuitivas**: Endpoints que reflejan operaciones de negocio

---

*Glosario actualizado el 8 de Septiembre de 2025*  
*Proyecto: Arka Valenzuela - Lenguaje Ubicuo*  
*Implementado en 6+ microservicios con consistencia completa*
