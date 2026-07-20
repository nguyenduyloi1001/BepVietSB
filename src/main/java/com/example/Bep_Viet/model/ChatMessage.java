package com.example.Bep_Viet.model;

import com.example.Bep_Viet.enums.ChatRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRole role;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // Lưu recipe_id nào AI đã nhắc trong tin nhắn này, dạng "12,45,78"
    // để câu hỏi sau (vd "món đầu tiên nấu bao lâu") AI biết đang nói món nào
    @Column(name = "referenced_recipe_ids", columnDefinition = "TEXT")
    private String referencedRecipeIds;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}