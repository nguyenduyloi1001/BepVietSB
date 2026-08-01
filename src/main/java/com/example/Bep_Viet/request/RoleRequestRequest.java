package com.example.Bep_Viet.request;

import com.example.Bep_Viet.enums.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleRequestRequest {
    @NotNull(message = "Loại vai trò không được để trống")
    private Role roleType;

    @Size(max = 500, message = "Lý do tối đa 500 ký tự")
    private String reason;
}
