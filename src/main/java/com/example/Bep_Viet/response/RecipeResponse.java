package com.example.Bep_Viet.response;

import com.example.Bep_Viet.enums.RecipeStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RecipeResponse {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private int cookingTime;
    private int servings;
    private Integer viewCount;
    private BigDecimal avgRating;
    private String slug;
    private RecipeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Long userId;
    private String userName;
    private String userAvatar;
    private String userRole;
    private Long dishTypeId;
    private String dishTypeName;

    private Long regionId;
    private String regionName;

    private Long occasionId;
    private String occasionName;

    private Long difficultyId;
    private String difficultyName;

    private List<RecipeIngredientResponse> ingredients;

    private List<RecipeStepResponse> steps;
}