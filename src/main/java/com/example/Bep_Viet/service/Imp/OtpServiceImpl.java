package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.OtpType;
import com.example.Bep_Viet.model.OtpVerification;
import com.example.Bep_Viet.repository.OtpVerificationRepository;
import com.example.Bep_Viet.service.OtpService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpVerificationRepository otpRepo;

    @Value("${brevo.api-key:}")
    private String apiKey;

    @Value("${mail.from:}")
    private String fromEmail;

    @Value("${mail.from-name:Bep Viet}")
    private String fromName;

    private static final int OTP_EXPIRE_MINUTES = 5;
    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Transactional
    public void sendOtp(String email, OtpType type) {
        // Xóa OTP cũ
        otpRepo.deleteByEmailAndType(email, type);

        // Tạo OTP 6 số
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        OtpVerification entity = new OtpVerification();
        entity.setEmail(email);
        entity.setOtp(otp);
        entity.setType(type);
        entity.setExpiresAt(LocalDateTime.now().plusMinutes(OTP_EXPIRE_MINUTES));
        entity.setUsed(false);
        otpRepo.save(entity);

        sendEmail(email, otp, type);
    }

    public boolean verifyOtp(String email, String otp, OtpType type) {
        Optional<OtpVerification> opt = otpRepo
                .findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(email, type);

        if (opt.isEmpty()) return false;

        OtpVerification entity = opt.get();

        if (entity.isExpired() || !entity.getOtp().equals(otp)) return false;

        entity.setUsed(true);
        otpRepo.save(entity);
        return true;
    }

    private void sendEmail(String to, String otp, OtpType type) {
        if (apiKey == null || apiKey.isBlank() || fromEmail == null || fromEmail.isBlank()) {
            throw new RuntimeException("Thiếu BREVO_API_KEY hoặc MAIL_FROM");
        }

        String subject = type == OtpType.REGISTER
                ? "[Bếp Việt] Mã xác nhận đăng ký"
                : "[Bếp Việt] Mã xác nhận đặt lại mật khẩu";

        String body = """
            <div style="font-family:sans-serif;max-width:480px;margin:auto;padding:32px;border-radius:12px;border:1px solid #e0e0e0">
              <h2 style="color:#2D6A4F;margin-bottom:8px">Bếp Việt 🍳</h2>
              <p style="color:#555">%s</p>
              <div style="font-size:40px;font-weight:bold;letter-spacing:10px;color:#1B3022;margin:32px 0;text-align:center">
                %s
              </div>
              <p style="color:#888;font-size:13px">Mã có hiệu lực trong <b>5 phút</b>. Không chia sẻ mã này cho ai.</p>
            </div>
            """.formatted(
                type == OtpType.REGISTER
                        ? "Cảm ơn bạn đã đăng ký! Dùng mã dưới để xác nhận tài khoản."
                        : "Dùng mã dưới để đặt lại mật khẩu của bạn.",
                otp
        );

        String json = "{"
                + "\"sender\":{\"name\":\"" + esc(fromName) + "\",\"email\":\"" + esc(fromEmail.trim()) + "\"},"
                + "\"to\":[{\"email\":\"" + esc(to) + "\"}],"
                + "\"subject\":\"" + esc(subject) + "\","
                + "\"htmlContent\":\"" + esc(body) + "\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BREVO_URL))
                    .timeout(Duration.ofSeconds(10))
                    .header("api-key", apiKey.trim())
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> res = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 != 2) {
                log.error("Brevo trả về {}: {}", res.statusCode(), res.body());
                throw new RuntimeException("Không thể gửi email");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Không thể gửi email");
        } catch (java.io.IOException e) {
            log.error("Gửi mail lỗi: {}", e.getMessage());
            throw new RuntimeException("Không thể gửi email");
        }
    }

    // Escape chuỗi để nhúng an toàn vào JSON
    private static String esc(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) sb.append(String.format("\\u%04x", (int) c));
                    else sb.append(c);
                }
            }
        }
        return sb.toString();
    }
}