package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.CategoryType;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Category;
import com.example.Bep_Viet.repository.CategoryRepository;
import com.example.Bep_Viet.request.CategoryRequest;
import com.example.Bep_Viet.response.CategoryResponse;
import com.example.Bep_Viet.service.CategoryService;
import com.example.Bep_Viet.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        String slug = SlugUtil.toSlug(request.getName());
        if(categoryRepository.existsBySlug(slug)){
            throw new AppException(ErrorCode.SLUG_EXISTED);
        }
        Category category = Category.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .type(request.getType())
                .build();
        Category savedCategory =categoryRepository.save(category);
        return mapToResponse(savedCategory);
    }

    @Override
    @Transactional
    public CategoryResponse getCategoryById(Long id) {
        return mapToResponse(findCategoryById(id));
    }

    @Override
    @Transactional
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public List<CategoryResponse> getAllCategory() {
        return categoryRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public List<CategoryResponse> getAllCategoryByType(CategoryType type) {
        List<Category> categories = categoryRepository.findByType(type);
        return categories.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public List<CategoryResponse> getAllCategoryByStatus(Boolean isActive) {
        List<Category> categories = categoryRepository.findByIsActive(isActive);
        return categories.stream().map(this::mapToResponse).toList();
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findCategoryById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            // Generate slug mới từ name mới
            String newSlug = SlugUtil.toSlug(request.getName());

            // Chỉ kiểm tra trùng nếu slug thay đổi
            if (!category.getSlug().equals(newSlug) && categoryRepository.existsBySlug(newSlug)) {
                throw new AppException(ErrorCode.SLUG_EXISTED);
            }

            category.setName(request.getName());
            category.setSlug(newSlug);
        }
        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }
        if (request.getImageUrl() != null) {
            category.setImageUrl(request.getImageUrl());
        }
        if (request.getType() != null) {
            category.setType(request.getType());
        }

        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public CategoryResponse toggleActive(Long id) {
        Category category = findCategoryById(id);
        category.setIsActive(!category.getIsActive());
        return mapToResponse(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryById(id);
        categoryRepository.delete(category);
    }

    //helper
    private Category findCategoryById(Long id)
    {
        return categoryRepository.findById(id).orElseThrow(()-> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }
    private CategoryResponse mapToResponse(Category category){
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .type(category.getType())
                .isActive(category.getIsActive())
                .createdAt(category.getCreatedAt())
                .build();
    }
}