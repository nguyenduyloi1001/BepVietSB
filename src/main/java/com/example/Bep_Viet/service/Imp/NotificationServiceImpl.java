package com.example.Bep_Viet.service.Imp;

import com.example.Bep_Viet.enums.NotificationTargetType;
import com.example.Bep_Viet.enums.NotificationType;
import com.example.Bep_Viet.exception.AppException;
import com.example.Bep_Viet.exception.ErrorCode;
import com.example.Bep_Viet.model.Notification;
import com.example.Bep_Viet.model.User;
import com.example.Bep_Viet.repository.NotificationRepository;
import com.example.Bep_Viet.repository.PostRepository;
import com.example.Bep_Viet.repository.RecipeRepository;
import com.example.Bep_Viet.repository.UserRepository;
import com.example.Bep_Viet.response.NotificationResponse;
import com.example.Bep_Viet.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final PostRepository postRepository; // ← thêm

    @Override
    public List<NotificationResponse> getMyNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public List<NotificationResponse> getMyUnread(Long userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream().map(this::mapToResponse).toList();
    }

    @Override
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    public NotificationResponse markOneAsRead(Long notificationId, Long currentUserId) {
        Notification notification = findById(notificationId);
        if (!notification.getUserId().equals(currentUserId))
            throw new AppException(ErrorCode.NOTIFICATION_UNAUTHORIZED);
        notification.setIsRead(true);
        return mapToResponse(notificationRepository.save(notification));
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    public NotificationResponse send(Long receiverId, Long actorId,
                                     NotificationType type, Long targetId,
                                     NotificationTargetType targetType,
                                     String note) {
        if (actorId != null && actorId.equals(receiverId)) return null;

        Notification saved = notificationRepository.save(Notification.builder()
                .userId(receiverId).actorId(actorId)
                .type(type).targetId(targetId).targetType(targetType)
                .note(note)
                .build());
        return mapToResponse(saved);
    }
    @Override
    public NotificationResponse send(Long receiverId, Long actorId,
                                     NotificationType type, Long targetId,
                                     NotificationTargetType targetType) {
        return send(receiverId, actorId, type, targetId, targetType, null);
    }

    private Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }

    private NotificationResponse mapToResponse(Notification n) {
        return NotificationResponse.builder()
                .id(n.getId()).userId(n.getUserId()).actorId(n.getActorId())
                .type(n.getType()).targetId(n.getTargetId()).targetType(n.getTargetType())
                .isRead(n.getIsRead()).createdAt(n.getCreatedAt())
                .message(buildMessage(n))
                .build();
    }

    private String buildMessage(Notification n) {
        String actor = n.getActorId() != null
                ? userRepository.findById(n.getActorId())
                .map(User::getFullName).orElse("Người dùng ẩn")
                : "Hệ thống";

        // ── Lấy tên target theo loại ──
        String targetName = resolveTargetName(n.getTargetType(), n.getTargetId());

        // ── Loại nội dung (công thức / bài viết / bình luận) ──
        String targetLabel = resolveTargetLabel(n.getTargetType());

        return switch (n.getType()) {
            case NEW_FOLLOWER ->
                    actor + " đã bắt đầu theo dõi bạn.";
            case NEW_COMMENT ->
                    actor + " đã bình luận vào " + targetLabel + " \"" + targetName + "\".";
            case NEW_LIKE ->
                    actor + " đã thích " + targetLabel + " \"" + targetName + "\".";
            case NEW_ANSWER ->
                    actor + " đã trả lời bình luận của bạn.";
            case RECIPE_APPROVED ->
                    "Công thức \"" + targetName + "\" đã được duyệt!";
            case RECIPE_REJECTED ->
                    "Công thức \"" + targetName + "\" chưa được duyệt. Vui lòng chỉnh sửa lại.";
            case ROLE_REQUEST_APPROVED ->  // 👈 thêm
                    "Chúc mừng! Đơn đăng ký vai trò của bạn đã được duyệt.";
            case ROLE_REQUEST_REJECTED -> {
                String reason = n.getNote() != null && !n.getNote().isBlank()
                        ? " Lý do: " + n.getNote()
                        : "";
                yield "Đơn đăng ký vai trò của bạn đã bị từ chối." + reason;
            }
        };
    }

    private String resolveTargetName(NotificationTargetType type, Long targetId) {
        if (type == null || targetId == null) return "";
        return switch (type) {
            case recipe -> recipeRepository.findById(targetId)
                    .map(r -> r.getName()).orElse("công thức");
            case post   -> postRepository.findById(targetId)
                    .map(p -> p.getTitle()).orElse("bài viết");
            default     -> "";
        };
    }

    private String resolveTargetLabel(NotificationTargetType type) {
        if (type == null) return "nội dung";
        return switch (type) {
            case recipe  -> "công thức";
            case post    -> "bài viết";
            default      -> "nội dung";
        };
    }
}