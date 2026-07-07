package com.example.Bep_Viet.response;

import com.example.Bep_Viet.enums.CategoryType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String imageUrl;
    private CategoryType type;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
