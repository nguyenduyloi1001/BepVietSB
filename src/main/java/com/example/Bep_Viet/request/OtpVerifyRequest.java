package com.example.Bep_Viet.request;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String email;
    private String otp;
}
