package com.arka.arka.infrastructure.adapter.in.web;

import com.arka.arka.domain.model.Category;
import com.arka.arka.domain.port.in.CategoryUseCase;
import com.arka.arka.infrastructure.adapter.in.web.dto.CategoryDto;
import com.arka.arka.infrastructure.adapter.in.web.mapper.CategoryWebMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/categorias")
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final CategoryWebMapper mapper;

    public CategoryController(CategoryUseCase categoryUseCase, CategoryWebMapper mapper) {
        this.categoryUseCase = categoryUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<Category> categories = categoryUseCase.getAllCategories();
        List<CategoryDto> categoryDtos = categories.stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categoryDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryUseCase.getCategoryById(id);
            return ResponseEntity.ok(mapper.toDto(category));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto dto) {
        try {
            Category category = mapper.toDomain(dto);
            Category savedCategory = categoryUseCase.createCategory(category);
            return ResponseEntity.ok(mapper.toDto(savedCategory));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryDto> updateCategory(@PathVariable Long id, @RequestBody CategoryDto dto) {
        try {
            Category category = mapper.toDomain(dto);
            Category updatedCategory = categoryUseCase.updateCategory(id, category);
            return ResponseEntity.ok(mapper.toDto(updatedCategory));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryUseCase.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
