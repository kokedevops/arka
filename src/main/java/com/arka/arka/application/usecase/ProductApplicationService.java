package com.arka.arka.application.usecase;

import com.arka.arka.domain.model.Product;
import com.arka.arka.domain.port.in.ProductUseCase;
import com.arka.arka.domain.port.out.ProductRepositoryPort;
import com.arka.arka.domain.port.out.CategoryRepositoryPort;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Application Service for Product Use Cases
 * This class orchestrates the business logic for Product operations
 * Following Hexagonal Architecture - Application Layer
 */
public class ProductApplicationService implements ProductUseCase {
    
    private final ProductRepositoryPort productRepository;
    private final CategoryRepositoryPort categoryRepository;

    public ProductApplicationService(ProductRepositoryPort productRepository, 
                                   CategoryRepositoryPort categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }

    @Override
    public Product createProduct(Product product) {
        validateProduct(product);
        validateCategoryExists(product.getCategoria().getId());
        return productRepository.save(product);
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        validateProduct(product);
        product.setId(id);
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> getProductsByCategory(String categoryName) {
        return productRepository.findByCategoriaNombre(categoryName);
    }

    @Override
    public List<Product> searchProductsByName(String name) {
        return productRepository.findByNombreContainingIgnoreCase(name);
    }

    @Override
    public List<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max) {
        if (min.compareTo(max) > 0) {
            throw new IllegalArgumentException("Min price cannot be greater than max price");
        }
        return productRepository.findByPriceRange(min, max);
    }

    @Override
    public List<Product> getAllProductsSorted() {
        return productRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(Product::getNombre, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Domain validation logic for Product
     */
    private void validateProduct(Product product) {
        if (product.getNombre() == null || product.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (!product.hasValidPrice()) {
            throw new IllegalArgumentException("Product must have a valid price");
        }
        if (product.getCategoria() == null || product.getCategoria().getId() == null) {
            throw new IllegalArgumentException("Product must have a valid category");
        }
    }

    /**
     * Validates that the category exists in the system
     */
    private void validateCategoryExists(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
    }
}
