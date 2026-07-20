package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.NotificationTargetType;
import com.example.Bep_Viet.enums.NotificationType;
import com.example.Bep_Viet.model.Like;
import com.example.Bep_Viet.repository.LikeRepository;
import com.example.Bep_Viet.repository.PostRepository;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.request.LikeRequest;
import com.example.Bep_Viet.response.LikeResponse;
import com.example.Bep_Viet.service.LikeService;
import com.example.Bep_Viet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.Bep_Viet.repository.ShareRepository;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeRepository likeRepository;
    private final RecipeRepository recipeRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;
    private final ShareRepository shareRepository;
    @Override
    @Transactional
    public LikeResponse toggle(Long userId, LikeRequest request) {
        Like.TargetType type = request.getTargetType();
        Long targetId = request.getTargetId();

        Optional<Like> existing = likeRepository
                .findByUserIdAndTargetIdAndTargetType(userId, targetId, type);

        boolean liked;
        if (existing.isPresent()) {
            likeRepository.delete(existing.get());
            liked = false;
        } else {
            likeRepository.save(Like.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .targetType(type)
                    .build());
            liked = true;

            //  Gửi thông báo khi LIKE (không gửi khi unlike)
            Long ownerId = resolveOwnerId(type, targetId);
            if (ownerId != null) {
                notificationService.send(
                        ownerId,
                        userId,
                        NotificationType.NEW_LIKE,
                        targetId,
                        mapToNotifTargetType(type)
                );
            }
        }

        long total = likeRepository.countByTargetIdAndTargetType(targetId, type);
        return LikeResponse.builder().liked(liked).totalLikes(total).build();
    }

    @Override
    public long count(Long targetId, String targetType) {
        return likeRepository.countByTargetIdAndTargetType(
                targetId, Like.TargetType.valueOf(targetType));
    }

    @Override
    public boolean isLiked(Long userId, Long targetId, String targetType) {
        return likeRepository.existsByUserIdAndTargetIdAndTargetType(
                userId, targetId, Like.TargetType.valueOf(targetType));
    }

    @Override
    public long countTotalLikesByUserId(Long userId) {
        return likeRepository.countTotalLikesByUserId(
                userId,
                Like.TargetType.recipe,
                Like.TargetType.post
        );
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private Long resolveOwnerId(Like.TargetType type, Long targetId) {
        if (type == Like.TargetType.recipe) {
            return recipeRepository.findById(targetId)
                    .map(r -> r.getUser().getId())
                    .orElse(null);
        } else if (type == Like.TargetType.post) {
            return postRepository.findById(targetId)
                    .map(p -> p.getUser().getId())
                    .orElse(null);
        } else if (type == Like.TargetType.share) {
            return shareRepository.findById(targetId).map(s -> s.getUser().getId()).orElse(null);}
            // owner ở đây là người đã share, không phải tác giả recipe/post gốc
        return null;
    }

    private NotificationTargetType mapToNotifTargetType(Like.TargetType type) {
        if (type == Like.TargetType.recipe)  return NotificationTargetType.recipe;
        if (type == Like.TargetType.post)    return NotificationTargetType.post;
        if (type == Like.TargetType.share)  return NotificationTargetType.share;
        return NotificationTargetType.post;
    }
}
