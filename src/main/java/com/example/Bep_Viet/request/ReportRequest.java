package com.example.Bep_Viet.request;

import com.example.Bep_Viet.enums.TargetType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    @NotNull(message = "Mục tiêu không được thiếu")
    private Long targetId;

    @NotNull(message = "Hãy chọn đối tượng report")
    private TargetType targetType;

    @NotNull(message = "Nôi dung là bắt buộc")
    private String reason;
}
