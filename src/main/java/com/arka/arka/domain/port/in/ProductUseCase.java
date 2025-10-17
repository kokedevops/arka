package com.arka.arka.domain.port.in;

import com.arka.arka.domain.model.Product;
import java.math.BigDecimal;
import java.util.List;

public interface ProductUseCase {
    List<Product> getAllProducts();
    Product getProductById(Long id);
    Product createProduct(Product product);
    Product updateProduct(Long id, Product product);
    void deleteProduct(Long id);
    List<Product> getProductsByCategory(String categoryName);
    List<Product> searchProductsByName(String name);
    List<Product> getProductsByPriceRange(BigDecimal min, BigDecimal max);
    List<Product> getAllProductsSorted();
}
