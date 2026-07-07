package com.example.Bep_Viet.request;

import com.example.Bep_Viet.enums.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Tên Category không được để trống")
    @Size(max = 100,message = "Tên Category không được quá 100 ký tự")
    private String name;

    @Size(max = 255,message = "Mô tả không quá 255 ký tự")
    private String description;

    @Size(max= 500,message = "Url ảnh không quá 500 ký tự")
    private String imageUrl;

    @NotNull(message = "Type không được để trống")
    private CategoryType type;
}
