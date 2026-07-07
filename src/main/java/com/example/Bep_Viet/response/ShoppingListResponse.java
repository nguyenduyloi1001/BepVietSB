package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ShoppingListResponse {
    private Long id;
    private String name;
    private Long mealPlanId;
    private List<ShoppingListItemResponse> items;
    private LocalDateTime createdAt;
}