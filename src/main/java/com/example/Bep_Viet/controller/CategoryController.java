package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.enums.CategoryType;
import com.example.Bep_Viet.request.CategoryRequest;
import com.example.Bep_Viet.response.CategoryResponse;
import com.example.Bep_Viet.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(categoryService.createCategory(request));
    }

    // GET: /api/categories/{id}
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    // GET: /api/categories/slug/{slug}
    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    // GET: /api/categories
    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategory());
    }

    // GET: /api/categories/type?type=REGION
    @GetMapping("/type")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByType(
            @RequestParam CategoryType type) {
        return ResponseEntity.ok(categoryService.getAllCategoryByType(type));
    }

    // GET: /api/categories/status?isActive=true
    @GetMapping("/status")
    public ResponseEntity<List<CategoryResponse>> getCategoriesByStatus(
            @RequestParam Boolean isActive) {
        return ResponseEntity.ok(categoryService.getAllCategoryByStatus(isActive));
    }

    // PUT: /api/categories/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    // PATCH: /api/categories/{id}/toggle-active
    @PatchMapping("/{id}/toggle-active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CategoryResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.toggleActive(id));
    }

    // DELETE: /api/categories/{id}
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
