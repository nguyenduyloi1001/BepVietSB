package com.example.Bep_Viet.response;

import com.example.Bep_Viet.enums.Role;
import com.example.Bep_Viet.enums.RoleRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RoleRequestResponse {
    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private Role roleType;
    private RoleRequestStatus status;
    private String note;
    private String reason;
    private Long reviewedBy;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
}
