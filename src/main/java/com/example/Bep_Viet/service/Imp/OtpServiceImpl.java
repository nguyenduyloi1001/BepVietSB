package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.OtpType;
import com.example.Bep_Viet.model.OtpVerification;
import com.example.Bep_Viet.repository.OtpVerificationRepository;
import com.example.Bep_Viet.service.OtpService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final OtpVerificationRepository otpRepo;
    private final JavaMailSender mailSender;

    private static final int OTP_EXPIRE_MINUTES = 5;

    @Transactional
    public void sendOtp(String email, OtpType type) {
        // Xóa OTP cũ
        otpRepo.deleteByEmailAndType(email, type);

        // Tạo OTP 6 số
        String otp = String.format("%06d", new Random().nextInt(999999));

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

        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Không thể gửi email: " + e.getMessage());
        }
    }
}