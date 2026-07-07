package com.example.Bep_Viet.repository;


import com.example.Bep_Viet.model.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient,Long> {
    List<RecipeIngredient> findByRecipeId(Long recipeId);
    void deleteByRecipeId(Long recipeId);
    boolean existsByRecipeIdAndIngredientId(Long recipeId, Long ingredientId);}
