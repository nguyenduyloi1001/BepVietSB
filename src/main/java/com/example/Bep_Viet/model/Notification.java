package com.example.Bep_Viet.model;


import com.example.Bep_Viet.enums.NotificationTargetType;
import com.example.Bep_Viet.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người nhận thông báo
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Ai tạo ra hành động (người like, follow, comment, ...)
    // null nếu là thông báo hệ thống (approve/reject)
    @Column(name = "actor_id")
    private Long actorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    // ID của đối tượng liên quan (recipe id, post id, comment id, user id)
    @Column(name = "target_id")
    private Long targetId;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private NotificationTargetType targetType;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}