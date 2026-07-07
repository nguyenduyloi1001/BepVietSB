package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Builder
public class RatingResponse {
    private Long id;
    private Long userId;
    private String userName;
    private Long recipeId;
    private Integer stars;
    private LocalDateTime createdAt;
}