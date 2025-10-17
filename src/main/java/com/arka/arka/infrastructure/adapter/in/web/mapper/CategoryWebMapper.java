package com.arka.arka.infrastructure.adapter.in.web.mapper;

import com.arka.arka.domain.model.Category;
import com.arka.arka.infrastructure.adapter.in.web.dto.CategoryDto;
import org.springframework.stereotype.Component;

@Component
public class CategoryWebMapper {

    public CategoryDto toDto(Category domain) {
        if (domain == null) {
            return null;
        }
        
        CategoryDto dto = new CategoryDto();
        dto.setId(domain.getId());
        dto.setNombre(domain.getNombre());
        
        return dto;
    }

    public Category toDomain(CategoryDto dto) {
        if (dto == null) {
            return null;
        }
        
        Category category = new Category();
        category.setId(dto.getId());
        category.setNombre(dto.getNombre());
        
        return category;
    }
}
