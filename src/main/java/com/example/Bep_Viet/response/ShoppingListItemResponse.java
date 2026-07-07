package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ShoppingListItemResponse {
    private Long id;
    private Long ingredientId;
    private String ingredientName;
    private BigDecimal quantity;
    private String unit;
    private Boolean isChecked;
}
