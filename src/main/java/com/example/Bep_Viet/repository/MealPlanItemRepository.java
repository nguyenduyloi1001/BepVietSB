package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.DayOfWeek;
import com.example.Bep_Viet.enums.MealTime;
import com.example.Bep_Viet.model.MealPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface MealPlanItemRepository extends JpaRepository<MealPlanItem, Long> {
    boolean existsByMealPlanIdAndDayOfWeekAndMealTime(Long mealPlanId, DayOfWeek dayOfWeek, MealTime mealTime);
    @Transactional  // <-- add
    @Modifying
    void deleteByMealPlanIdAndDayOfWeekAndMealTime(Long mealPlanId, DayOfWeek dayOfWeek, MealTime mealTime);

    Optional<MealPlanItem> findByMealPlanIdAndDayOfWeekAndMealTime(
            Long mealPlanId, DayOfWeek dayOfWeek, MealTime mealTime);


    @Modifying
    @Transactional
    @Query("DELETE FROM MealPlanItem m WHERE m.recipe.id = :recipeId")
    void deleteByRecipeId(Long recipeId);
}
