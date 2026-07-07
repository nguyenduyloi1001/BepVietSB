package com.example.Bep_Viet.service;

import com.example.Bep_Viet.model.Recipe;
import com.example.Bep_Viet.request.RecipeStepRequest;
import com.example.Bep_Viet.response.RecipeStepResponse;

import java.util.List;

public interface RecipeStepService {
    void addAll(Recipe recipe, List<RecipeStepRequest> requests);
    RecipeStepResponse add(Long recipeId, RecipeStepRequest request);
    RecipeStepResponse update(Long id, RecipeStepRequest request);
    void delete(Long id);
    List<RecipeStepResponse> getByRecipeId(Long recipeId);
    void deleteByRecipeId(Long recipeId);
}
