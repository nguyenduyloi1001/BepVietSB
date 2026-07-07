package com.example.Bep_Viet.request;

import com.example.Bep_Viet.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentRequest {
    @NotBlank(message = "Nội dung không được để trống")
    private String content;

    @NotNull(message = "Target không được để trống")
    private Long targetId;

    @NotNull(message = "Loại target không được để trống")
    private TargetType targetType;

    private Long parentId;
}
