package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Category;
import com.example.Bep_Viet.model.Ingredient;
import com.example.Bep_Viet.repository.CategoryRepository;
import com.example.Bep_Viet.repository.IngredientRepository;
import com.example.Bep_Viet.request.IngredientRequest;
import com.example.Bep_Viet.response.IngredientResponse;
import com.example.Bep_Viet.service.IngredientService;
import com.example.Bep_Viet.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class IngredientServiceImpl implements IngredientService {
    private final IngredientRepository ingredientRepository;
    private final CategoryRepository categoryRepository;
    @Override
    public IngredientResponse createIngredient(IngredientRequest request) {
        if(ingredientRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.INGREDIENT_NAME_EXISTED);
        }
        String slug = SlugUtil.toSlug(request.getName());
        if(ingredientRepository.existsBySlug(slug)){
            throw new AppException(ErrorCode.INGREDIENT_SLUG_EXISTED);
        }
        Category category=null;
        if(request.getCategoryId()!=null){
            category = categoryRepository.findById(request.getCategoryId()).orElseThrow(()->new AppException(ErrorCode.CATEGORY_NOT_FOUND));
        }
        Ingredient ingredient = Ingredient.builder()
                .name(request.getName())
                .slug(slug)
                .imageUrl(request.getImageUrl())
                .category(category)
                .build();
        return mapToResponse(ingredientRepository.save(ingredient));
    }

    @Override
    public IngredientResponse getIngredientById(Long id) {
        Ingredient ingredient = findIngredientById(id);
        return mapToResponse(ingredient);
    }

    @Override
    public IngredientResponse getIngredientBySlug(String slug) {
        Ingredient ingredient = ingredientRepository.findBySlug(slug).orElseThrow(()-> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
        return mapToResponse(ingredient);
    }

    @Override
    public List<IngredientResponse> getAllIngredient(String keyword, Long categoryId) {
        return ingredientRepository.searchIngredients(keyword,categoryId).stream().map(this::mapToResponse).toList();
    }

    @Override
    public IngredientResponse updateIngredient(Long id, IngredientRequest request) {
        Ingredient ingredient = findIngredientById(id);

        if (request.getName() != null && !request.getName().isBlank()) {
            String newSlug = SlugUtil.toSlug(request.getName());

            if (!ingredient.getName().equalsIgnoreCase(request.getName())
                    && ingredientRepository.existsByName(request.getName())) {
                throw new AppException(ErrorCode.INGREDIENT_NAME_EXISTED);
            }

            if (!ingredient.getSlug().equals(newSlug)
                    && ingredientRepository.existsBySlug(newSlug)) {
                throw new AppException(ErrorCode.INGREDIENT_SLUG_EXISTED);
            }

            ingredient.setName(request.getName());
            ingredient.setSlug(newSlug);
        }

        if (request.getImageUrl() != null) {
            ingredient.setImageUrl(request.getImageUrl());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
            ingredient.setCategory(category);
        }

        return mapToResponse(ingredientRepository.save(ingredient));
    }

    @Override
    public void delete(Long id) {
        ingredientRepository.delete(findIngredientById(id));
    }

    //helper
    private Ingredient findIngredientById(Long id){
        return ingredientRepository.findById(id).orElseThrow(()->new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
    }

    private IngredientResponse mapToResponse(Ingredient ingredient){
        return IngredientResponse.builder()
                .id(ingredient.getId())
                .name(ingredient.getName())
                .slug(ingredient.getSlug())
                .imageUrl(ingredient.getImageUrl())
                .categoryId(ingredient.getCategory() != null ? ingredient.getCategory().getId() : null)
                .categoryName(ingredient.getCategory() != null ? ingredient.getCategory().getName() : null)
                .createdAt(ingredient.getCreatedAt())
                .build();
    }
}