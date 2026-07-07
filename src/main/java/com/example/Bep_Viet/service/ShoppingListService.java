package com.example.Bep_Viet.service;

import com.example.Bep_Viet.response.ShoppingListItemResponse;
import com.example.Bep_Viet.response.ShoppingListResponse;

import java.util.List;

public interface ShoppingListService {
    ShoppingListResponse generateFromMealPlan(Long mealPlanId, Long userId);
    ShoppingListResponse getById(Long id);
    List<ShoppingListResponse> getByUserId(Long userId);
    ShoppingListItemResponse toggleCheck(Long itemId);
    public void delete(Long id, Long userId);
}
