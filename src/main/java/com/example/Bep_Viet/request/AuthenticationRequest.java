package com.example.Bep_Viet.request;

import lombok.Data;

@Data
public class AuthenticationRequest {
    private String email;
    private String password;
}
