package com.example.Bep_Viet.request;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class RecipeRequest {
    @NotBlank(message = "Tên món ăn không được để trống")
    @Size(max = 255,message = "Tên món ăn không được quá 255 ký tự")
    private String name;

    private String description;

    private String imageUrl;

    @Min(value = 1,message = "Thời gian nấu phải lớn hơn 0")
    private int cookingTime;

    @Min(value = 1 ,message = " Số khẩu phần phải lớn hơn 0")
    private int servings;

    private Long dishTypeId;
    private Long regionId;
    private Long occasionId;
    private Long difficultyId;

    @NotNull(message = "Danh sách nguyên liệu không được để trống")
    @NotEmpty(message = "Công thức phải có ít nhất 1 nguyên liệu")
    private List<RecipeIngredientRequest> ingredients;

    @NotNull(message = "Các bước thực hiện không được để trống")
    @NotEmpty(message = "Công thức phải có ít nhất 1 bước")
    private List<RecipeStepRequest> steps;

}
