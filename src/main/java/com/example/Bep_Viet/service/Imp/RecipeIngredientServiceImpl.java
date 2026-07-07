package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Ingredient;
import com.example.Bep_Viet.model.Recipe;
import com.example.Bep_Viet.model.RecipeIngredient;
import com.example.Bep_Viet.repository.IngredientRepository;
import com.example.Bep_Viet.repository.RecipeIngredientRepository;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.request.RecipeIngredientRequest;
import com.example.Bep_Viet.response.RecipeIngredientResponse;
import com.example.Bep_Viet.service.RecipeIngredientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RecipeIngredientServiceImpl implements RecipeIngredientService {
    private final RecipeIngredientRepository repository;
    private final IngredientRepository ingredientRepository;
    private final RecipeRepository recipeRepository;
    @Override
    public void addAll(Recipe recipe, List<RecipeIngredientRequest> ingredients) {
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredientRepository.findById(ingredients.get(i).getIngredientId())
                    .orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));

            repository.save(RecipeIngredient.builder()
                    .recipe(recipe)
                    .ingredient(ingredient)
                    .quantity(ingredients.get(i).getQuantity())
                    .unit(ingredients.get(i).getUnit())
                    .note(ingredients.get(i).getNote())
                    .build());
        }
    }

    @Override
    public RecipeIngredientResponse add(Long recipeId, RecipeIngredientRequest request) {
        Recipe recipe = recipeRepository.findById(recipeId).orElseThrow(() -> new AppException(ErrorCode.RECIPE_NOT_FOUND));

        Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                .orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));

        if (repository.existsByRecipeIdAndIngredientId(recipeId, request.getIngredientId())) {
            throw new AppException(ErrorCode.INGREDIENT_ALREADY_IN_RECIPE);
        }

        RecipeIngredient ri = RecipeIngredient.builder()
                .recipe(recipe)
                .ingredient(ingredient)
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .note(request.getNote())
                .build();

        return mapToResponse(repository.save(ri));
    }

    @Override
    public RecipeIngredientResponse update(Long id, RecipeIngredientRequest request) {
        RecipeIngredient ri = findById(id);

        if (request.getIngredientId() != null) {
            Ingredient ingredient = ingredientRepository.findById(request.getIngredientId())
                    .orElseThrow(() -> new AppException(ErrorCode.INGREDIENT_NOT_FOUND));
            ri.setIngredient(ingredient);
        }
        if (request.getQuantity() != null) ri.setQuantity(request.getQuantity());
        if (request.getUnit() != null) ri.setUnit(request.getUnit());
        if (request.getNote() != null) ri.setNote(request.getNote());

        return mapToResponse(repository.save(ri));
    }

    @Override
    public void delete(Long id) {
        repository.delete(findById(id));
    }

    @Override
    public void deleteByRecipeId(Long recipeId) {
        repository.deleteByRecipeId(recipeId);
    }

    //helper
    private RecipeIngredient findById(Long id){
        return repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.RECIPE_INGREDIENT_NOT_FOUND));
    }

    private RecipeIngredientResponse mapToResponse(RecipeIngredient ri) {
        return RecipeIngredientResponse.builder()
                .id(ri.getId())
                .ingredientId(ri.getIngredient().getId())
                .ingredientName(ri.getIngredient().getName())
                .quantity(ri.getQuantity())
                .unit(ri.getUnit())
                .note(ri.getNote())
                .build();
    }
}