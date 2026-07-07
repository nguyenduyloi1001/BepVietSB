package com.example.Bep_Viet.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RecipeStepRequest {
    @NotNull(message = "Số thứ tự không được để trống")
    private Integer stepNumber;

    @NotBlank(message = "Nội dung không được để trống")
    private String instruction;

    private String imageUrl;
    private String videoUrl;
    private Integer timerMinutes;
}
