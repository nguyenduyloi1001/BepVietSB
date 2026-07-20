package com.example.Bep_Viet.response;

import com.example.Bep_Viet.enums.TargetType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;
    private Long targetId;
    private TargetType targetType;
    private Long parentId;
    private String content;
    private List<CommentResponse> replies;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

