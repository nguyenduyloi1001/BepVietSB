package com.example.Bep_Viet.repository;

import com.example.Bep_Viet.enums.OtpType;
import com.example.Bep_Viet.model.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByEmailAndTypeAndUsedFalseOrderByExpiresAtDesc(String email, OtpType type);
    void deleteByEmailAndType(String email, OtpType type);
}
