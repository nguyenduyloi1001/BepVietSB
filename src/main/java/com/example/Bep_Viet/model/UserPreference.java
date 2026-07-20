package com.example.Bep_Viet.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "preference_key"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // vd: "diet", "favorite_ingredients"
    @Column(name = "preference_key", nullable = false, length = 50)
    private String preferenceKey;

    // ⭐ SỬA: đổi từ length=255 sang TEXT, vì favorite_ingredients lưu JSON
    // tích lũy nhiều ingredient theo thời gian, dễ vượt quá 255 ký tự
    @Column(name = "preference_value", nullable = false, columnDefinition = "TEXT")
    private String preferenceValue;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}