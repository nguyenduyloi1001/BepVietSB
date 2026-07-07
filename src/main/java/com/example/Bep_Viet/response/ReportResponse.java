package com.example.Bep_Viet.response;

import com.example.Bep_Viet.enums.ReportStatus;
import com.example.Bep_Viet.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponse {
    private Long id;
    private Long userId;
    private String email;
    private Long targetId;
    private TargetType targetType;
    private String targetSlug;
    private String reason;
    private ReportStatus status;
    private LocalDateTime createdAt;
}