package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.model.RecipeStep;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RecipeStepRepository extends JpaRepository<RecipeStep,Long> {
    List<RecipeStep> findByRecipeIdOrderByStepNumberAsc(Long recipeId);
    // tim recipe va sapxeptang dan
    boolean existsByRecipeIdAndStepNumber(Long recipeId, Integer stepNumber); //  kiem tra id co ton tai hay chua
    void deleteByRecipeId(Long recipeId);
}
