package com.example.Bep_Viet.service;

import com.example.Bep_Viet.enums.OtpType;

public interface OtpService {
    void sendOtp(String email, OtpType type);
    boolean verifyOtp(String email, String otp, OtpType type);
}
