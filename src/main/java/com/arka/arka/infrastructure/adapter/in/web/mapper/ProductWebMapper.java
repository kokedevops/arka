package com.arka.arka.infrastructure.adapter.in.web.mapper;

import com.arka.arka.domain.model.Product;
import com.arka.arka.domain.model.Category;
import com.arka.arka.infrastructure.adapter.in.web.dto.ProductDto;
import org.springframework.stereotype.Component;

@Component
public class ProductWebMapper {

    public ProductDto toDto(Product domain) {
        if (domain == null) {
            return null;
        }
        
        ProductDto dto = new ProductDto();
        dto.setId(domain.getId());
        dto.setNombre(domain.getNombre());
        dto.setDescripcion(domain.getDescripcion());
        dto.setCategoriaId(domain.getCategoria() != null ? domain.getCategoria().getId() : null);
        dto.setMarca(domain.getMarca());
        dto.setPrecioUnitario(domain.getPrecioUnitario());
        dto.setStock(domain.getStock());
        
        return dto;
    }

    public Product toDomain(ProductDto dto) {
        if (dto == null) {
            return null;
        }
        
        Product product = new Product();
        product.setId(dto.getId());
        product.setNombre(dto.getNombre());
        product.setDescripcion(dto.getDescripcion());
        
        if (dto.getCategoriaId() != null) {
            Category category = new Category();
            category.setId(dto.getCategoriaId());
            product.setCategoria(category);
        }
        
        product.setMarca(dto.getMarca());
        product.setPrecioUnitario(dto.getPrecioUnitario());
        product.setStock(dto.getStock());
        
        return product;
    }
}
