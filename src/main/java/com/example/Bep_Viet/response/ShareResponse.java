package com.example.Bep_Viet.response;

import com.example.Bep_Viet.enums.TargetType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ShareResponse {
    private Long id;
    private Long userId;
    private String userName;
    private String userAvatar;

    private Long targetId;
    private TargetType targetType;

    // preview rút gọn của recipe/post được share
    private String title;      // recipe.name hoặc post.title
    private String imageUrl;   // recipe.imageUrl hoặc post.thumbnail
    private String slug;       // chỉ có khi là RECIPE

    private LocalDateTime createdAt;
}