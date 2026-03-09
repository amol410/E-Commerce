package com.ecom.ecommerce.controller;

import com.ecom.ecommerce.dto.CategoryDto;
import com.ecom.ecommerce.entity.Category;
import com.ecom.ecommerce.response.ApiResponse;
import com.ecom.ecommerce.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Category management")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    @Operation(summary = "Get all categories (flat list)")
    public ResponseEntity<ApiResponse<List<Category>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success("Categories fetched", categoryService.getAll()));
    }

    @GetMapping("/tree")
    @Operation(summary = "Get recursive category tree")
    public ResponseEntity<ApiResponse<List<Category>>> getTree() {
        return ResponseEntity.ok(ApiResponse.success("Category tree fetched", categoryService.getCategoryTree()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<Category>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Category fetched", categoryService.getById(id)));
    }

    @PostMapping
    @Operation(summary = "Create category (Admin only)")
    public ResponseEntity<ApiResponse<Category>> create(@Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Category created", categoryService.create(dto)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update category (Admin only)")
    public ResponseEntity<ApiResponse<Category>> update(@PathVariable Long id, @Valid @RequestBody CategoryDto dto) {
        return ResponseEntity.ok(ApiResponse.success("Category updated", categoryService.update(id, dto)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete category (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        categoryService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Category deleted", null));
    }
}
