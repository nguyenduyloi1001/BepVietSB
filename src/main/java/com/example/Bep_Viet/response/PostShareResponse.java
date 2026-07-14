package com.example.Bep_Viet.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostShareResponse {
    private Long id;

    // Thông tin người đăng lại
    private Long userId;
    private String username;
    private String userAvatar;

    private String content;
    private LocalDateTime createdAt;

    // Thông tin bài viết gốc (rút gọn)
    private Long originalPostId;
    private String originalTitle;
    private String originalThumbnail;
    private String originalAuthorName;
}