package com.example.Bep_Viet.model;

import com.example.Bep_Viet.enums.PostStatus;
import com.example.Bep_Viet.enums.PostType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false,length = 255)
    private String title;

    @Column(nullable = false,length = 500)
    private String content;

    @Column(length = 500)
    private String thumbnail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType postType;

    @Enumerated(EnumType.STRING)
    private PostStatus postStatus = PostStatus.PENDING;

    @Column(name = "original_recipe_id")
    private Long originalRecipeId;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}