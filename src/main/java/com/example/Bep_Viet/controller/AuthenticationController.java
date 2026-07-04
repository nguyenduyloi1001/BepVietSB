package com.example.Bep_Viet.controller;

import com.example.Bep_Viet.request.AuthenticationRequest;
import com.example.Bep_Viet.request.OtpVerifyRequest;
import com.example.Bep_Viet.request.ResetPasswordRequest;
import com.example.Bep_Viet.request.UserRequest;
import com.example.Bep_Viet.response.AuthenticationResponse;
import com.example.Bep_Viet.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
    @PostMapping("/register/initiate")
    public ResponseEntity<?> initiateRegister(@RequestBody UserRequest request) {
        authenticationService.initiateRegister(request);
        return ResponseEntity.ok(Map.of("message", "OTP đã được gửi đến " + request.getEmail()));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<?> completeRegister(@RequestBody OtpVerifyRequest request) {
        authenticationService.completeRegister(request);
        return ResponseEntity.ok(Map.of("message", "Đăng ký thành công"));
    }

    // ── FORGOT PASSWORD ───────────────────────────────
    @PostMapping("/forgot-password/initiate")
    public ResponseEntity<?> initiateForgotPassword(@RequestBody Map<String, String> body) {
        authenticationService.initiateForgotPassword(body.get("email"));
        return ResponseEntity.ok(Map.of("message", "Nếu email tồn tại, OTP đã được gửi"));
    }

    @PostMapping("/forgot-password/verify")
    public ResponseEntity<?> verifyForgotOtp(@RequestBody OtpVerifyRequest request) {
        authenticationService.verifyForgotPasswordOtp(request);
        return ResponseEntity.ok(Map.of("message", "OTP hợp lệ"));
    }

    @PostMapping("/forgot-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
    }
}
