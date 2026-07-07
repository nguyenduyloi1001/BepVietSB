package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.OtpType;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.AuthenticationRequest;
import com.example.Bep_Viet.request.OtpVerifyRequest;
import com.example.Bep_Viet.request.ResetPasswordRequest;
import com.example.Bep_Viet.request.UserRequest;
import com.example.Bep_Viet.response.AuthenticationResponse;
import com.example.Bep_Viet.response.UserResponse;
import com.example.Bep_Viet.service.AuthenticationService;
import com.example.Bep_Viet.service.OtpService;
import com.example.Bep_Viet.service.UserService;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationImpl implements AuthenticationService {

    UserRepository userRepository;
    PasswordEncoder passwordEncoder;

    OtpService otpService;
    UserService userService;

    com.github.benmanes.caffeine.cache.Cache<String, UserRequest> pendingRegisterCache =
            com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                    .expireAfterWrite(10, java.util.concurrent.TimeUnit.MINUTES)
                    .maximumSize(500)
                    .build();

    @NonFinal
    @Value("${jwt.signerKey}")
    String signerKey;

    @NonFinal
    @Value("${jwt.expiration}")
    long expiration;

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        UserResponse userResponse = mapToUserResponse(user);

        // 🔥 2. Đút cục userResponse béo tốt vào trong AuthenticationResponse trả về
        return AuthenticationResponse.builder()
                .token(generateToken(user))
                .authenticated(true)
                .userResponse(userResponse) // Nạp thông tin user sang Frontend
                .build();
    }

    @Override
    public void initiateRegister(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_EXISTED);
        }
        pendingRegisterCache.put(request.getEmail(), request);
        otpService.sendOtp(request.getEmail(), OtpType.REGISTER);
    }

    @Override
    public void completeRegister(OtpVerifyRequest request) {
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpType.REGISTER)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
        UserRequest userRequest = pendingRegisterCache.getIfPresent(request.getEmail());
        if (userRequest == null) {
            throw new AppException(ErrorCode.OTP_EXPIRED);
        }
        userService.createUser(userRequest);
        pendingRegisterCache.invalidate(request.getEmail());
    }

    // ── FORGOT PASSWORD ───────────────────────────────

    @Override
    public void initiateForgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) return; // tránh email enumeration
        otpService.sendOtp(email, OtpType.FORGOT_PASSWORD);
    }

    @Override
    public void verifyForgotPasswordOtp(OtpVerifyRequest request) {
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpType.FORGOT_PASSWORD)) {
            throw new AppException(ErrorCode.INVALID_OTP);
        }
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    //helper
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl()) // <-- Gán link ảnh từ Cloudinary ở DB ra đây!
                .bio(user.getBio())
                .location(user.getLocation())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String generateToken(User user) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject(user.getEmail())
                .issuer("BepViet.com")
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(expiration, ChronoUnit.SECONDS).toEpochMilli()))
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .build();

        JWSObject jwsObject = new JWSObject(header, new Payload(claimsSet.toJSONObject()));

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Không thể tạo token", e);
            throw new RuntimeException(e);
        }
    }
}