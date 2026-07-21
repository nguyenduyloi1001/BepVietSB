package com.example.Bep_Viet.request;

import com.example.Bep_Viet.enums.TargetType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareRequest {
    @NotNull(message = "targetId không được để trống")
    private Long targetId;

    @NotNull(message = "targetType không được để trống")
    private TargetType targetType; // RECIPE hoặc POST
}