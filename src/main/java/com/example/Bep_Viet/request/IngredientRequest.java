package com.example.Bep_Viet.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IngredientRequest {
    @NotBlank(message = "Tên nguyên liệu không được để trống")
    @Size(max = 100,message = "Tên nguyên liệu không được quá 100 ký tự")
    private String name;
    @Size(max = 500,message = "URL ảnh không được quá 500 ký tự")
    private String imageUrl;
    private Long categoryId;
}
