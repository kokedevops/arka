package com.arka.arka.infrastructure.adapter.in.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.arka.arka.domain.port.in.*;
import com.arka.arka.domain.model.*;
import com.arka.arka.infrastructure.adapter.in.web.mapper.*;
import com.arka.arka.infrastructure.adapter.in.web.dto.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.math.BigDecimal;

/**
 * 🌐 API de Terceros - Servicio de Datos CRUD
 * 
 * Implementa las especificaciones exactas requeridas:
 * - GET /ObtenerDatos/{tabla}: Obtiene todos los registros
 * - GET /ObtenerDatos/{tabla}/{id}: Obtiene un registro específico
 * - POST /GuardarDatos/{tabla}: Guarda un nuevo registro
 * - DELETE /BorrarDatos/{tabla}/{id}: Borra un registro
 */
@RestController
@RequestMapping("/api/terceros")
@CrossOrigin(origins = "*")
public class ThirdPartyApiController {

    @Autowired
    private ProductUseCase productUseCase;
    
    @Autowired
    private CustomerUseCase customerUseCase;
    
    @Autowired
    private OrderUseCase orderUseCase;
    
    @Autowired
    private CartUseCase cartUseCase;
    
    @Autowired
    private CategoryUseCase categoryUseCase;
    
    @Autowired
    private ProductWebMapper productMapper;
    
    @Autowired
    private CustomerWebMapper customerMapper;
    
    @Autowired
    private OrderWebMapper orderMapper;
    
    @Autowired
    private CartWebMapper cartMapper;
    
    @Autowired
    private CategoryWebMapper categoryMapper;

    /**
     * ✅ GET /ObtenerDatos/{tabla}: Obtiene todos los registros de la tabla especificada
     */
    @GetMapping("/ObtenerDatos/{tabla}")
    public ResponseEntity<?> obtenerTodosLosDatos(@PathVariable String tabla) {
        try {
            switch (tabla.toLowerCase()) {
                case "productos":
                    List<Product> products = productUseCase.getAllProducts();
                    List<ProductDto> productDtos = products.stream()
                            .map(productMapper::toDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(createSuccessResponse("productos", productDtos, productDtos.size()));
                    
                case "usuarios":
                case "clientes":
                    List<Customer> customers = customerUseCase.getAllCustomers();
                    List<CustomerDto> customerDtos = customers.stream()
                            .map(customerMapper::toDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(createSuccessResponse("clientes", customerDtos, customerDtos.size()));
                    
                case "pedidos":
                case "ordenes":
                    List<Order> orders = orderUseCase.getAllOrders();
                    List<OrderDto> orderDtos = orders.stream()
                            .map(orderMapper::toDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(createSuccessResponse("pedidos", orderDtos, orderDtos.size()));
                    
                case "carritos":
                    List<Cart> carts = cartUseCase.getAllCarts();
                    List<CartDto> cartDtos = carts.stream()
                            .map(cartMapper::toDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(createSuccessResponse("carritos", cartDtos, cartDtos.size()));
                    
                case "categorias":
                    List<Category> categories = categoryUseCase.getAllCategories();
                    List<CategoryDto> categoryDtos = categories.stream()
                            .map(categoryMapper::toDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(createSuccessResponse("categorias", categoryDtos, categoryDtos.size()));
                    
                default:
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Tabla no válida. Tablas disponibles: productos, usuarios, pedidos, carritos, categorias"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno del servidor: " + e.getMessage()));
        }
    }

    /**
     * ✅ GET /ObtenerDatos/{tabla}/{id}: Obtiene un registro específico de la tabla por su ID
     */
    @GetMapping("/ObtenerDatos/{tabla}/{id}")
    public ResponseEntity<?> obtenerDatoPorId(@PathVariable String tabla, @PathVariable Long id) {
        try {
            switch (tabla.toLowerCase()) {
                case "productos":
                    Product product = productUseCase.getProductById(id);
                    ProductDto productDto = productMapper.toDto(product);
                    return ResponseEntity.ok(createSuccessResponse("producto", productDto, 1));
                    
                case "usuarios":
                case "clientes":
                    Customer customer = customerUseCase.getCustomerById(id);
                    CustomerDto customerDto = customerMapper.toDto(customer);
                    return ResponseEntity.ok(createSuccessResponse("cliente", customerDto, 1));
                    
                case "pedidos":
                case "ordenes":
                    Order order = orderUseCase.getOrderById(id);
                    OrderDto orderDto = orderMapper.toDto(order);
                    return ResponseEntity.ok(createSuccessResponse("pedido", orderDto, 1));
                    
                case "carritos":
                    Cart cart = cartUseCase.getCartById(id);
                    CartDto cartDto = cartMapper.toDto(cart);
                    return ResponseEntity.ok(createSuccessResponse("carrito", cartDto, 1));
                    
                case "categorias":
                    Category category = categoryUseCase.getCategoryById(id);
                    CategoryDto categoryDto = categoryMapper.toDto(category);
                    return ResponseEntity.ok(createSuccessResponse("categoria", categoryDto, 1));
                    
                default:
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Tabla no válida. Tablas disponibles: productos, usuarios, pedidos, carritos, categorias"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Registro no encontrado con ID: " + id + " en tabla: " + tabla));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error interno del servidor: " + e.getMessage()));
        }
    }

    /**
     * ✅ POST /GuardarDatos/{tabla}: Guarda un nuevo registro en la tabla especificada
     */
    @PostMapping("/GuardarDatos/{tabla}")
    public ResponseEntity<?> guardarDatos(@PathVariable String tabla, @RequestBody Map<String, Object> datos) {
        try {
            switch (tabla.toLowerCase()) {
                case "productos":
                    // Crear ProductDto desde el Map
                    ProductDto productDto = mapToProductDto(datos);
                    Product product = productMapper.toDomain(productDto);
                    Product savedProduct = productUseCase.createProduct(product);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(createSuccessResponse("producto_creado", productMapper.toDto(savedProduct), 1));
                    
                case "usuarios":
                case "clientes":
                    CustomerDto customerDto = mapToCustomerDto(datos);
                    Customer customer = customerMapper.toDomain(customerDto);
                    Customer savedCustomer = customerUseCase.createCustomer(customer);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(createSuccessResponse("cliente_creado", customerMapper.toDto(savedCustomer), 1));
                    
                case "pedidos":
                case "ordenes":
                    OrderDto orderDto = mapToOrderDto(datos);
                    Order order = orderMapper.toDomain(orderDto);
                    Order savedOrder = orderUseCase.createOrder(order);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(createSuccessResponse("pedido_creado", orderMapper.toDto(savedOrder), 1));
                    
                case "carritos":
                    CartDto cartDto = mapToCartDto(datos);
                    Cart cart = cartMapper.toDomain(cartDto);
                    Cart savedCart = cartUseCase.createCart(cart);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(createSuccessResponse("carrito_creado", cartMapper.toDto(savedCart), 1));
                    
                case "categorias":
                    CategoryDto categoryDto = mapToCategoryDto(datos);
                    Category category = categoryMapper.toDomain(categoryDto);
                    Category savedCategory = categoryUseCase.createCategory(category);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(createSuccessResponse("categoria_creada", categoryMapper.toDto(savedCategory), 1));
                    
                default:
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Tabla no válida para inserción. Tablas disponibles: productos, usuarios, pedidos, carritos, categorias"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(createErrorResponse("Datos inválidos: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al guardar datos: " + e.getMessage()));
        }
    }

    /**
     * ✅ DELETE /BorrarDatos/{tabla}/{id}: Borra un registro de la tabla especificada por su ID
     */
    @DeleteMapping("/BorrarDatos/{tabla}/{id}")
    public ResponseEntity<?> borrarDatos(@PathVariable String tabla, @PathVariable Long id) {
        try {
            switch (tabla.toLowerCase()) {
                case "productos":
                    productUseCase.deleteProduct(id);
                    return ResponseEntity.ok(createSuccessResponse("producto_eliminado", "ID: " + id, 1));
                    
                case "usuarios":
                case "clientes":
                    customerUseCase.deleteCustomer(id);
                    return ResponseEntity.ok(createSuccessResponse("cliente_eliminado", "ID: " + id, 1));
                    
                case "pedidos":
                case "ordenes":
                    orderUseCase.deleteOrder(id);
                    return ResponseEntity.ok(createSuccessResponse("pedido_eliminado", "ID: " + id, 1));
                    
                case "carritos":
                    cartUseCase.deleteCart(id);
                    return ResponseEntity.ok(createSuccessResponse("carrito_eliminado", "ID: " + id, 1));
                    
                case "categorias":
                    categoryUseCase.deleteCategory(id);
                    return ResponseEntity.ok(createSuccessResponse("categoria_eliminada", "ID: " + id, 1));
                    
                default:
                    return ResponseEntity.badRequest()
                            .body(createErrorResponse("Tabla no válida para eliminación. Tablas disponibles: productos, usuarios, pedidos, carritos, categorias"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("Registro no encontrado con ID: " + id + " en tabla: " + tabla));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error al eliminar registro: " + e.getMessage()));
        }
    }

    /**
     * 📋 Endpoint de información de la API de terceros
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("api_name", "ARKA API de Terceros - Servicio de Datos CRUD");
        info.put("version", "1.0.0");
        info.put("description", "API que simula un servicio de terceros con operaciones CRUD");
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("GET /ObtenerDatos/{tabla}", "Obtiene todos los registros de la tabla");
        endpoints.put("GET /ObtenerDatos/{tabla}/{id}", "Obtiene un registro específico por ID");
        endpoints.put("POST /GuardarDatos/{tabla}", "Guarda un nuevo registro (JSON en body)");
        endpoints.put("DELETE /BorrarDatos/{tabla}/{id}", "Elimina un registro por ID");
        
        info.put("endpoints", endpoints);
        info.put("tablas_disponibles", List.of("productos", "usuarios", "pedidos", "carritos", "categorias"));
        info.put("formato_respuesta", "JSON estándar con success, message, data, count");
        
        return ResponseEntity.ok(info);
    }

    // ===== MÉTODOS HELPER =====

    private Map<String, Object> createSuccessResponse(String operation, Object data, int count) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Operación " + operation + " ejecutada exitosamente");
        response.put("data", data);
        response.put("count", count);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", message);
        response.put("data", null);
        response.put("count", 0);
        response.put("timestamp", System.currentTimeMillis());
        return response;
    }

    // Métodos de mapeo (simplificados para el ejemplo)
    private ProductDto mapToProductDto(Map<String, Object> data) {
        ProductDto dto = new ProductDto();
        if (data.containsKey("nombre")) dto.setNombre((String) data.get("nombre"));
        if (data.containsKey("descripcion")) dto.setDescripcion((String) data.get("descripcion"));
        if (data.containsKey("precio")) {
            BigDecimal precio = BigDecimal.valueOf(((Number) data.get("precio")).doubleValue());
            dto.setPrecioUnitario(precio);
        }
        if (data.containsKey("precioUnitario")) {
            BigDecimal precio = BigDecimal.valueOf(((Number) data.get("precioUnitario")).doubleValue());
            dto.setPrecioUnitario(precio);
        }
        if (data.containsKey("stock")) dto.setStock(((Number) data.get("stock")).intValue());
        return dto;
    }

    private CustomerDto mapToCustomerDto(Map<String, Object> data) {
        CustomerDto dto = new CustomerDto();
        if (data.containsKey("nombre")) dto.setNombre((String) data.get("nombre"));
        if (data.containsKey("email")) dto.setEmail((String) data.get("email"));
        if (data.containsKey("telefono")) dto.setTelefono((String) data.get("telefono"));
        return dto;
    }

    private OrderDto mapToOrderDto(Map<String, Object> data) {
        OrderDto dto = new OrderDto();
        if (data.containsKey("total")) {
            BigDecimal total = BigDecimal.valueOf(((Number) data.get("total")).doubleValue());
            dto.setTotal(total);
        }
        if (data.containsKey("estado")) dto.setEstado((String) data.get("estado"));
        return dto;
    }

    private CartDto mapToCartDto(Map<String, Object> data) {
        CartDto dto = new CartDto();
        if (data.containsKey("estado")) dto.setEstado((String) data.get("estado"));
        return dto;
    }

    private CategoryDto mapToCategoryDto(Map<String, Object> data) {
        CategoryDto dto = new CategoryDto();
        if (data.containsKey("nombre")) dto.setNombre((String) data.get("nombre"));
        // CategoryDto solo tiene campo 'nombre', no 'descripcion'
        return dto;
    }
}
