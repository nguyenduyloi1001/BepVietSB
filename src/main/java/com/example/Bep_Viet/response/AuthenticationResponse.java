package com.example.Bep_Viet.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AuthenticationResponse {
    private String token;
    private boolean authenticated;
    private UserResponse userResponse;
}
