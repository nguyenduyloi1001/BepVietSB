package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.NotificationTargetType;
import com.example.Bep_Viet.enums.NotificationType;
import com.example.Bep_Viet.enums.TargetType;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;

import com.example.Bep_Viet.model.Comment;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.CommentRepository;
import com.example.Bep_Viet.repository.PostRepository;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.request.CommentRequest;
import com.example.Bep_Viet.response.CommentResponse;

import com.example.Bep_Viet.service.CommentService;
import com.example.Bep_Viet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final PostRepository postRepository;
    private final NotificationService notificationService;

    @Override
    public CommentResponse create(CommentRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Comment parent = null;
        if (request.getParentId() != null) {
            parent = commentRepository.findById(request.getParentId())
                    .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
        }

        Comment comment = Comment.builder()
                .user(user)
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .parent(parent)
                .content(request.getContent())
                .build();

        Comment saved = commentRepository.save(comment);

//        // ─── Gửi thông báo ───────────────────────────────────────
        NotificationTargetType notifTargetType = mapToNotifTargetType(request.getTargetType());

        if (parent != null) {
            // Reply → báo cho chủ comment gốc (new_answer)
            notificationService.send(
                    parent.getUser().getId(),
                    userId,
                    NotificationType.NEW_ANSWER,
                    saved.getId(),
                    notifTargetType
            );
        } else {
            // Comment mới → báo cho chủ bài (new_comment)
            Long contentOwnerId = resolveContentOwnerId(request.getTargetType(), request.getTargetId());
            if (contentOwnerId != null) {
                notificationService.send(
                        contentOwnerId,
                        userId,
                        NotificationType.NEW_COMMENT,
                        request.getTargetId(),
                        notifTargetType
                );
            }
        }
        return mapToResponse(saved);
    }


    @Override
    public List<CommentResponse> getByTarget(Long targetId, TargetType targetType) {
        return commentRepository
                .findByTargetIdAndTargetTypeAndParentIsNull(targetId, targetType)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public CommentResponse update(Long id, String content, Long currentUserId) {
        Comment comment = findById(id);
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        comment.setContent(content);
        return mapToResponse(commentRepository.save(comment));
    }

    @Override
    public void delete(Long id, Long currentUserId) {
        Comment comment = findById(id);
        if (!comment.getUser().getId().equals(currentUserId)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        commentRepository.delete(comment);
    }

    // ─── Helpers ─────────────────────────────────────────────────

    private Comment findById(Long id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private Long resolveContentOwnerId(TargetType targetType, Long targetId) {
        if (targetType == TargetType.RECIPE) {
            return recipeRepository.findById(targetId)
                    .map(r -> r.getUser().getId())
                    .orElse(null);
        } else if (targetType == TargetType.POST) {
            return postRepository.findById(targetId)
                    .map(p -> p.getUser().getId())
                    .orElse(null);
        }
        return null;
    }

    private NotificationTargetType mapToNotifTargetType(TargetType targetType) {
        if (targetType == TargetType.RECIPE) {
            return NotificationTargetType.recipe;
        } else if (targetType == TargetType.POST) {
            return NotificationTargetType.post;
        } else if (targetType == TargetType.QUESTION) {
            return NotificationTargetType.question;
        }
        return NotificationTargetType.post;
    }

    private CommentResponse mapToResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getUsername())
                .userAvatar(comment.getUser().getAvatarUrl())
                .targetId(comment.getTargetId())
                .targetType(comment.getTargetType())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .content(comment.getContent())
                .replies(comment.getReplies().stream().map(this::mapToResponse).toList())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
    }
}