package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class IngredientResponse {
    private Long id;
    private String name;
    private String slug;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;
}
