package com.arka.arka.infrastructure.adapter.in.web;

import com.arka.arka.domain.model.Product;
import com.arka.arka.domain.port.in.ProductUseCase;
import com.arka.arka.infrastructure.adapter.in.web.dto.ProductDto;
import com.arka.arka.infrastructure.adapter.in.web.mapper.ProductWebMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/productos")
public class ProductController {

    private final ProductUseCase productUseCase;
    private final ProductWebMapper mapper;

    public ProductController(ProductUseCase productUseCase, ProductWebMapper mapper) {
        this.productUseCase = productUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productUseCase.getAllProducts();
        List<ProductDto> productDtos = products.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        try {
            Product product = productUseCase.getProductById(id);
            return ResponseEntity.ok(mapper.toDto(product));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/categoria/{nombre}")
    public ResponseEntity<List<ProductDto>> getByCategory(@PathVariable String nombre) {
        List<Product> products = productUseCase.getProductsByCategory(nombre);
        List<ProductDto> productDtos = products.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDtos);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@RequestBody ProductDto dto) {
        try {
            Product product = mapper.toDomain(dto);
            Product savedProduct = productUseCase.createProduct(product);
            return ResponseEntity.ok(mapper.toDto(savedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto dto) {
        try {
            Product product = mapper.toDomain(dto);
            Product updatedProduct = productUseCase.updateProduct(id, product);
            return ResponseEntity.ok(mapper.toDto(updatedProduct));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productUseCase.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<ProductDto>> searchProducts(@RequestParam String term) {
        List<Product> products = productUseCase.searchProductsByName(term);
        List<ProductDto> productDtos = products.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/ordenados")
    public ResponseEntity<List<ProductDto>> getAllProductsSorted() {
        List<Product> products = productUseCase.getAllProductsSorted();
        List<ProductDto> productDtos = products.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/rango")
    public ResponseEntity<List<ProductDto>> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        try {
            List<Product> products = productUseCase.getProductsByPriceRange(min, max);
            List<ProductDto> productDtos = products.stream()
                    .map(mapper::toDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(productDtos);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
