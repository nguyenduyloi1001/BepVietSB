package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.CategoryType;
import com.example.Bep_Viet.request.CategoryRequest;
import com.example.Bep_Viet.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse createCategory(CategoryRequest request);
    CategoryResponse getCategoryById(Long id);
    CategoryResponse getCategoryBySlug(String slug);
    List<CategoryResponse> getAllCategory();
    List<CategoryResponse> getAllCategoryByType(CategoryType type);
    List<CategoryResponse> getAllCategoryByStatus(Boolean isActive);
    CategoryResponse updateCategory(Long id, CategoryRequest request);
    CategoryResponse toggleActive(Long id);
    void deleteCategory(Long id);
}
