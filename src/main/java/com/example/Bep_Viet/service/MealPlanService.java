package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.DayOfWeek;
import com.example.Bep_Viet.enums.MealTime;
import com.example.Bep_Viet.request.MealPlanItemRequest;
import com.example.Bep_Viet.request.MealPlanRequest;
import com.example.Bep_Viet.response.MealPlanResponse;

import java.util.List;

public interface MealPlanService {
    MealPlanResponse create(MealPlanRequest request, Long userId);
    MealPlanResponse getById(Long id, Long currentUserId);
    List<MealPlanResponse> getByUserId(Long userId);
    MealPlanResponse addItem(Long mealPlanId, MealPlanItemRequest request, Long currentUserId);
    MealPlanResponse updateItem(Long mealPlanId, MealPlanItemRequest request, Long currentUserId);
    void removeItem(Long mealPlanId, DayOfWeek dayOfWeek, MealTime mealTime, Long currentUserId);
    void delete(Long id, Long currentUserId);
}
