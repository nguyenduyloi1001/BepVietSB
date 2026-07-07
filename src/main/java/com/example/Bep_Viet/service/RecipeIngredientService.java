package com.example.Bep_Viet.service;

import com.example.Bep_Viet.model.Recipe;
import com.example.Bep_Viet.request.RecipeIngredientRequest;
import com.example.Bep_Viet.response.RecipeIngredientResponse;

import java.util.List;

public interface RecipeIngredientService {
    void addAll(Recipe recipe, List<RecipeIngredientRequest> recipeIngredientRequests);
    RecipeIngredientResponse add(Long recipeId, RecipeIngredientRequest request);
    RecipeIngredientResponse update(Long id, RecipeIngredientRequest request);
    void delete(Long id);
    void deleteByRecipeId(Long recipeId);


}
