package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecipeStepResponse {
    private Long id;
    private Integer stepNumber;
    private String instruction;
    private String imageUrl;
    private String videoUrl;
    private Integer timerMinutes;
}
