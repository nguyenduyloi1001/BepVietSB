package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.PostStatus;
import com.example.Bep_Viet.enums.RecipeStatus;
import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.*;
import com.example.Bep_Viet.repository.*;
import com.example.Bep_Viet.request.ShareRequest;
import com.example.Bep_Viet.response.ShareResponse;
import com.example.Bep_Viet.service.ShareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShareServiceImpl implements ShareService {

    private final ShareRepository shareRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final PostRepository postRepository;

    @Override
    @Transactional
    public ShareResponse share(ShareRequest request, Long userId) {
        Long targetId = request.getTargetId();
        TargetType targetType = request.getTargetType();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // validate theo từng loại target
        if (targetType == TargetType.RECIPE) {
            Recipe recipe = recipeRepository.findById(targetId)
                    .orElseThrow(() -> new AppException(ErrorCode.RECIPE_NOT_FOUND));
            if (recipe.getStatus() != RecipeStatus.PUBLISHED) {
                throw new AppException(ErrorCode.RECIPE_SHARE_NOT_PUBLISHED);
            }
            if (recipe.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.RECIPE_SHARE_SELF_NOT_ALLOWED);
            }
            if (shareRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)) {
                throw new AppException(ErrorCode.RECIPE_SHARE_ALREADY_EXISTS);
            }
        } else if (targetType == TargetType.POST) {
            Post post = postRepository.findById(targetId)
                    .orElseThrow(() -> new AppException(ErrorCode.POST_NOT_FOUND));
            if (post.getPostStatus() != PostStatus.PUBLISHED) {
                throw new AppException(ErrorCode.POST_SHARE_NOT_PUBLISHED);
            }
            if (post.getUser().getId().equals(userId)) {
                throw new AppException(ErrorCode.POST_SHARE_SELF_NOT_ALLOWED);
            }
            if (shareRepository.existsByUserIdAndTargetIdAndTargetType(userId, targetId, targetType)) {
                throw new AppException(ErrorCode.POST_SHARE_ALREADY_EXISTS);
            }
        } else {
            throw new AppException(ErrorCode.FORBIDDEN);
        }

        Share share = Share.builder()
                .user(user)
                .targetId(targetId)
                .targetType(targetType)
                .build();

        return mapToResponse(shareRepository.save(share));
    }

    @Override
    @Transactional
    public void unshare(Long shareId, Long currentUserId) {
        Share share = findById(shareId);

        boolean isRecipe = share.getTargetType() == TargetType.RECIPE;
        ErrorCode notFound = isRecipe ? ErrorCode.RECIPE_SHARE_NOT_FOUND : ErrorCode.POST_SHARE_NOT_FOUND;
        ErrorCode forbidden = isRecipe ? ErrorCode.RECIPE_SHARE_FORBIDDEN : ErrorCode.POST_SHARE_FORBIDDEN;

        if (share == null) throw new AppException(notFound);
        if (!share.getUser().getId().equals(currentUserId)) {
            throw new AppException(forbidden);
        }

        shareRepository.delete(share);
    }

    @Override
    public List<ShareResponse> getByTarget(Long targetId, TargetType targetType) {
        return shareRepository.findByTargetIdAndTargetType(targetId, targetType)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<ShareResponse> getByUserId(Long userId) {
        return shareRepository.findByUserId(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public long count(Long targetId, TargetType targetType) {
        return shareRepository.countByTargetIdAndTargetType(targetId, targetType);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private Share findById(Long id) {
        return shareRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RECIPE_SHARE_NOT_FOUND));
        // lưu ý: nếu muốn phân biệt đúng message RECIPE_SHARE_NOT_FOUND / POST_SHARE_NOT_FOUND
        // ngay từ bước tìm kiếm thì cần biết trước targetType, xử lý ở tầng controller
        // hoặc đổi ErrorCode chung "SHARE_NOT_FOUND" cho gọn.
    }

    private ShareResponse mapToResponse(Share share) {
        ShareResponse.ShareResponseBuilder builder = ShareResponse.builder()
                .id(share.getId())
                .userId(share.getUser().getId())
                .userName(share.getUser().getUsername())
                .userAvatar(share.getUser().getAvatarUrl())
                .targetId(share.getTargetId())
                .targetType(share.getTargetType())
                .createdAt(share.getCreatedAt());

        if (share.getTargetType() == TargetType.RECIPE) {
            recipeRepository.findById(share.getTargetId()).ifPresent(r -> {
                builder.title(r.getName());
                builder.imageUrl(r.getImageUrl());
                builder.slug(r.getSlug());
            });
        } else if (share.getTargetType() == TargetType.POST) {
            postRepository.findById(share.getTargetId()).ifPresent(p -> {
                builder.title(p.getTitle());
                builder.imageUrl(p.getThumbnail());
            });
        }

        return builder.build();
    }
}