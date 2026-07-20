package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.RecipeStatus;
import com.example.Bep_Viet.request.RecipeRequest;
import com.example.Bep_Viet.response.RecipeResponse;

import java.util.List;

public interface RecipeService {
    RecipeResponse createRecipe(RecipeRequest request,Long userId);

    RecipeResponse getRecipeById(Long id);

    RecipeResponse getBySlug(String slug);

    List<RecipeResponse> getAllRecipe();

    List<RecipeResponse> search(String keyword,Long dishTypeId,Long regionId,Long difficultyId);

    RecipeResponse updateRecipe(Long id,RecipeRequest request,Long currentUserId);

    void delete(Long id, Long currentUserId, boolean isAdmin);

    RecipeResponse approve(Long id);

    RecipeResponse reject(Long id);

    List<RecipeResponse> getByUserId(Long userId);

    List<RecipeResponse> getByStatus(RecipeStatus status);

    List<RecipeResponse> getAllRecipeSorted();

    // ⭐ MỚI: Gợi ý recipe cá nhân hóa dựa trên user_preferences (favorite_ingredients + diet)
    List<RecipeResponse> getForYou(Long userId);
}