package com.example.Bep_Viet.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RecipeIngredientRequest {
    @NotNull(message = "Tên nguyên liệu không được để trống")
    private Long ingredientId;

    private BigDecimal quantity;

    private String unit;

    private String note;
}
