package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class MealPlanResponse {
    private Long id;
    private Long userId;
    private String name;
    private LocalDate weekStartDate;
    private List<MealPlanItemResponse> items;
    private LocalDateTime createdAt;
}
